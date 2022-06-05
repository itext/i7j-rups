package com.itextpdf.rups.api;

public interface IRupsPlugin {

    boolean initialize(RupsPluginContext rupsPluginContext);

    void setIPdfController(IPdfController pdfController);
}
