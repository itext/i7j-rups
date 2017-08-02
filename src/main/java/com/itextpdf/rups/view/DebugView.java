package com.itextpdf.rups.view;

import com.itextpdf.rups.model.SwingHelper;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.io.IOException;
import java.io.OutputStream;

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
     *
     * @return DebugView
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
