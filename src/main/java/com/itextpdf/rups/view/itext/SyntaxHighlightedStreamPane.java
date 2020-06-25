/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups.view.itext;

import com.itextpdf.io.source.PdfTokenizer;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.canvas.parser.util.PdfCanvasParser;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.LoggerMessages;
import com.itextpdf.rups.view.contextmenu.ContextMenuMouseListener;
import com.itextpdf.rups.view.contextmenu.StreamPanelContextMenu;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class SyntaxHighlightedStreamPane extends JScrollPane implements Observer {

    /**
     * The text pane with the content stream.
     */
    protected ColorTextPane text;

    /**
     * Syntax highlight attributes for operators
     */
    protected static Map<String, Map<Object, Object>> attributemap = null;

    /**
     * Highlight operands according to their operator
     */
    protected static boolean matchingOperands = false;

    /**
     * Factory that allows you to create RandomAccessSource files
     */
    protected static final RandomAccessSourceFactory RASF = new RandomAccessSourceFactory();

    protected StreamPanelContextMenu popupMenu;

    protected PdfObjectTreeNode target;

    protected UndoManager manager;

    private static int MAX_NUMBER_OF_EDITS = 8192;

    //Todo: Remove that field after proper application structure will be implemented.
    private PdfReaderController controller;

    private static Method pdfStreamGetInputStreamMethod;

    static {
        try {
            pdfStreamGetInputStreamMethod = PdfStream.class.getDeclaredMethod("getInputStream");
            pdfStreamGetInputStreamMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException any) {
            pdfStreamGetInputStreamMethod = null;
            LoggerHelper.error(LoggerMessages.REFLECTION_PDFSTREAM_ERROR, any, PdfReaderController.class);
        }
    }

    /**
     * Constructs a SyntaxHighlightedStreamPane.
     *
     * @param controller the pdf reader text
     * @param pluginMode the plugin mode
     */
    public SyntaxHighlightedStreamPane(PdfReaderController controller, boolean pluginMode) {
        super();
        initAttributes();
        text = new ColorTextPane();
        setViewportView(text);
        this.controller = controller;

        popupMenu = new StreamPanelContextMenu(text, this, pluginMode);
        text.setComponentPopupMenu(popupMenu);
        text.addMouseListener(new ContextMenuMouseListener(popupMenu, text));

        manager = new UndoManager();
        manager.setLimit(MAX_NUMBER_OF_EDITS);
        text.getDocument().addUndoableEditListener(manager);
        text.registerKeyboardAction(new UndoAction(manager), KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        text.registerKeyboardAction(new RedoAction(manager), KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (observable instanceof PdfReaderController && obj instanceof RupsEvent) {
            clearPane();
        }
    }

    /**
     * Renders the content stream of a PdfObject or empties the text area.
     *
     * @param target the node of which the content stream needs to be rendered
     */
    public void render(PdfObjectTreeNode target) {
        manager.discardAllEdits();
        manager.setLimit(0);
        this.target = target;
        if (!(target.getPdfObject() instanceof PdfStream)) {
            clearPane();
            return;
        }
        PdfStream stream = (PdfStream) target.getPdfObject();
        text.setText("");
        //Check if stream is image
        if (PdfName.Image.equals(stream.getAsName(PdfName.Subtype))) {
            try {
                //Convert byte array back to Image
                if (!stream.get(PdfName.Width, false).isNumber() && !stream.get(PdfName.Height, false).isNumber())
                    return;
                PdfImageXObject pimg = new PdfImageXObject(stream);
                BufferedImage img = pimg.getBufferedImage();
                if (img == null) {
                    text.setText("Image can't be loaded.");
                } else {
                    //Show image in textpane
                    StyledDocument doc = (StyledDocument) text.getDocument();
                    Style style = doc.addStyle("Image", null);
                    StyleConstants.setIcon(style, new ImageIcon(img));

                    try {
                        doc.insertString(doc.getLength(), "ignored text", style);
                        JButton saveImage = new JButton("Save Image");
                        final BufferedImage saveImg = img;
                        saveImage.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent event) {
                                try {
                                    FileDialog fileDialog = new FileDialog(new Frame(), "Save", FileDialog.SAVE);
                                    fileDialog.setFilenameFilter(new FilenameFilter() {
                                        public boolean accept(File dir, String name) {
                                            return name.endsWith(".jpg");
                                        }
                                    });
                                    fileDialog.setFile("Untitled.jpg");
                                    fileDialog.setVisible(true);
                                    ImageIO.write(saveImg, "jpg", new File(fileDialog.getDirectory() + fileDialog.getFile()));
                                } catch (HeadlessException | IOException e) {
                                    LoggerHelper.error(LoggerMessages.IMAGE_PARSING_ERROR, e, getClass());
                                }
                            }
                        });
                        text.append("\n", null);
                        text.insertComponent(saveImage);
                    } catch (BadLocationException e) {
                        LoggerHelper.error(LoggerMessages.UNEXPECTED_EXCEPTION_DEFAULT, e, getClass());
                    }
                }
            } catch (IOException e) {
                LoggerHelper.error(LoggerMessages.UNEXPECTED_EXCEPTION_DEFAULT, e, getClass());
            }
            setTextEditableRoutine(false);
        } else if (stream.get(PdfName.Length1) != null) {
            try {
                setTextEditableRoutine(true);
                byte[] bytes = stream.getBytes(false);
                text.setText(new String(bytes));
                text.setCaretPosition(0);
            } catch (com.itextpdf.io.IOException e) {
                text.setText("");
                setTextEditableRoutine(false);
            }
        } else if (stream.get(PdfName.Length1) == null) {
            text.registerKeyboardAction(new SaveAction(text, this.target), KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
            setTextEditableRoutine(true);
            String newline = "\n";
            byte[] bb = null;
            try {
                bb = stream.getBytes();

                PdfTokenizer tokeniser = new PdfTokenizer(new RandomAccessFileOrArray(RASF.createSource(bb)));

                PdfCanvasParser ps = new PdfCanvasParser(tokeniser);
                ArrayList<PdfObject> tokens = new ArrayList<>();
                while (ps.parse(tokens).size() > 0) {
                    // operator is at the end
                    String operator = (tokens.get(tokens.size() - 1)).toString();
                    // operands are in front of their operator
                    StringBuilder operandssb = new StringBuilder();
                    for (int i = 0; i < tokens.size() - 1; i++) {
                        append(operandssb, tokens.get(i));
                    }
                    String operands = operandssb.toString();

                    Map<Object, Object> attributes = attributemap.get(operator);
                    Map<Object, Object> attributesOperands = null;
                    if (matchingOperands)
                        attributesOperands = attributes;

                    text.append(operands, attributesOperands);
                    text.append(operator + newline, attributes);
                }
            } catch (PdfException | com.itextpdf.io.IOException e) {
                LoggerHelper.warn(LoggerMessages.PDFSTREAM_PARSING_ERROR, e, getClass());
                if (bb != null) {
                    text.setText(new String(bb));
                }
            } catch (IOException ignored) {
            }
            text.setCaretPosition(0); // set the caret at the start so the panel will show the first line
        }
        text.repaint();
        manager.setLimit(MAX_NUMBER_OF_EDITS);
        repaint();
    }

    public void saveToTarget() {
        manager.discardAllEdits();
        manager.setLimit(0);
        if (controller != null && ((PdfDictionary) target.getPdfObject()).containsKey(PdfName.Filter)) {
            controller.deleteTreeNodeDictChild(target, PdfName.Filter);
        }
        ((PdfStream) target.getPdfObject()).setData(text.getText().getBytes());
        if (controller != null) {
            controller.selectNode(target);
        }
        manager.setLimit(MAX_NUMBER_OF_EDITS);
    }

    protected void append(StringBuilder sb, PdfObject obj) {
        switch (obj.getType()) {
            case PdfObject.STRING:
                PdfString str = (PdfString) obj;
                if (str.isHexWriting()) {
                    sb.append("<");
                    byte[] b = str.getValueBytes();
                    String hex;
                    for (byte aB : b) {
                        hex = Integer.toHexString((aB & 0xFF));
                        if (hex.length() % 2 == 1)
                            sb.append("0");
                        sb.append(hex);
                    }
                    sb.append("> ");
                } else {
                    sb
                            .append("(")
                            .append(obj)
                            .append(") ");
                }
                break;
            case PdfObject.DICTIONARY:
                PdfDictionary dict = (PdfDictionary) obj;
                sb.append("<<");
                for (PdfName key : dict.keySet()) {
                    sb
                            .append(key)
                            .append(" ");
                    append(sb, dict.get(key, false));
                }
                sb.append(">> ");
                break;
            default:
                sb
                        .append(obj)
                        .append(" ");
        }
    }

    /**
     * Initialize the syntax highlighting attributes.
     * This could be read from a configuration file, but is hard coded for now
     */
    protected void initAttributes() {
        attributemap = new HashMap<>();

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

    private void setTextEditableRoutine(boolean editable) {
        text.setEditable(editable);
        if ((pdfStreamGetInputStreamMethod != null) && editable && (target != null) && (target.getPdfObject() instanceof PdfStream)) {
            try {
                popupMenu.setSaveToStreamEnabled(pdfStreamGetInputStreamMethod.invoke(target.getPdfObject()) == null);
                return;
            } catch (Exception any) {
                LoggerHelper.error(LoggerMessages.REFLECTION_INVOCATION_PDFSTREAM_ERROR, any, getClass());
            }
        }
        popupMenu.setSaveToStreamEnabled(false);
    }

    private void clearPane() {
        target = null;
        manager.discardAllEdits();
        manager.setLimit(0);
        text.setText("");
        setTextEditableRoutine(false);
    }

    /**
     * a serial version id.
     */
    private static final long serialVersionUID = -3699893393067753664L;

}

class ColorTextPane extends JTextPane {

    /**
     * Appends a string to the JTextPane, with style attributes applied.
     *
     * @param s    the String to be appended
     * @param attr a Map of attributes used to style the string
     */
    public void append(String s, Map<Object, Object> attr) {
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
        int len = getDocument().getLength();
        setCaretPosition(len);
        setCharacterAttributes(aset, true);
        replaceSelection(s);
    }

    private static final long serialVersionUID = 1302283071087762495L;
}

class UndoAction extends AbstractAction {
    private UndoManager manager;

    public UndoAction(UndoManager manager) {
        this.manager = manager;
    }

    public void actionPerformed(ActionEvent evt) {
        try {
            manager.undo();
        } catch (CannotUndoException e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}

class RedoAction extends AbstractAction {
    private UndoManager manager;

    public RedoAction(UndoManager manager) {
        this.manager = manager;
    }

    public void actionPerformed(ActionEvent evt) {
        try {
            manager.redo();
        } catch (CannotRedoException e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}

class SaveAction extends AbstractAction {

    private final ColorTextPane text;
    private final PdfObjectTreeNode target;

    public SaveAction(ColorTextPane text, PdfObjectTreeNode target) {
        this.text = text;
        this.target = target;
    }

    public void actionPerformed(ActionEvent evt) {
        try {
            String contentStreamContent = this.text.getText();
            PdfObject targetPdfObject = this.target.getPdfObject();
            if ( targetPdfObject instanceof PdfStream ) {
                PdfStream stream = (PdfStream) targetPdfObject;
                stream.setData(contentStreamContent.getBytes());
            }
            System.out.println();
        } catch (CannotRedoException e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
