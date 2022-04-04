package com.itextpdf.rups.view.itext;


import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.LoggerMessages;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ContentStreamSyntaxDocument extends DefaultStyledDocument {

    private static final int INLINE_IMAGE_EXPECTED_TOKEN_COUNT = 2;

    public final Object BINARY_CONTENT = new Object();
    public final Object ENCODING = new Object();

    private final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Syntax highlight attributes for operators
     */
    protected static final Map<String, Map<Object, Object>> attributemap;

    static {
        attributemap = new HashMap<>();
        initAttributes();
    }


    private Element skipUntilParent(Element parent, ElementIterator it) {
        Element current;
        while ((current = it.next()) != null) {
            if(current.getParentElement() == parent) {
                return current;
            }
        }
        return null;
    }

    private String getContentEncoding(Element el) {
        Object encAttr = el.getAttributes().getAttribute(ENCODING);
        return encAttr != null ? (String) encAttr : PdfEncodings.PDF_DOC_ENCODING;
    }

    public void write(OutputStream os) throws IOException, BadLocationException {
        ElementIterator it = new ElementIterator(getDefaultRootElement());
        Element current;

        while ((current = it.next()) != null) {
            byte[] binaryContent = (byte[]) current.getAttributes().getAttribute(BINARY_CONTENT);
            if(binaryContent != null) {
                // write binary content, and...
                os.write(binaryContent);
                // ...skip all children of the current element
                current = skipUntilParent(current.getParentElement(), it);
                if(current == null) {
                    break;
                }
            }
            if(current.isLeaf()) {
                int start = current.getStartOffset();
                int end = current.getEndOffset();
                String text = getText(start, end - start);
                String enc = getContentEncoding(current);
                os.write(PdfEncodings.convertToBytes(text, enc));
            }
        }
    }

    /**
     * Append a PDF object to the content stream syntax document.
     *
     * @param obj     The object to add.
     * @param attrs   The attributes to add.
     */
    public void appendPdfObject(PdfObject obj, AttributeSet attrs) {
        switch (obj.getType()) {
            case PdfObject.STRING:
                appendPdfString((PdfString) obj, attrs);
                break;
            case PdfObject.DICTIONARY:
                PdfDictionary dict = (PdfDictionary) obj;
                appendText("<<", attrs);
                for (PdfName key : dict.keySet()) {
                    appendPdfObject(key, attrs);
                    appendPdfObject(dict.get(key, false), attrs);
                }
                appendText(">> ", attrs);
                break;
            case PdfObject.ARRAY:
                appendText("[", attrs);
                for (PdfObject item : (PdfArray) obj) {
                    appendPdfObject(item, attrs);
                }
                appendText("] ", attrs);
                break;
            default:
                appendText(obj + " ", attrs);
        }
    }

    protected void appendGraphicsOperator(java.util.List<PdfObject> tokens, boolean matchingOperands) {

        // operator is at the end
        final String operator = (tokens.get(tokens.size() - 1)).toString();
        // Inline images are parsed as stream + EI
        if ("EI".equals(operator)
                && tokens.size() == INLINE_IMAGE_EXPECTED_TOKEN_COUNT
                && tokens.get(0) instanceof PdfStream) {
            appendInlineImage((PdfStream) tokens.get(0));
            return;
        }
        final AttributeSet attributes = packageAttributes(attributemap.get(operator));
        for (int i = 0; i < tokens.size() - 1; i++) {
            appendPdfObject(tokens.get(i), matchingOperands ? attributes : null);
        }
        appendText(operator + "\n", attributes);
    }

    /**
     * Append an inline image to the content stream syntax document.
     *
     * @param stm
     *   The {@link PdfStream} containing the image data.
     */
    protected void appendInlineImage(final PdfStream stm) {
        appendText("BI\n", packageAttributes(attributemap.get("BI")));

        for (final PdfName key : stm.keySet()) {
            appendPdfObject(key, null);
            appendPdfObject(stm.get(key, false), null);
        }
        appendText("\nID\n", packageAttributes(attributemap.get("ID")));

        boolean imageShown = false;
        if (stm.get(PdfName.Width, false).isNumber()
                && stm.get(PdfName.Height, false).isNumber()) {
            imageShown = insertAndRenderInlineImage(stm);
        }
        if(!imageShown) {
            // display error message backed by bytes
            MutableAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(BINARY_CONTENT, stm.getBytes(false));
            appendText("Could not process image content\n", attrs);
        }
        appendText("EI\n", packageAttributes(attributemap.get("EI")));
    }

    protected static JButton createSaveImageButton(final BufferedImage saveImg) {

        final JButton saveImgButton = new JButton("Save Image");
        saveImgButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {
                    FileDialog fileDialog = new FileDialog(new Frame(), "Save", FileDialog.SAVE);
                    fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".jpg"));
                    fileDialog.setFile("Untitled.jpg");
                    fileDialog.setVisible(true);
                    ImageIO.write(saveImg, "jpg", new File(fileDialog.getDirectory() + fileDialog.getFile()));
                } catch (HeadlessException | IOException e) {
                    LoggerHelper.error(LoggerMessages.IMAGE_PARSING_ERROR, e, getClass());
                }
            }
        });
        return saveImgButton;
    }

    /**
     * Initialize the syntax highlighting attributes.
     * This could be read from a configuration file, but is hard coded for now
     */
     private static void initAttributes() {
        Map<Object, Object> opConstructionPainting = new HashMap<>();
        Color darkorange = new Color(255, 140, 0);
        opConstructionPainting.put(StyleConstants.Foreground, darkorange);
        opConstructionPainting.put(StyleConstants.Background, Color.WHITE);
        attributemap.put("m", opConstructionPainting);
        attributemap.put("l", opConstructionPainting);
        attributemap.put("c", opConstructionPainting);
        attributemap.put("v", opConstructionPainting);
        attributemap.put("y", opConstructionPainting);
        attributemap.put("h", opConstructionPainting);
        attributemap.put("re", opConstructionPainting);
        attributemap.put("S", opConstructionPainting);
        attributemap.put("s", opConstructionPainting);
        attributemap.put("f", opConstructionPainting);
        attributemap.put("F", opConstructionPainting);
        attributemap.put("f*", opConstructionPainting);
        attributemap.put("B", opConstructionPainting);
        attributemap.put("B*", opConstructionPainting);
        attributemap.put("b", opConstructionPainting);
        attributemap.put("b*", opConstructionPainting);
        attributemap.put("n", opConstructionPainting);
        attributemap.put("W", opConstructionPainting);
        attributemap.put("W*", opConstructionPainting);

        Map<Object, Object> graphicsdelim = new HashMap<>();
        graphicsdelim.put(StyleConstants.Foreground, Color.WHITE);
        graphicsdelim.put(StyleConstants.Background, Color.RED);
        graphicsdelim.put(StyleConstants.Bold, true);
        attributemap.put("q", graphicsdelim);
        attributemap.put("Q", graphicsdelim);

        Map<Object, Object> graphics = new HashMap<>();
        graphics.put(StyleConstants.Foreground, Color.RED);
        graphics.put(StyleConstants.Background, Color.WHITE);
        attributemap.put("w", graphics);
        attributemap.put("J", graphics);
        attributemap.put("j", graphics);
        attributemap.put("M", graphics);
        attributemap.put("d", graphics);
        attributemap.put("ri", graphics);
        attributemap.put("i", graphics);
        attributemap.put("gs", graphics);
        attributemap.put("cm", graphics);
        attributemap.put("g", graphics);
        attributemap.put("G", graphics);
        attributemap.put("rg", graphics);
        attributemap.put("RG", graphics);
        attributemap.put("k", graphics);
        attributemap.put("K", graphics);
        attributemap.put("cs", graphics);
        attributemap.put("CS", graphics);
        attributemap.put("sc", graphics);
        attributemap.put("SC", graphics);
        attributemap.put("scn", graphics);
        attributemap.put("SCN", graphics);
        attributemap.put("sh", graphics);

        Map<Object, Object> xObject = new HashMap<>();
        xObject.put(StyleConstants.Foreground, Color.BLACK);
        xObject.put(StyleConstants.Background, Color.YELLOW);
        attributemap.put("Do", xObject);

        Map<Object, Object> inlineImage = new HashMap<>();
        inlineImage.put(StyleConstants.Foreground, Color.BLACK);
        inlineImage.put(StyleConstants.Background, Color.YELLOW);
        inlineImage.put(StyleConstants.Italic, true);
        attributemap.put("BI", inlineImage);
        attributemap.put("EI", inlineImage);

        Map<Object, Object> textdelim = new HashMap<>();
        textdelim.put(StyleConstants.Foreground, Color.WHITE);
        textdelim.put(StyleConstants.Background, Color.BLUE);
        textdelim.put(StyleConstants.Bold, true);
        attributemap.put("BT", textdelim);
        attributemap.put("ET", textdelim);

        Map<Object, Object> text = new HashMap<>();
        text.put(StyleConstants.Foreground, Color.BLUE);
        text.put(StyleConstants.Background, Color.WHITE);
        attributemap.put("ID", text);
        attributemap.put("Tc", text);
        attributemap.put("Tw", text);
        attributemap.put("Tz", text);
        attributemap.put("TL", text);
        attributemap.put("Tf", text);
        attributemap.put("Tr", text);
        attributemap.put("Ts", text);
        attributemap.put("Td", text);
        attributemap.put("TD", text);
        attributemap.put("Tm", text);
        attributemap.put("T*", text);
        attributemap.put("Tj", text);
        attributemap.put("'", text);
        attributemap.put("\"", text);
        attributemap.put("TJ", text);

        Map<Object, Object> markedContent = new HashMap<>();
        markedContent.put(StyleConstants.Foreground, Color.MAGENTA);
        markedContent.put(StyleConstants.Background, Color.WHITE);
        attributemap.put("BMC", markedContent);
        attributemap.put("BDC", markedContent);
        attributemap.put("EMC", markedContent);
    }

    private void appendPdfString(PdfString str, AttributeSet attrs) {
        if (str.isHexWriting()) {
            StringBuilder sb = new StringBuilder();
            sb.append('<');
            hexlify(str.getValueBytes(), sb);
            sb.append("> ");
            appendText(sb.toString(), attrs);
        } else {
            // TODO encoding logic goes here
            appendText("(" + str + ") ", attrs);
        }
    }

    private AttributeSet packageAttributes(Map<Object, Object> attr) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = SimpleAttributeSet.EMPTY;
        // some default attributes
        if (attr == null) {
            attr = new HashMap<>();
            attr.put(StyleConstants.Foreground, Color.BLACK);
            attr.put(StyleConstants.Background, Color.WHITE);
        }
        // add attributes
        for (Object key : attr.keySet()) {
            aset = sc.addAttribute(aset, key, attr.get(key));
        }
        return aset;
    }

    private void appendText(String s, AttributeSet attr) {
        int len = getLength();
        try {
            insertString(len, s, attr != null ? attr : SimpleAttributeSet.EMPTY);
        } catch (BadLocationException e) {
            LoggerHelper.error(LoggerMessages.UNEXPECTED_EXCEPTION_DEFAULT, e, getClass());
        }
    }

    private void appendDisplayOnlyNewline() {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(BINARY_CONTENT, new byte[0]);

        try {
            insertString(getLength(), "\n", attrs);
        } catch (BadLocationException e) {
            LoggerHelper.error(LoggerMessages.UNEXPECTED_EXCEPTION_DEFAULT, e, getClass());
        }
    }

    private boolean insertAndRenderInlineImage(final PdfStream stm) {
        BufferedImage img;
        try {
            // inline image parser takes care of expanding abbreviations
            img = new PdfImageXObject(stm).getBufferedImage();
        } catch (IOException e) {
            LoggerHelper.error(LoggerMessages.UNEXPECTED_EXCEPTION_DEFAULT, e, getClass());
            return false;
        }
        MutableAttributeSet imageAttrs = new SimpleAttributeSet();
        StyleConstants.setIcon(imageAttrs, new ImageIcon(img));
        imageAttrs.addAttribute(BINARY_CONTENT, stm.getBytes(false));

        try {
            // add the image
            insertString(getLength(), " ", imageAttrs);
            appendDisplayOnlyNewline();
            // add the button
            MutableAttributeSet buttonAttrs = new SimpleAttributeSet();
            StyleConstants.setComponent(buttonAttrs, createSaveImageButton(img));
            buttonAttrs.addAttribute(BINARY_CONTENT, new byte[0]);
            insertString(getLength(), " ", buttonAttrs);
            appendDisplayOnlyNewline();
        } catch (BadLocationException e) {
            LoggerHelper.error(LoggerMessages.UNEXPECTED_EXCEPTION_DEFAULT, e, getClass());
            return false;
        }
        return true;
    }

    private void hexlify(byte[] bytes, StringBuilder sb) {
        for(byte b : bytes) {
            sb.append(HEX_DIGITS[(b >> 4) & 0xf]);
            sb.append(HEX_DIGITS[b & 0xf]);
        }
    }
}
