package com.itextpdf.rups.event;

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

public class NodeDeleteArrayChildEvent extends RupsEvent {

    Content content;

    public NodeDeleteArrayChildEvent(int index, PdfObjectTreeNode parent) {
        content = new Content(index, parent);
    }

    @Override
    public int getType() {
        return NODE_DELETE_ARRAY_CHILD_EVENT;
    }

    @Override
    public Object getContent() {
        return content;
    }

    public class Content {
        public int index;
        public PdfObjectTreeNode parent;

        public Content(int index, PdfObjectTreeNode parent) {
            this.index = index;
            this.parent = parent;
        }
    }
}
