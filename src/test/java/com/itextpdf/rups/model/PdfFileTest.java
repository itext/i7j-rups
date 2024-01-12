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
package com.itextpdf.rups.model;

import com.itextpdf.kernel.exceptions.BadPasswordException;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
class PdfFileTest {
    private static final Path RESOURCES_DIR_PATH = Paths.get(
            "./src/test/resources/com/itextpdf/rups/model/pdfFile"
    );

    private static final String USER_PASSWORD = "password-user";
    private static final String OWNER_PASSWORD = "password-owner";

    @Test
    void open_RegularFileTest() throws IOException {
        PdfFile openedFile = openTestFile("regular.pdf", "");
        /*
         * Should be opened as owner (i.e. read/write), as it is not encrypted.
         */
        Assertions.assertTrue(openedFile.isOpenedAsOwner());
        Assertions.assertNotNull(openedFile.getByteArrayOutputStream());
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "REGULAR",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    @Test
    void open_OwnerProtectedFileWithoutPasswordTest() throws IOException {
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
    void open_OwnerProtectedFileWithIncorrectPasswordTest() throws IOException {
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
    void open_OwnerProtectedFileWithCorrectPasswordTest() throws IOException {
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
    void open_UserProtectedFileWithoutPasswordTest() {
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
    void open_UserProtectedFileWithIncorrectPasswordTest() {
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
    void open_UserProtectedFileWithUserPasswordTest() throws IOException {
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
    void open_UserProtectedFileWithOwnerPasswordTest() throws IOException {
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

    @Test
    void openAsOwner_RegularFileTest() throws IOException {
        PdfFile openedFile = openTestFileAsOwner("regular.pdf", "");
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "REGULAR",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    @Test
    void openAsOwner_OwnerProtectedFileWithoutPasswordTest() throws IOException {
        /*
         * Here it should throw a bad password error, as it won't get opened as
         * an owner without a password.
         */
        Assertions.assertThrows(
                BadPasswordException.class,
                () -> openTestFileAsOwner("protected_with_owner_password.pdf", null)
        );
    }

    @Test
    void openAsOwner_OwnerProtectedFileWithIncorrectPasswordTest() throws IOException {
        /*
         * Here it should throw a bad password error, as it won't get opened as
         * an owner with an incorrect password.
         */
        Assertions.assertThrows(
                BadPasswordException.class,
                () -> openTestFileAsOwner("protected_with_owner_password.pdf", "incorrect")
        );
    }

    @Test
    void openAsOwner_OwnerProtectedFileWithCorrectPasswordTest() throws IOException {
        PdfFile openedFile = openTestFileAsOwner("protected_with_owner_password.pdf", OWNER_PASSWORD);
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "ONLY OWNER PASSWORD",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    @Test
    void openAsOwner_UserProtectedFileWithoutPasswordTest() {
        /*
         * Here it should throw a bad password error, since we need at least a
         * correct user password to just open this file.
         */
        Assertions.assertThrows(
                BadPasswordException.class,
                () -> openTestFileAsOwner("protected_with_both_passwords.pdf", null)
        );
    }

    @Test
    void openAsOwner_UserProtectedFileWithIncorrectPasswordTest() {
        /*
         * Here it should throw a bad password error, since we need at least a
         * correct user password to just open this file.
         */
        Assertions.assertThrows(
                BadPasswordException.class,
                () -> openTestFileAsOwner("protected_with_both_passwords.pdf", "incorrect")
        );
    }

    @Test
    void openAsOwner_UserProtectedFileWithUserPasswordTest() {
        /*
         * Here it should throw a bad password error, as it won't get opened as
         * an owner without an owner password.
         */
        Assertions.assertThrows(
                BadPasswordException.class,
                () -> openTestFileAsOwner("protected_with_both_passwords.pdf", USER_PASSWORD)
        );
    }

    @Test
    void openAsOwner_UserProtectedFileWithOwnerPasswordTest() throws IOException {
        PdfFile openedFile = openTestFileAsOwner("protected_with_both_passwords.pdf", OWNER_PASSWORD);
        // Sanity check, that it opened the document properly
        Assertions.assertEquals(
                "DIFFERENT USER AND OWNER PASSWORDS",
                PdfTextExtractor.getTextFromPage(openedFile.getPdfDocument().getFirstPage())
        );
    }

    private PdfFile openTestFile(String fileName, String password) throws IOException {
        final File testFile = RESOURCES_DIR_PATH.resolve(fileName).toFile();
        final PdfFile openedFile = PdfFile.open(testFile, new StaticPasswordProvider(testFile, password));

        // These assertions should be valid on any properly opened file
        Assertions.assertEquals(testFile, openedFile.getOriginalFile());
        Assertions.assertArrayEquals(readTestFileBytes(fileName), openedFile.getOriginalContent());
        Assertions.assertNotNull(openedFile.getPdfDocument());
        return openedFile;
    }

    private PdfFile openTestFileAsOwner(String fileName, String password) throws IOException {
        final File testFile = RESOURCES_DIR_PATH.resolve(fileName).toFile();
        final PdfFile openedFile = PdfFile.openAsOwner(
                testFile, new StaticPasswordProvider(testFile, password)
        );

        // These assertions should be valid on any properly opened file
        Assertions.assertEquals(testFile, openedFile.getOriginalFile());
        Assertions.assertArrayEquals(readTestFileBytes(fileName), openedFile.getOriginalContent());
        Assertions.assertNotNull(openedFile.getPdfDocument());
        // Should be opened as owner, as we are forcing it here
        Assertions.assertTrue(openedFile.isOpenedAsOwner());
        Assertions.assertNotNull(openedFile.getByteArrayOutputStream());
        return openedFile;
    }

    private byte[] readTestFileBytes(String filename) throws IOException {
        return Files.readAllBytes(RESOURCES_DIR_PATH.resolve(filename));
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
