/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfResources;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.canvas.parser.util.PdfCanvasParser;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.LoggerMessages;
import com.itextpdf.rups.view.contextmenu.ContextMenuMouseListener;
import com.itextpdf.rups.view.contextmenu.StreamPanelContextMenu;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class SyntaxHighlightedStreamPane extends JScrollPane implements Observer {

    /**
     * The text pane with the content stream.
     */
    protected final JTextPane text;

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

    private static final int MAX_NUMBER_OF_EDITS = 8192;

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
     * @param controller the pdf reader controller
     * @param pluginMode the plugin mode
     */
    public SyntaxHighlightedStreamPane(PdfReaderController controller, boolean pluginMode) {
        super();
        text = new JTextPane();
        text.setDocument(new ContentStreamSyntaxDocument());
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
                        doc.insertString(doc.getLength(), "\n", SimpleAttributeSet.EMPTY);
                        text.insertComponent(ContentStreamSyntaxDocument.createSaveImageButton(img));
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
            } catch (com.itextpdf.io.exceptions.IOException e) {
                text.setText("");
                setTextEditableRoutine(false);
            }
        } else {
            renderGenericContentStream(stream);
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
        int sizeEst = text.getText().length();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(sizeEst);
        try {
            ((ContentStreamSyntaxDocument) text.getDocument()).write(baos);
        } catch (IOException | BadLocationException e) {
            LoggerHelper.error(LoggerMessages.UNEXPECTED_EXCEPTION_DEFAULT, e, getClass());
        }
        ((PdfStream) target.getPdfObject()).setData(baos.toByteArray());
        if (controller != null) {
            controller.selectNode(target);
        }
        manager.setLimit(MAX_NUMBER_OF_EDITS);
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

    private void renderGenericContentStream(PdfStream stream) {
        setTextEditableRoutine(true);
        byte[] bb = null;
        try {
            bb = stream.getBytes();

            final PdfTokenizer tokeniser =
                    new PdfTokenizer(new RandomAccessFileOrArray(RASF.createSource(bb)));

            final PdfCanvasParser ps = new PdfCanvasParser(tokeniser, new PdfResources());
            final ArrayList<PdfObject> tokens = new ArrayList<>();
            while (ps.parse(tokens).size() > 0) {
                ((ContentStreamSyntaxDocument) text.getDocument()).appendGraphicsOperator(tokens, matchingOperands);
            }
        } catch (PdfException | com.itextpdf.io.exceptions.IOException e) {
            LoggerHelper.warn(LoggerMessages.PDFSTREAM_PARSING_ERROR, e, getClass());
            if (bb != null) {
                text.setText(new String(bb));
            }
        } catch (IOException ignored) {
        }
        text.setCaretPosition(0); // set the caret at the start so the panel will show the first line
    }


}


class UndoAction extends AbstractAction {
    private final UndoManager manager;

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
    private final UndoManager manager;

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
