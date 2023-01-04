/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
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
package com.itextpdf.rups.view;

import com.itextpdf.rups.event.ConsoleWriteEvent;
import com.itextpdf.rups.event.RupsEvent;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.Color;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;

/**
 * A Class that is used for displaying logger messages to a {@link JTextPane}.
 */
public class Console extends Observable implements Observer {

    /**
     * Single Console instance.
     */
    private static Console console = null;

    /**
     * The StyleContext for the Console.
     */
    ConsoleStyleContext styleContext = new ConsoleStyleContext();

    /**
     * The text area to which everything is written.
     */
    private final JTextPane textArea = new JTextPane(new DefaultStyledDocument(styleContext));

    private static final int MAX_TEXT_AREA_SIZE = 8192;

    private static final int BUFFER_SIZE = 1024;

    /**
     * Creates a new Console object.
     */
    private Console() {
        // Add a scrolling text area
        textArea.setEditable(false);
    }

    /**
     * Console is a Singleton class: you can only get one Console.
     *
     * @return an instance of the Console
     */
    public static synchronized Console getInstance() {
        if (console == null) {
            console = new Console();
        }
        return console;
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (obj instanceof RupsEvent) {
            final RupsEvent event = (RupsEvent) obj;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    clearWithBuffer("");
                    break;
            }
        }
    }

    private void updateTextPane(final String msg, final String type) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                try {
                    final Document doc = textArea.getDocument();
                    if (doc.getLength() + msg.length() > MAX_TEXT_AREA_SIZE) {
                        Console.this.clearWithBuffer(Language.ERROR_TOO_MANY_OUTPUT.getString());
                    }
                    doc.insertString(doc.getLength(), msg, styleContext.getStyle(type));
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    Console.this.setChanged();
                    Console.this.notifyObservers(new ConsoleWriteEvent());
                } catch (BadLocationException ignored) {
                    // Intentionally Empty
                }
            }
        });
    }

    private void clearWithBuffer(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                try {
                    final String backupString = textArea.getText(
                            Math.max(textArea.getDocument().getLength() - BUFFER_SIZE, 0),
                            Math.min(textArea.getDocument().getLength(), BUFFER_SIZE));
                    textArea.setText("");
                    final Document doc = textArea.getDocument();
                    doc.insertString(doc.getLength(), backupString, styleContext.getStyle(ConsoleStyleContext.BACKUP));
                    doc.insertString(doc.getLength(), message == null ? "" : message,
                            styleContext.getStyle(ConsoleStyleContext.INFO));
                } catch (BadLocationException any) {
                    textArea.setText(message == null ? "" : message);
                }
                textArea.setCaretPosition(textArea.getDocument().getLength());
                Console.this.setChanged();
                Console.this.notifyObservers(new ConsoleWriteEvent());
            }
        });
    }

    /**
     * Get the JTextArea to which everything is written.
     *
     * @return the {@link JTextArea} to which everything is written
     */
    public JTextPane getTextArea() {
        return textArea;
    }

    static class ConsoleOutputStream extends OutputStream {

        private final String type;

        ConsoleOutputStream(String type) {
            this.type = type;
        }

        @Override
        public void write(final int b) {
            Console.getInstance().updateTextPane(String.valueOf((char) b), type);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            Console.getInstance().updateTextPane(new String(b, off, len), type);
        }

        @Override
        public void write(byte[] b) {
            write(b, 0, b.length);
        }
    }

    /**
     * The style context defining the styles of each type of PrintStream.
     */
    static class ConsoleStyleContext extends StyleContext {

        /**
         * The name of the Style used for Info messages
         */
        public static final String INFO = Language.CONSOLE_INFO.getString();
        /**
         * The name of the Style used for chunks of text left after clean up
         */
        public static final String BACKUP = Language.CONSOLE_BACKUP.getString();
        /**
         * The name of the Style used for Error and Warning messages
         */
        public static final String ERROR = Language.CONSOLE_ERROR.getString();

        /**
         * Creates the style context for the Console.
         */
        public ConsoleStyleContext() {
            super();
            final Style root = getStyle(DEFAULT_STYLE);
            Style s = addStyle(INFO, root);
            StyleConstants.setForeground(s, Color.BLACK);
            s = addStyle(BACKUP, root);
            StyleConstants.setForeground(s, Color.LIGHT_GRAY);
            s = addStyle(ERROR, root);
            StyleConstants.setForeground(s, Color.RED);
        }
    }
}
