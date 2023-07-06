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
package com.itextpdf.rups.view;

import com.itextpdf.rups.RupsConfiguration;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.JTabbedPane;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class RupsTabbedPaneTest {

    private static final String EXPECTED_TAB_TITLE = "hello_world.pdf";
    private static final String EXPECTED_TAB_TITLE_2 = "hello_world_copy.pdf";
    private static final String INPUT_1 = "src/test/resources/com/itextpdf/rups/controller/hello_world.pdf";
    private static final String INPUT_2 = "src/test/resources/com/itextpdf/rups/controller/hello_world_copy.pdf";

    private RupsTabbedPane tabbedPane;
    private Dimension dimension;

    @BeforeEach
    public void before() {
        this.tabbedPane = new RupsTabbedPane();
        this.dimension = Toolkit.getDefaultToolkit().getScreenSize();
    }

    @Test
    public void initializationTest() {
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assertions.assertEquals(1, jTabbedPane.getTabCount());
        Assertions.assertEquals(Language.DEFAULT_TAB_TITLE.getString(), jTabbedPane.getTitleAt(0));
    }

    @Test
    public void openNullFileTest() {
        this.tabbedPane.openNewFile(null, this.dimension);
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assertions.assertEquals(1, jTabbedPane.getTabCount());
        Assertions.assertEquals(Language.DEFAULT_TAB_TITLE.getString(), jTabbedPane.getTitleAt(0));
    }

    @Test
    public void openNewFileTest() {
        File file = new File(INPUT_1);
        this.tabbedPane.openNewFile(file, this.dimension);
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assertions.assertEquals(1, jTabbedPane.getTabCount());
        Assertions.assertEquals(EXPECTED_TAB_TITLE, jTabbedPane.getTitleAt(0));
    }

    @Test
    public void openTwoFiles() {
        File file = new File(INPUT_1);
        this.tabbedPane.openNewFile(file, this.dimension);
        File file2 = new File(INPUT_2);
        this.tabbedPane.openNewFile(file2, this.dimension);
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assertions.assertEquals(2, jTabbedPane.getTabCount());
        Assertions.assertEquals(EXPECTED_TAB_TITLE, jTabbedPane.getTitleAt(0));
        Assertions.assertEquals(EXPECTED_TAB_TITLE_2, jTabbedPane.getTitleAt(1));
    }

    @Test
    public void openTwoFilesAndCloseAllTest() {
        File file = new File(INPUT_1);
        this.tabbedPane.openNewFile(file, this.dimension);
        File file2 = new File(INPUT_2);
        this.tabbedPane.openNewFile(file2, this.dimension);
        this.tabbedPane.closeCurrentFile();
        this.tabbedPane.closeCurrentFile();
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assertions.assertEquals(1, jTabbedPane.getTabCount());
        Assertions.assertEquals(Language.DEFAULT_TAB_TITLE.getString(), jTabbedPane.getTitleAt(0));
    }

    @Test
    public void isFileDuplicateTest() {
        boolean originalValue = RupsConfiguration.INSTANCE.canOpenDuplicateFiles();
        RupsConfiguration.INSTANCE.setOpenDuplicateFiles(false);
        RupsConfiguration.INSTANCE.saveConfiguration();

        File file = new File(INPUT_1);
        this.tabbedPane.openNewFile(file, this.dimension);
        boolean isAlreadyOpened = this.tabbedPane.isFileAlreadyOpen(file);

        RupsConfiguration.INSTANCE.setOpenDuplicateFiles(originalValue);
        RupsConfiguration.INSTANCE.saveConfiguration();

        Assertions.assertTrue(isAlreadyOpened);
    }
}
