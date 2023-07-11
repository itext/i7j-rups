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

import com.itextpdf.kernel.exceptions.BadPasswordException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.ReaderProperties;
import com.itextpdf.rups.view.Language;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Wrapper for both iText's PdfReader (referring to a PDF file to read)
 * and SUN's PDFFile (referring to the same PDF file to render).
 */
public final class PdfFile implements IPdfFile {
    /**
     * The original PDF document location.
     */
    private final File originalFile;

    /**
     * Raw content
     */
    private final byte[] originalContent;

    /**
     * The PdfDocument object.
     */
    private PdfDocument document = null;

    private ByteArrayOutputStream writerOutputStream = null;

    private PdfFile(File file, byte[] content) {
        this.originalFile = file;
        this.originalContent = content;
    }

    public static PdfFile open(File file) throws IOException {
        return open(file, Files.readAllBytes(file.toPath()), DialogPasswordProvider.anyPassword());
    }

    public static PdfFile open(File file, IPasswordProvider passwordProvider) throws IOException {
        return open(file, Files.readAllBytes(file.toPath()), passwordProvider);
    }

    public static PdfFile open(File file, byte[] content) throws IOException {
        return open(file, content, DialogPasswordProvider.anyPassword());
    }

    public static PdfFile open(File file, byte[] content, IPasswordProvider passwordProvider) throws IOException {
        final PdfFile pdfFile = new PdfFile(file, content);
        pdfFile.openDocument(passwordProvider, false);
        return pdfFile;
    }

    public static PdfFile openAsOwner(File file) throws IOException {
        return openAsOwner(file, Files.readAllBytes(file.toPath()), DialogPasswordProvider.ownerPassword());
    }

    public static PdfFile openAsOwner(File file, IPasswordProvider passwordProvider) throws IOException {
        return openAsOwner(file, Files.readAllBytes(file.toPath()), passwordProvider);
    }

    public static PdfFile openAsOwner(File file, byte[] content) throws IOException {
        return openAsOwner(file, content, DialogPasswordProvider.ownerPassword());
    }


    public static PdfFile openAsOwner(File file, byte[] content, IPasswordProvider passwordProvider)
            throws IOException {
        final PdfFile pdfFile = new PdfFile(file, content);
        pdfFile.openDocument(passwordProvider, true);
        return pdfFile;
    }

    @Override
    public File getOriginalFile() {
        return originalFile;
    }

    @Override
    public PdfDocument getPdfDocument() {
        return document;
    }

    @Override
    public byte[] getOriginalContent() {
        return originalContent;
    }

    @Override
    public ByteArrayOutputStream getByteArrayOutputStream() {
        return writerOutputStream;
    }

    /**
     * Opens the document, using the file and content stored in the current
     * object. If password is required, then the password provider will be
     * called.
     */
    private void openDocument(IPasswordProvider passwordProvider, boolean requireEditable)
            throws IOException {
        /*
         * This should pass in the majority of cases (i.e. if the document is
         * non-encrypted).
         */
        if (openDocumentReadWrite()) {
            return;
        }

        /*
         * If it is, actually, protected, we will try to open it in a read-only
         * mode. This should pass, if the document is protected, but there is no
         * user password.
         *
         * In this case any editing operations in RUPS should be disabled.
         *
         * If the requireEditable flag is set to true, we skip trying to open
         * the document in a read-only mode.
         */
        if (!requireEditable && openDocumentReadOnly()) {
            return;
        }

        /*
         * If it, actually, has a user password set, then we will use the
         * password provider to get the password and will try to use it to open
         * the document.
         *
         * Since user can provide any of the two password, we will try first to
         * open as an owner (read/write) and then, if failed, as a user
         * (read-only). If both failed, we will keep asking for the correct
         * password until password provider signals cancellation.
         *
         * If the requireEditable flag is set to true, we skip trying to open
         * the document in a read-only mode.
         */
        while (true) {
            final byte[] password = passwordProvider.get(getOriginalFile());
            if (password == null) {
                throw new BadPasswordException(Language.ERROR_MISSING_PASSWORD.getString());
            }
            if (openDocumentReadWrite(password)) {
                return;
            }
            if (!requireEditable && openDocumentReadOnly(password)) {
                return;
            }
            /*
             * If the password provider is not interactive and the password is
             * incorrect, then there is no point in trying again.
             */
            if (!passwordProvider.isInteractive()) {
                throw new BadPasswordException(Language.ERROR_WRONG_PASSWORD.getString());
            }
        }
    }

    /**
     * Tries to open the PDF document in a read/write mode without a password.
     * If the document is encrypted, returns {@code false}.
     *
     * @return {@code true} on success; {@code false} if encrypted
     */
    private boolean openDocumentReadWrite() throws IOException {
        return openDocumentReadWrite(new byte[0]);
    }

    /**
     * Tries to open the PDF document in a read/write mode with the provided
     * password. If the password is incorrect, returns {@code false}.
     *
     * @param password password to use, when decrypting
     *
     * @return {@code true} on success; {@code false} if invalid password
     */
    /*
     * "Either log or rethrow this exception." warning is ignored, as the whole
     * contract of the method is to return false on a "bad password" (i.e. this
     * is not an exceptional case here).
     */
    @SuppressWarnings("java:S1166")
    private boolean openDocumentReadWrite(byte[] password) throws IOException {
        try {
            final ReaderProperties readerProperties = new ReaderProperties().setPassword(password);
            final PdfReader reader = new PdfReader(
                    new ByteArrayInputStream(getOriginalContent()),
                    readerProperties
            );
            final ByteArrayOutputStream tempWriterOutputStream = new ByteArrayOutputStream();
            final PdfWriter writer = new PdfWriter(tempWriterOutputStream);
            document = new PdfDocument(reader, writer);
            writerOutputStream = tempWriterOutputStream;
            return true;
        } catch (BadPasswordException e) {
            return false;
        }
    }

    /**
     * Tries to open the PDF document in a read-only mode without a password.
     * If the document is encrypted, returns {@code false}.
     *
     * @return {@code true} on success; {@code false} if encrypted
     */
    private boolean openDocumentReadOnly() throws IOException {
        return openDocumentReadOnly(new byte[0]);
    }

    /**
     * Tries to open the PDF document in a read-only mode with the provided
     * password. If the password is incorrect, returns {@code false}.
     *
     * @param password password to use, when decrypting
     *
     * @return {@code true} on success; {@code false} if invalid password
     */
    /*
     * "Either log or rethrow this exception." warning is ignored, as the whole
     * contract of the method is to return false on a "bad password" (i.e. this
     * is not an exceptional case here).
     */
    @SuppressWarnings("java:S1166")
    private boolean openDocumentReadOnly(byte[] password) throws IOException {
        try {
            final ReaderProperties readerProperties = new ReaderProperties();
            readerProperties.setPassword(password);
            final PdfReader reader = new PdfReader(
                    new ByteArrayInputStream(getOriginalContent()),
                    readerProperties
            );
            document = new PdfDocument(reader);
            writerOutputStream = null;
            return true;
        } catch (BadPasswordException e) {
            return false;
        }
    }
}
