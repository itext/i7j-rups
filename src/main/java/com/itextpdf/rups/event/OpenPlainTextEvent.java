package com.itextpdf.rups.event;

public class OpenPlainTextEvent extends RupsEvent {
    @Override
    public int getType() {
        return OPEN_PLAIN_TEXT_EVENT;
    }

    @Override
    public Object getContent() {
        return null;
    }
}
