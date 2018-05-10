package com.itextpdf.rups.view.itext;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.model.IndirectObjectFactory;
import com.itextpdf.rups.model.TreeNodeFactory;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.XfaTreeNode;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;

@Category(UnitTest.class)
public class FormTreeTest extends ExtendedITextTest {

    private static final String sourceFolder = "./src/test/resources/com/itextpdf/rups/view/itext/";

    @Test
    public void testLoadXfa() throws IOException {
        File inPdf = new File(sourceFolder + "cmp_purchase_order_filled.pdf");

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(inPdf));

        IndirectObjectFactory indirectObjectFactory = new IndirectObjectFactory(pdfDocument);
        while (indirectObjectFactory.storeNextObject());
        TreeNodeFactory factory = new TreeNodeFactory(indirectObjectFactory);

        PdfReaderController controller = new PdfReaderController(null, null, true);
        FormTree formTree = new FormTree(controller);

        PdfObject xfa = pdfDocument.getCatalog().getPdfObject().getAsDictionary(PdfName.AcroForm).getAsArray(PdfName.XFA);
        PdfObjectTreeNode xfaObjTreeNode = PdfObjectTreeNode.getInstance(xfa);
        XfaTreeNode xfaTreeNode = new XfaTreeNode(xfaObjTreeNode);

        formTree.loadXfa(factory, xfaTreeNode, xfaObjTreeNode);
    }


}
