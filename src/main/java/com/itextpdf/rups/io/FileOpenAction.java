package com.itextpdf.rups.io;

import com.itextpdf.rups.event.OpenFileEvent;
import com.itextpdf.rups.event.RupsEvent;

import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.util.Observer;

public class FileOpenAction extends FileChooserAction {
    /**
     * Creates a new file chooser action.
     *
     * @param observer the object waiting for you to select file
     * @param filter   a filter to apply when browsing
     * @param parent   a parent Component for chooser dialog
     */
    public FileOpenAction(Observer observer, FileFilter filter, Component parent) {
        super(observer, "Open", filter, parent);
    }

    @Override
    protected int showDialog() {
        return fileChooser.showOpenDialog(parent);
    }

    @Override
    protected RupsEvent getEvent() {
        return new OpenFileEvent(getFile());
    }
}
