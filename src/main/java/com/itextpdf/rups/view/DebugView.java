package com.itextpdf.rups.view;

import com.itextpdf.rups.model.SwingHelper;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * A Class that is used for displaying debug messages to a JTextPane.
 */
public class DebugView {

    /**
     * Single DebugView instance.
     */
    private static DebugView debugView = null;

    private final JTextArea textArea = new JTextArea();

    private static final int MAX_LINES = 8192;
    private static final int BACKUP_SIZE = 8192;


    /**
     * Creates a new DebugView object.
     */
    private DebugView(boolean pluginMode) {
        // Add a scrolling text area
        textArea.setEditable(false);
    }

    /**
     * Console is a Singleton class: you can only get one DebugView.
     */
    public static synchronized DebugView getInstance() {
        if (debugView == null) {
            debugView = new DebugView(false);
        }
        return debugView;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    private void updateTextPane(final String msg) {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                Document doc = textArea.getDocument();
                if (textArea.getLineCount() >= MAX_LINES) {
                    String backupString = "";
                    try {
                        backupString = textArea.getText(
                                Math.max(textArea.getDocument().getLength() - BACKUP_SIZE, 0),
                                Math.min(textArea.getDocument().getLength(), BACKUP_SIZE));
                    } catch (BadLocationException ignored) {
                    }
                    textArea.setText(backupString + "\n...too many output\n");
                }
                textArea.append(msg);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        }, true);
    }

    static class DebugOutputStream extends OutputStream {
        @Override
        public void write(final int b) throws IOException {
            DebugView.getInstance().updateTextPane(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            DebugView.getInstance().updateTextPane(new String(b, off, len));
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }
    }
}
