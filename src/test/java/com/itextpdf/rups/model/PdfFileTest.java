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
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
class PdfFileTest {
    private static final File RESOURCES_DIR = new File("./src/test/resources/com/itextpdf/rups/model/pdfFile");

    private static final String USER_PASSWORD = "password-user";
    private static final String OWNER_PASSWORD = "password-owner";

    @Test
    void openRegularFileTest() throws IOException {
        PdfFile openedFile = openTestFile("regular.pdf", "");
        /*
         * Should be opened as owner (i.e. read/write), as it is not encrypted.
         */
        Assertions.assertTrue(openedFile.isOpenedAsOwner());
        Assertions.assertNotNull(openedFile.getByteArrayOutputStream());
        Assertions.assertNotNull(openedFile.getPdfDocument());
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "REGULAR",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    @Test
    void openOwnerProtectedFileWithoutPasswordTest() throws IOException {
        PdfFile openedFile = openTestFile("protected_with_owner_password.pdf", null);
        /*
         * Should be opened as user (i.e. read-only), as it is protected, and
         * we did not provide any password.
         */
        Assertions.assertFalse(openedFile.isOpenedAsOwner());
        Assertions.assertNull(openedFile.getByteArrayOutputStream());
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "ONLY OWNER PASSWORD",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    @Test
    void openOwnerProtectedFileWithIncorrectPasswordTest() throws IOException {
        PdfFile openedFile = openTestFile("protected_with_owner_password.pdf", "incorrect");
        /*
         * Should be opened as user (i.e. read-only), as it is protected, and
         * we did not provide a correct password.
         */
        Assertions.assertFalse(openedFile.isOpenedAsOwner());
        Assertions.assertNull(openedFile.getByteArrayOutputStream());
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "ONLY OWNER PASSWORD",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    @Test
    void openOwnerProtectedFileWithCorrectPasswordTest() throws IOException {
        PdfFile openedFile = openTestFile("protected_with_owner_password.pdf", OWNER_PASSWORD);
        /*
         * Should still be opened as user (i.e. read-only), as it is protected,
         * and the open method does not request a password, if the file can be
         * opened in a read-only mode.
         */
        Assertions.assertFalse(openedFile.isOpenedAsOwner());
        Assertions.assertNull(openedFile.getByteArrayOutputStream());
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "ONLY OWNER PASSWORD",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    @Test
    void openUserProtectedFileWithoutPasswordTest() {
        /*
         * Here it should throw a bad password error, since we need at least a
         * correct user password to open this file.
         */
        Assertions.assertThrows(
                BadPasswordException.class,
                () -> openTestFile("protected_with_both_passwords.pdf", null)
        );
    }

    @Test
    void openUserProtectedFileWithIncorrectPasswordTest() {
        /*
         * Here it should throw a bad password error, since we need at least a
         * correct user password to open this file.
         */
        Assertions.assertThrows(
                BadPasswordException.class,
                () -> openTestFile("protected_with_both_passwords.pdf", "incorrect")
        );
    }

    @Test
    void openUserProtectedFileWithUserPasswordTest() throws IOException {
        PdfFile openedFile = openTestFile("protected_with_both_passwords.pdf", USER_PASSWORD);
        /*
         * Should be opened as user (i.e. read-only), as it is protected, and
         * we only provided the user password.
         */
        Assertions.assertFalse(openedFile.isOpenedAsOwner());
        Assertions.assertNull(openedFile.getByteArrayOutputStream());
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "DIFFERENT USER AND OWNER PASSWORDS",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    @Test
    void openUserProtectedFileWithOwnerPasswordTest() throws IOException {
        PdfFile openedFile = openTestFile("protected_with_both_passwords.pdf", OWNER_PASSWORD);
        /*
         * Should be opened as owner (i.e. read/write), as it is protected, and
         * we provided the owner password.
         */
        Assertions.assertTrue(openedFile.isOpenedAsOwner());
        Assertions.assertNotNull(openedFile.getByteArrayOutputStream());
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "DIFFERENT USER AND OWNER PASSWORDS",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    private PdfFile openTestFile(String fileName, String password) throws IOException {
        File testFile = new File(RESOURCES_DIR, fileName);
        byte[] testFileBytes = Files.readAllBytes(testFile.toPath());
        PdfFile openedFile = PdfFile.open(testFile, testFileBytes, new StaticPasswordProvider(testFile, password));

        // These assertions should be valid on any properly opened file
        Assertions.assertEquals(testFile, openedFile.getOriginalFile());
        Assertions.assertArrayEquals(testFileBytes, openedFile.getOriginalContent());
        Assertions.assertNotNull(openedFile.getPdfDocument());
        return openedFile;
    }

    private static class StaticPasswordProvider implements IPasswordProvider {
        private final File expectedFile;
        private final byte[] password;

        public StaticPasswordProvider(File expectedFile, String password) {
            this.expectedFile = expectedFile;
            if (password != null) {
                this.password = password.getBytes(StandardCharsets.UTF_8);
            } else {
                this.password = null;
            }
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public byte[] get(File originalFile) {
            Assertions.assertEquals(expectedFile, originalFile);
            return password;
        }
    }
}
