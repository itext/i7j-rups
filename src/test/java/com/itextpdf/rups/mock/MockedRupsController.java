package com.itextpdf.rups.mock;

import com.itextpdf.rups.controller.IRupsController;
import com.itextpdf.rups.model.PdfFile;

import java.awt.Component;
import java.io.File;

public class MockedRupsController implements IRupsController {

    private int openCount = 0;

    private PdfFile pdfFile;

    public MockedRupsController() {
    }

    public MockedRupsController(PdfFile pdfFile) {
        this.pdfFile = pdfFile;
    }

    @Override
    public Component getMasterComponent() {
        return null;
    }

    @Override
    public PdfFile getCurrentFile() {
        return this.pdfFile;
    }

    @Override
    public void openNewFile(File file) {
        this.openCount++;
    }

    @Override
    public void closeCurrentFile() {
        this.openCount--;
    }

    public int getOpenedCount() {
        return this.openCount;
    }
}
