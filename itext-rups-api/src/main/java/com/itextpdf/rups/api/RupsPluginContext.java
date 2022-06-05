package com.itextpdf.rups.api;

import javax.swing.JMenuBar;

public class RupsPluginContext {
    private JMenuBar menuBar;
    private IRupsController rupsController;
    private IPdfController pdfController;

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public RupsPluginContext setMenuBar(JMenuBar rupsMenuBar) {
        this.menuBar = rupsMenuBar;
        return this;
    }

    public IRupsController getRupsController() {
        return rupsController;
    }

    public RupsPluginContext setRupsController(IRupsController rupsController) {
        this.rupsController = rupsController;
        return this;
    }

    public IPdfController getPdfController() {
        return pdfController;
    }

    public RupsPluginContext setPdfController(IPdfController pdfController) {
        this.pdfController = pdfController;
        return this;
    }
}
