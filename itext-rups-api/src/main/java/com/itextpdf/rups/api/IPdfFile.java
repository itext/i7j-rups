package com.itextpdf.rups.api;

import com.itextpdf.kernel.pdf.PdfDocument;

import java.io.File;

public interface IPdfFile {

    /**
     * Getter for iText's PdfDocument object.
     *
     * @return a PdfDocument object
     */
    PdfDocument getPdfDocument();

    /**
     * Getter for the filename
     *
     * @return the original filename
     * @since 5.0.3
     */
    String getFileName();

    /**
     * Returns the directory of the PdfFile instance.
     *
     * @return the directory of the opened file
     */
    File getDirectory();
}
