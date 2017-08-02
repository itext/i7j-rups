package com.itextpdf.rups.event;

public class NewIndirectObjectEvent extends RupsEvent {

    @Override
    public int getType() {
        return NEW_INDIRECT_OBJECT_EVENT;
    }

    @Override
    public Object getContent() {
        return null;
    }
}
