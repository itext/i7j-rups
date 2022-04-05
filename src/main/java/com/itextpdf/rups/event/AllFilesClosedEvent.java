package com.itextpdf.rups.event;

public class AllFilesClosedEvent extends RupsEvent {
    @Override
    public int getType() {
        return RupsEvent.ALL_FILES_CLOSED;
    }

    @Override
    public Object getContent() {
        return null;
    }
}
