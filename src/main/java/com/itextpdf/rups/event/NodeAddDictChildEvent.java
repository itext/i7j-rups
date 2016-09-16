package com.itextpdf.rups.event;

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import javax.swing.text.AbstractDocument;

public class NodeAddDictChildEvent extends RupsEvent {

    Content content;

    public NodeAddDictChildEvent(PdfName key, PdfObject value, PdfObjectTreeNode parent, int index) {
        content = new Content(key, value, parent, index);
    }

    @Override
    public int getType() {
        return NODE_ADD_DICT_CHILD_EVENT;
    }

    @Override
    public Object getContent() {
        return content;
    }

    public class Content {
        public PdfName key;
        public PdfObject value;
        public PdfObjectTreeNode parent;
        public int index;

        public Content(PdfName key, PdfObject value, PdfObjectTreeNode parent, int index) {
            this.key = key;
            this.value = value;
            this.parent = parent;
            this.index = index;
        }
    }
}
