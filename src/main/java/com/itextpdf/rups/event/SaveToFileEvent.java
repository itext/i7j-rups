package com.itextpdf.rups.event;

import java.io.File;

public class SaveToFileEvent extends RupsEvent {

    private File file;

    public SaveToFileEvent(File file) {
        this.file = file;
    }

    @Override
    public int getType() {
        return SAVE_TO_FILE_EVENT;
    }

    @Override
    public Object getContent() {
        return file;
    }
}