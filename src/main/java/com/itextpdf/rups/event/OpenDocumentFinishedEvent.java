package com.itextpdf.rups.event;



public class OpenDocumentFinishedEvent extends RupsEvent {
    @Override
    public int getType() {
        return OPEN_DOCUMENT_FINISHED_EVENT;
    }

    @Override
    public Object getContent() {
        return null;
    }
}
