package com.itextpdf.rups.controller;

import com.itextpdf.rups.model.PdfFile;

import java.awt.Component;
import java.io.File;

/**
 * The controller in charge of the application. To view the specific tab controllers, look at the RupsInstanceController
 * class.
 */
public interface IRupsController {

    /**
     * Returns the main component of RUPS.
     *
     * @return Component, typically a JTabbedPane
     */
    Component getMasterComponent();

    /**
     * Returns the currently loaded File.
     *
     * @return PdfFile
     */
    PdfFile getCurrentFile();

    /**
     * Opens a new File in RUPS.
     *
     * @param file the file to be opened.
     */
    void openNewFile(File file);

    /**
     * Closes the currently opened file.
     */
    void closeCurrentFile();
}
