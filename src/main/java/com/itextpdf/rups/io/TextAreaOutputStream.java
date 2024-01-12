/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.rups.io;

import javax.swing.JTextArea;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Everything writing to this OutputStream will be shown in a JTextArea.
 */
public class TextAreaOutputStream extends OutputStream {
    /**
     * The text area to which we want to write.
     */
    protected JTextArea text;

    /**
     * Constructs a TextAreaOutputStream.
     *
     * @param text the text area to which we want to write.
     */
    public TextAreaOutputStream(JTextArea text) {
        this.text = text;
        clear();
    }

    /**
     * Clear the text area.
     */
    public void clear() {
        text.setText("");
    }

    /**
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int i) {
        final byte[] b = {(byte) i};
        write(b, 0, 1);
    }

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) {
        final String snippet = new String(b, off, len, StandardCharsets.UTF_8);
        text.append(snippet);
    }

    /**
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);
        final int bufferSize = 1024;
        final byte[] snippet = new byte[bufferSize];
        int bytesread;
        while ((bytesread = bais.read(snippet)) > 0) {
            write(snippet, 0, bytesread);
        }
    }
}
