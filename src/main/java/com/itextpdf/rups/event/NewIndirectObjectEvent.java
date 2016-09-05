package com.itextpdf.rups.event;

import java.io.File;

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
