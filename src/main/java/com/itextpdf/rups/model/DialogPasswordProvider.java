/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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
package com.itextpdf.rups.model;

import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.rups.view.Language;

import com.ibm.icu.text.StringPrep;
import com.ibm.icu.text.StringPrepParseException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class DialogPasswordProvider implements IPasswordProvider {
    public static final int MAX_PASSWORD_BYTE_LENGTH = 127;

    private final String title;

    public DialogPasswordProvider(String title) {
        this.title = title;
    }

    public static DialogPasswordProvider anyPassword() {
        return new DialogPasswordProvider(Language.ENTER_ANY_PASSWORD.getString());
    }

    public static DialogPasswordProvider ownerPassword() {
        return new DialogPasswordProvider(Language.ENTER_OWNER_PASSWORD.getString());
    }

    @Override
    public boolean isInteractive() {
        return true;
    }

    /*
     * Ignoring the "Return an empty array instead of null" warning. Here null
     * means, that the user cancelled password input operation. So it is
     * different from an empty password.
     */
    @SuppressWarnings("java:S1168")
    @Override
    public byte[] get(File originalFile) {
        // originalFile is ignored, since we are using a GUI

        final JPasswordField passwordField = new JPasswordField(32);

        final JOptionPane pane =
                new JOptionPane(passwordField, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
                    @Override
                    public void selectInitialValue() {
                        passwordField.requestFocusInWindow();
                    }
                };

        JDialog dialog = pane.createDialog(this.title);
        dialog.setVisible(true);
        dialog.dispose();

        Object selectedValue = pane.getValue();
        // If user didn't click OK in the dialog
        if (!(selectedValue instanceof Integer) || (Integer) selectedValue != JOptionPane.OK_OPTION) {
            return null;
        }
        return preparePasswordForOpen(new String(passwordField.getPassword()));
    }

    private static byte[] preparePasswordForOpen(String inputPassword) {
        final StringPrep prep = StringPrep.getInstance(StringPrep.RFC4013_SASLPREP);
        final String prepped;
        try {
            // we're invoking StringPrep to open a document -> pass ALLOW_UNASSIGNED
            prepped = prep.prepare(inputPassword, StringPrep.ALLOW_UNASSIGNED);
        } catch (StringPrepParseException e) {
            throw new PdfException(Language.ERROR_PASSWORD.getString(), e);
        }
        byte[] resultingBytes = prepped.getBytes(StandardCharsets.UTF_8);
        if (resultingBytes.length <= MAX_PASSWORD_BYTE_LENGTH) {
            return resultingBytes;
        } else {
            byte[] trimmed = new byte[MAX_PASSWORD_BYTE_LENGTH];
            System.arraycopy(resultingBytes, 0, trimmed, 0, trimmed.length);
            return trimmed;
        }
    }
}
