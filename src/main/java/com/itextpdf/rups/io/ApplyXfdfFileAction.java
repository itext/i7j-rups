package com.itextpdf.rups.io;

import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.view.XfdfMergeDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ApplyXfdfFileAction implements ActionListener {
    private RupsController controller;

    public ApplyXfdfFileAction(RupsController controller) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PdfFile pdfFile = this.controller.getPdfFile();

        if ( pdfFile == null || pdfFile.getPdfDocument() == null ) {
            // TODO show error message
            return;
        }

        // check for unsaved changes

        // open filechooser
        XfdfMergeDialog xfdfMergeDialog = new XfdfMergeDialog(this.controller);
        xfdfMergeDialog.show();
    }
}
