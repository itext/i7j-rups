package com.itextpdf.rups.io;

import com.itextpdf.rups.controller.IRupsController;
import com.itextpdf.rups.model.PdfFile;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * An action used to open a PDF file in the system viewer.
 */
public class OpenInViewerAction implements ActionListener {

    private final IRupsController controller;

    private final ISystemViewerAction systemViewerAction;

    /**
     * Creates an OpenInViewerAction with the DefaultSystemViewerAction.
     *
     * @param controller RupsController
     */
    public OpenInViewerAction(IRupsController controller) {
        this(controller, new DefaultSystemViewerAction());
    }

    /**
     * Creates an OpenInViewerAction.
     *
     * @param controller         RupsController
     * @param systemViewerAction the action to open PDF files in the system viewer
     */
    public OpenInViewerAction(IRupsController controller, ISystemViewerAction systemViewerAction) {
        this.controller = controller;
        this.systemViewerAction = systemViewerAction;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        if (!this.systemViewerAction.isViewingSupported()) {
            return;
        }

        final PdfFile pdfFile = this.controller.getCurrentFile();

        if (pdfFile == null || pdfFile.getDirectory() == null) {
            return;
        }

        this.systemViewerAction.openFile(new File(pdfFile.getDirectory(), pdfFile.getFilename()));
    }
}
