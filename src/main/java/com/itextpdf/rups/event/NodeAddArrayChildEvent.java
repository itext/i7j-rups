package com.itextpdf.rups.event;

import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

public class NodeAddArrayChildEvent extends RupsEvent {

    Content content;

    public NodeAddArrayChildEvent(PdfObject value, PdfObjectTreeNode parent, int index) {
        content = new Content(value, parent, index);
    }

    @Override
    public int getType() {
        return NODE_ADD_ARRAY_CHILD_EVENT;
    }

    @Override
    public Object getContent() {
        return content;
    }

    public class Content {
        public PdfObject value;
        public PdfObjectTreeNode parent;
        public int index;

        public Content(PdfObject value, PdfObjectTreeNode parent, int index) {
            this.value = value;
            this.parent = parent;
            this.index = index;
        }
    }
}
