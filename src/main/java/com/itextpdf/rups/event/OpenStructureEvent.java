package com.itextpdf.rups.event;

public class OpenStructureEvent extends RupsEvent {
    @Override
    public int getType() {
        return OPEN_STRUCTURE_EVENT;
    }

    @Override
    public Object getContent() {
        return null;
    }
}