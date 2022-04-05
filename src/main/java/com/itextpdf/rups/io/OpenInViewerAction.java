package com.itextpdf.rups.io;

import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.view.RupsMenuBar;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class OpenInViewerAction implements ActionListener {

    private final RupsController controller;

    public OpenInViewerAction(RupsController controller) {
        this.controller = controller;
    }

    public void actionPerformed(ActionEvent e) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            PdfFile pdfFile = this.controller.getCurrentFile();
            if (pdfFile == null || pdfFile.getDirectory() == null) {
                return;
            }
            File myFile = new File(pdfFile.getDirectory(), pdfFile.getFilename());
            Desktop.getDesktop().open(myFile);
        } catch (IOException ex) {
            // no application registered for PDFs
        }
    }
}
