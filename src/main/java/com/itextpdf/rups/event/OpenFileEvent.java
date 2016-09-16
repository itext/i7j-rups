package com.itextpdf.rups.event;


import java.io.File;

public class OpenFileEvent extends RupsEvent {

    private File file;

    public OpenFileEvent(File file) {
        this.file = file;
    }

    @Override
    public int getType() {
        return OPEN_FILE_EVENT;
    }

    @Override
    public Object getContent() {
        return file;
    }
}