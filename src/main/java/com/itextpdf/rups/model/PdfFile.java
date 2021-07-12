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
package com.itextpdf.rups.model;

import com.itextpdf.kernel.exceptions.BadPasswordException;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.ReaderProperties;

import com.ibm.icu.text.StringPrepParseException;
import com.ibm.icu.text.StringPrep;
import com.itextpdf.kernel.pdf.StampingProperties;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Wrapper for both iText's PdfReader (referring to a PDF file to read)
 * and SUN's PDFFile (referring to the same PDF file to render).
 */
public class PdfFile {

    public static final String IGNORE_PERMS_ENV_VAR = "RUPS_IGNORE_PERMS";
    /**
     * The directory where the file can be found (if the PDF was passed as a file).
     */
    protected File directory = null;

    /**
     * The original filename.
     */
    protected String filename = null;

    /**
     * The PdfDocument object.
     */
    protected PdfDocument document = null;

    /**
     * Raw content
     */
    protected byte[] rawContent = null;

    protected ByteArrayOutputStream baos = null;

    protected boolean readOnly = false;

    public static final int MAX_PASSWORD_BYTE_LENGTH = 127;

    /**
     * Constructs a PdfFile object.
     *
     * @param file the File to read
     * @throws IOException  an I/O exception
     * @throws PdfException a PDF exception
     */
    public PdfFile(File file) throws IOException, PdfException {
        if (file == null)
            throw new IOException("No file selected.");
        directory = file.getParentFile();
        filename = file.getName();
        try {
            readFile(new FileInputStream(file), false, readOnly);
        } catch (BadPasswordException bpe) {
            readFile(new FileInputStream(file), true, readOnly);
        }
    }

    /**
     * Constructs a PdfFile object.
     *
     * @param file the byte[] to read
     * @param readOnly read only
     * @throws IOException  an I/O exception
     * @throws PdfException a PDF exception
     */
    public PdfFile(byte[] file, boolean readOnly) throws IOException, PdfException {
        rawContent = file;
        this.readOnly = readOnly;

        try {
            readFile(new ByteArrayInputStream(file), false, readOnly);
        } catch (BadPasswordException bpe) {
            readFile(new ByteArrayInputStream(file), true, readOnly);
        }
    }

    private static byte[] preparePasswordForOpen(String inputPassword) {
        StringPrep prep = StringPrep.getInstance(StringPrep.RFC4013_SASLPREP);
        String prepped;
        try {
            // we're invoking StringPrep to open a document -> pass ALLOW_UNASSIGNED
            prepped = prep.prepare(inputPassword, StringPrep.ALLOW_UNASSIGNED);
        } catch (StringPrepParseException e) {
            throw new PdfException("Failed to process password", e);
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

    private static byte[] requestPassword() {
        final JPasswordField passwordField = new JPasswordField(32);

        JOptionPane pane = new JOptionPane(passwordField, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
            @Override
            public void selectInitialValue() {
                passwordField.requestFocusInWindow();
            }
        };

        pane.createDialog(null, "Enter the User or Owner Password of this PDF file").setVisible(true);

        String passwordString = new String(passwordField.getPassword());
        return preparePasswordForOpen(passwordString);
    }

    private static boolean checkIgnorePermissions() {
        // set unethical reading based on environment variable
        final String ignorePermsEnv;
        try {
            ignorePermsEnv = System.getenv(IGNORE_PERMS_ENV_VAR);
        } catch (SecurityException ex) {
            return false;
        }
        return ignorePermsEnv != null && "1".equals(ignorePermsEnv.trim());
    }

    /**
     * Does the actual reading of the file into PdfReader and PDFFile.
     *
     * @param fis       inputstream
     * @param checkPass check password
     * @param readOnly  read only
     * @throws IOException  an I/O exception
     * @throws PdfException a PDF exception
     */
    protected final void readFile(InputStream fis, boolean checkPass, boolean readOnly)
            throws IOException, PdfException {
        // reading the file into PdfReader
        final ReaderProperties readerProps = new ReaderProperties();
        if (checkPass) {
            readerProps.setPassword(requestPassword());
        }
        final PdfReader reader = new PdfReader(fis, readerProps)
                .setUnethicalReading(checkIgnorePermissions());
        baos = new ByteArrayOutputStream();
        if (readOnly) {
            document = new PdfDocument(reader);
        } else {
            document = new PdfDocument(reader, new PdfWriter(baos), new StampingProperties().preserveEncryption());
        }
        // we have some extra work to do if the document was encrypted
        if(reader.isEncrypted() && !reader.isOpenedWithFullPermission()) {
            JOptionPane.showMessageDialog(
                    null,
                    "You opened the document using the user password instead of the owner password.");
        }
    }

    /**
     * Getter for iText's PdfDocument object.
     *
     * @return a PdfDocument object
     */
    public PdfDocument getPdfDocument() {
        return document;
    }

    /**
     * Getter for the filename
     *
     * @return the original filename
     * @since 5.0.3
     */
    public String getFilename() {
        return filename;
    }

    public File getDirectory() {
        return directory;
    }

    public String getRawContent() {
        try {
            return new String(rawContent, "Cp1252");
        } catch (UnsupportedEncodingException e) {
            return "Wrong Encoding";
        }
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return baos;
    }
}
