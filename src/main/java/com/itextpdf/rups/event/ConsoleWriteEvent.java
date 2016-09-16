package com.itextpdf.rups.event;

public class ConsoleWriteEvent extends RupsEvent {
    @Override
    public int getType() {
        return CONSOLE_WRITE_EVENT;
    }

    @Override
    public Object getContent() {
        return null;
    }
}
