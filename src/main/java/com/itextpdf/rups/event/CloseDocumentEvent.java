package com.itextpdf.rups.event;

public class CloseDocumentEvent extends RupsEvent {
    @Override
    public int getType() {
        return CLOSE_DOCUMENT_EVENT;
    }

    @Override
    public Object getContent() {
        return null;
    }
}
