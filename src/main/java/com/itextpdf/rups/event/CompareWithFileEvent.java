package com.itextpdf.rups.event;

import java.io.File;

public class CompareWithFileEvent extends RupsEvent {

    private File file;

    public CompareWithFileEvent(File file) {
        this.file = file;
    }

    @Override
    public int getType() {
        return COMPARE_WITH_FILE_EVENT;
    }

    @Override
    public Object getContent() {
        return file;
    }
}
