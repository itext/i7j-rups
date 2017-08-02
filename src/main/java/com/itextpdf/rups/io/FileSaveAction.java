package com.itextpdf.rups.io;

import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.event.SaveToFileEvent;

import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.util.Observer;

public class FileSaveAction extends FileChooserAction {

    /**
     * Creates a new file chooser action.
     *
     * @param observer the object waiting for you to select file
     * @param filter   a filter to apply when browsing
     * @param parent   a parent Component for chooser dialog
     */
    public FileSaveAction(Observer observer, FileFilter filter, Component parent) {
        super(observer, "Save as...", filter, parent);
    }

    @Override
    protected int showDialog() {
        return fileChooser.showSaveDialog(parent);
    }

    @Override
    protected RupsEvent getEvent() {
        return new SaveToFileEvent(getFile());
    }
}
