package com.itextpdf.rups.api;

public abstract class AbstractRupsPlugin implements IRupsPlugin {

    protected IPdfController pdfController;

    @Override
    public void setIPdfController(IPdfController pdfController) {
        this.pdfController = pdfController;
    }
}
