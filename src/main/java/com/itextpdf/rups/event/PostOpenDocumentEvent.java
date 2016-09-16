package com.itextpdf.rups.event;

import com.itextpdf.rups.model.ObjectLoader;

public class PostOpenDocumentEvent extends RupsEvent {

    private ObjectLoader loader;

    public PostOpenDocumentEvent(ObjectLoader loader) {
        this.loader = loader;
    }

    @Override
    public int getType() {
        return OPEN_DOCUMENT_POST_EVENT;
    }

    @Override
    public Object getContent() {
        return loader;
    }
}