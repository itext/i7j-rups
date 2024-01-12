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
package com.itextpdf.rups.view.itext;

import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.PostOpenDocumentEvent;
import com.itextpdf.rups.model.IProgressDialog;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.view.itext.treenodes.StructureTreeNode;
import com.itextpdf.test.ExtendedITextTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("IntegrationTest")
class StructureTreeTest extends ExtendedITextTest {


    private static final String sourceFolder = "./src/test/resources/com/itextpdf/rups/controller/";

    @Test
    void extractMcidContentInStructureTreeTest() throws IOException {
        File file = new File(sourceFolder + "hello_world_tagged.pdf");
        PdfFile pdfFile = PdfFile.open(file, Files.readAllBytes(file.toPath()));

        StructureTreeNode rootNode = getStructureTreeRootNode(pdfFile);
        StructureTreeNode mciChild = (StructureTreeNode) rootNode.getChildAt(0).getChildAt(0)
                .getChildAt(0).getChildAt(0);
        String nodeLabel = (String) mciChild.getUserObject();
        Assertions.assertEquals("0 [Hello ]", nodeLabel);
    }

    @Test
    void extractMcidContentInStructureTreeWithActualTextTest() throws IOException {
        File file = new File(sourceFolder + "hello_world_tagged_actualtext.pdf");
        PdfFile pdfFile = PdfFile.open(file, Files.readAllBytes(file.toPath()));

        StructureTreeNode rootNode = getStructureTreeRootNode(pdfFile);
        StructureTreeNode mciChild = (StructureTreeNode) rootNode.getChildAt(0).getChildAt(0)
                .getChildAt(0).getChildAt(0);
        String nodeLabel = (String) mciChild.getUserObject();
        Assertions.assertEquals("0 [Olleh ]", nodeLabel);
    }

    private static StructureTreeNode getStructureTreeRootNode(PdfFile pdfFile) {

        PdfReaderController controller = new PdfReaderController(null, null);
        ObjectLoader loader = new ObjectLoader(
                controller, pdfFile, "Test loader", new DummyProgressDialog()
        );
        // preload everything
        loader.doTask();

        // initialise the main PDF object tree view
        controller.update(controller, new PostOpenDocumentEvent(loader));

        // set up the structure tree pane and fake-load it
        StructureTree tree = new StructureTree(controller);
        tree.setLoader(loader);
        tree.setModel(tree.recalculateTreeModel());
        return (StructureTreeNode) tree.getModel().getRoot();
    }

    private static final class DummyProgressDialog implements IProgressDialog {

        @Override
        public void setMessage(String msg) {

        }

        @Override
        public void setValue(int value) {

        }

        @Override
        public void setTotal(int n) {

        }

        @Override
        public void showErrorDialog(Exception ex) {

        }

        @Override
        public void setVisible(boolean visible) {

        }
    }
}
