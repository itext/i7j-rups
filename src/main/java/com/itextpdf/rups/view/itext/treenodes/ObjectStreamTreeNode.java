package com.itextpdf.rups.view.itext.treenodes;

import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.view.Language;

public class ObjectStreamTreeNode extends PdfObjectTreeNode {

    public ObjectStreamTreeNode(PdfObject object) {
        super(object);
        setUserObject(Language.PDF_OBJECT_STREAMS_TREE_NODE.getString());
    }

}
