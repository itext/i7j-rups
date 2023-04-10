/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

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

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
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
    private DebugView() {
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
            debugView = new DebugView();
        }
        return debugView;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    private void updateTextPane(final String msg) {
        SwingUtilities.invokeLater(new UpdateTextPaneTask(msg));
    }

    static class UpdateTextPaneTask implements Runnable {
        private final String msg;

        public UpdateTextPaneTask(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            JTextArea textArea = DebugView.getInstance().getTextArea();
            if (textArea.getLineCount() >= MAX_LINES) {
                String backupString = "";
                try {
                    backupString = textArea.getText(Math.max(
                                    textArea.getDocument().getLength() - BACKUP_SIZE, 0),
                            Math.min(textArea.getDocument().getLength(), BACKUP_SIZE));
                } catch (BadLocationException ignored) {
                    // Intentionally left blank
                }
                textArea.setText(backupString + Language.ERROR_TOO_MANY_OUTPUT.getString());
            }
            textArea.append(msg);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    static class DebugOutputStream extends OutputStream {
        @Override
        public void write(final int b) {
            DebugView.getInstance().updateTextPane(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b, int off, int len) {
            DebugView.getInstance().updateTextPane(new String(b, off, len));
        }

        @Override
        public void write(byte[] b) {
            write(b, 0, b.length);
        }
    }
}
