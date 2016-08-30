package com.itextpdf.rups.event;

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

public class NodeDeleteDictChildEvent extends RupsEvent {

    Content content;

    public NodeDeleteDictChildEvent(PdfName key, PdfObjectTreeNode parent) {
        content = new Content(key, parent);
    }

    @Override
    public int getType() {
        return NODE_DELETE_DICT_CHILD_EVENT;
    }

    @Override
    public Object getContent() {
        return content;
    }

    public class Content {
        public PdfName key;
        public PdfObjectTreeNode parent;

        public Content(PdfName key, PdfObjectTreeNode parent) {
            this.key = key;
            this.parent = parent;
        }
    }
}
