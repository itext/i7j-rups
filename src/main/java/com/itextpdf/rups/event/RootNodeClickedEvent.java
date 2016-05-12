package com.itextpdf.rups.event;

public class RootNodeClickedEvent extends RupsEvent {

    @Override
    public int getType() {
        return ROOT_NODE_CLICKED_EVENT;
    }

    @Override
    public Object getContent() {
        return null;
    }
}
