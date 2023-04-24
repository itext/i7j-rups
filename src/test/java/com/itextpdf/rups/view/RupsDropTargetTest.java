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

import com.itextpdf.rups.mock.MockedDropTargetDropEvent;
import com.itextpdf.rups.mock.MockedRupsController;
import com.itextpdf.rups.mock.MockedTransferable;
import com.itextpdf.test.ITextTest;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class RupsDropTargetTest extends ITextTest {

    private RupsDropTarget dropTarget;
    private List<File> fileList;
    private MockedRupsController rupsController;

    @BeforeEach
    public void beforeTest() {
        this.rupsController = new MockedRupsController();
        this.dropTarget = new RupsDropTarget(rupsController);
        this.fileList = Arrays.asList(new File("a.pdf"), new File("b.pdf"));
    }

    @Test
    public void nullTest() {
        this.dropTarget.drop(null);
        Assertions.assertEquals(0, this.rupsController.getOpenedCount());
    }

    @Test
    public void noDataFlavorTest() {
        Transferable transferable = new MockedTransferable(null);
        List<File> files = this.dropTarget.extractFilesFromTransferable(transferable);
        Assertions.assertNotNull(files);
        Assertions.assertTrue(files.isEmpty());
    }

    @Test
    public void noSupportedDataFlavorTest() {
        Transferable transferable = new MockedTransferable(DataFlavor.imageFlavor);
        List<File> files = this.dropTarget.extractFilesFromTransferable(transferable);
        Assertions.assertNotNull(files);
        Assertions.assertTrue(files.isEmpty());
    }

    @Test
    public void javaFileListFlavorTest() {
        Transferable transferable = new MockedTransferable(DataFlavor.javaFileListFlavor, fileList);
        List<File> files = this.dropTarget.extractFilesFromTransferable(transferable);
        Assertions.assertNotNull(files);
        Assertions.assertEquals(this.fileList.size(), files.size());
    }

    @Test
    public void stringFlavorTest() throws MalformedURLException {
        StringBuilder builder = new StringBuilder();

        for ( File file : this.fileList ) {
            builder.append(file.toURL());
            builder.append(" ");
        }

        Transferable transferable = new MockedTransferable(DataFlavor.stringFlavor, builder.toString());
        List<File> files = this.dropTarget.extractFilesFromTransferable(transferable);
        Assertions.assertNotNull(files);
        Assertions.assertEquals(this.fileList.size(), files.size());
    }

    @Test
    public void test() {
        MockedTransferable mockedTransferable = new MockedTransferable(DataFlavor.javaFileListFlavor, fileList);
        DropTarget dropTarget = new DropTarget();
        this.dropTarget.drop(new MockedDropTargetDropEvent(mockedTransferable, dropTarget));
        Assertions.assertEquals(fileList.size(), this.rupsController.getOpenedCount());
    }
}
