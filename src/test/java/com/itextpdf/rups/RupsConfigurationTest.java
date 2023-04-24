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
package com.itextpdf.rups;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import javax.swing.WindowConstants;

@Tag("UnitTest")
public class RupsConfigurationTest {

    private static Properties copy;

    @BeforeAll
    public static void beforeClass() throws BackingStoreException {
        copy = RupsConfiguration.INSTANCE.getCurrentState();
        RupsConfiguration.INSTANCE.resetToDefaultProperties();
    }

    @Test
    public void setHomeFolderTest() {
        String path = "src/test";
        RupsConfiguration.INSTANCE.setHomeFolder(path);
        RupsConfiguration.INSTANCE.saveConfiguration();
        File homeFolder = RupsConfiguration.INSTANCE.getHomeFolder();
        File expected = new File(path);
        Assertions.assertEquals(expected, homeFolder);
    }

    @Test
    public void setHomeFolderToNullTest() {
        RupsConfiguration.INSTANCE.setHomeFolder(null);
        RupsConfiguration.INSTANCE.saveConfiguration();
        File homeFolder = RupsConfiguration.INSTANCE.getHomeFolder();
        File expected = new File(System.getProperty("user.home"));
        Assertions.assertEquals(expected, homeFolder);
    }

    @Test
    public void setHomeFolderToFileTest() {
        String path = "src/test/resources/com/itextpdf/rups/controller/hello_world.pdf";
        RupsConfiguration.INSTANCE.setHomeFolder(path);
        RupsConfiguration.INSTANCE.saveConfiguration();
        File homeFolder = RupsConfiguration.INSTANCE.getHomeFolder();
        File expected = new File(System.getProperty("user.home"));
        Assertions.assertEquals(expected, homeFolder);
    }

    @Test
    public void setHomeFolderToNonExistentDirectoryTest() {
        String path = "does/not/exist/";
        RupsConfiguration.INSTANCE.setHomeFolder(path);
        RupsConfiguration.INSTANCE.saveConfiguration();
        File homeFolder = RupsConfiguration.INSTANCE.getHomeFolder();
        File expected = new File(System.getProperty("user.home"));
        Assertions.assertEquals(expected, homeFolder);
    }

    @Test
    public void setDuplicateFilesTrueTest() {
        RupsConfiguration.INSTANCE.setOpenDuplicateFiles(true);
        RupsConfiguration.INSTANCE.saveConfiguration();
        Assertions.assertTrue(RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
    }

    @Test
    public void setDuplicateFilesFalseTest() {
        RupsConfiguration.INSTANCE.setOpenDuplicateFiles(false);
        RupsConfiguration.INSTANCE.saveConfiguration();
        Assertions.assertFalse(RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
    }

    @Test
    public void setLookAndFeel() {
        String laf = "crossplatform";
        RupsConfiguration.INSTANCE.setLookAndFeel(laf);
        RupsConfiguration.INSTANCE.saveConfiguration();
        String lookAndFeel = RupsConfiguration.INSTANCE.getLookAndFeel();
        Assertions.assertTrue(lookAndFeel.contains("javax.swing.plaf"));
    }

    @Test
    public void setSystemLookAndFeel() {
        String laf = "system";
        RupsConfiguration.INSTANCE.setLookAndFeel(laf);
        RupsConfiguration.INSTANCE.saveConfiguration();
        String lookAndFeel = RupsConfiguration.INSTANCE.getLookAndFeel();
        Assertions.assertTrue(lookAndFeel.contains("javax.swing.plaf") || lookAndFeel.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"));
    }

    @Test
    public void clearUnsavedChangesTest() {
        RupsConfiguration.INSTANCE.setLookAndFeel("system");
        Assertions.assertTrue(RupsConfiguration.INSTANCE.hasUnsavedChanges());
        RupsConfiguration.INSTANCE.cancelTemporaryChanges();
        Assertions.assertFalse(RupsConfiguration.INSTANCE.hasUnsavedChanges());
    }

    @Test
    public void closingOperationsPossibleValuesTest() {
        int closeOperation = RupsConfiguration.INSTANCE.getCloseOperation();
        Assertions.assertTrue(
                closeOperation == WindowConstants.DO_NOTHING_ON_CLOSE ||
                        closeOperation == WindowConstants.HIDE_ON_CLOSE ||
                        closeOperation == WindowConstants.EXIT_ON_CLOSE ||
                        closeOperation == WindowConstants.DISPOSE_ON_CLOSE
                );
    }

    @AfterAll
    public static void afterClass() {
        RupsConfiguration.INSTANCE.restore(copy);
    }

}
