package com.itextpdf.rups.event;

import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

public class TreeNodeClickedEvent extends RupsEvent {

    PdfObjectTreeNode node;

    public TreeNodeClickedEvent(PdfObjectTreeNode node) {
        this.node = node;
    }

    @Override
    public int getType() {
        return TREE_NODE_CLICKED_EVENT;
    }

    @Override
    public Object getContent() {
        return node;
    }
}
