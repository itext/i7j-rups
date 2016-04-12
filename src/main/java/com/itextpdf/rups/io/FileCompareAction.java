package com.itextpdf.rups.io;

import java.util.Observable;
import javax.swing.filechooser.FileFilter;

public class FileCompareAction extends FileChooserAction{

    /**
     * Creates a new file chooser action.
     *
     * @param observable the object waiting for you to select file
     * @param caption    a description for the action
     * @param filter     a filter to apply when browsing
     * @param newFile    indicates if you should browse for a new or existing file
     */
    public FileCompareAction(Observable observable, String caption, FileFilter filter, boolean newFile) {
        super(observable, caption, filter, newFile);
    }
}
