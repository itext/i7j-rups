package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsInstanceController;
import com.itextpdf.rups.model.PdfFile;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * A subclass of JPanel to allow for a tie between a tab ({@link com.itextpdf.rups.view.RupsTabbedPane RupsTabbedPane})
 * and its related {@link com.itextpdf.rups.controller.RupsInstanceController RupsInstanceController}.
 */
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
