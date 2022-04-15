package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsInstanceController;
import com.itextpdf.rups.model.PdfFile;

import javax.swing.*;
import java.awt.*;

public class RupsPanel extends JPanel {

    private RupsInstanceController rupsInstanceController;

    public RupsPanel() {
        setLayout(new BorderLayout());
    }

    public void setRupsInstanceController(RupsInstanceController rupsInstanceController) {
        this.rupsInstanceController = rupsInstanceController;
    }

    public PdfFile getPdfFile() {
        return this.rupsInstanceController.getPdfFile();
    }

    public RupsInstanceController getRupsInstanceController() {
        return this.rupsInstanceController;
    }
}
