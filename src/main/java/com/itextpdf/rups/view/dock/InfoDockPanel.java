package com.itextpdf.rups.view.dock;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.rups.model.PdfFile;

import javax.swing.*;
import java.awt.*;

public class InfoDockPanel extends JPanel {

    public InfoDockPanel(PdfFile pdfFile) {
        Box box = Box.createVerticalBox();
        add(box);

        PdfDocument pdfDocument = pdfFile.getPdfDocument();
        PdfDocumentInfo documentInfo = pdfDocument.getDocumentInfo();

        box.add(new JLabel(pdfFile.getFilename()));
        box.add(new JLabel(documentInfo.getProducer()));
        box.add(new JLabel(documentInfo.getAuthor()));
        box.add(new JLabel(documentInfo.getTitle()));
    }
}
