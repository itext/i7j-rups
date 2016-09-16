package com.itextpdf.rups.event;

import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfObject;

public class PostNewIndirectObjectEvent extends RupsEvent {

    private PdfObject object;

    public PostNewIndirectObjectEvent(PdfObject object) {
        this.object = object;
    }

    @Override
    public int getType() {
        return POST_NEW_INDIRECT_OBJECT_EVENT;
    }

    @Override
    public Object getContent() {
        return object;
    }
}
