package com.itextpdf.rups.io;

import com.itextpdf.rups.event.CompareWithFileEvent;
import com.itextpdf.rups.event.RupsEvent;

import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.util.Observer;

public class FileCompareAction extends FileChooserAction {


    /**
     * Creates a new file chooser action.
     *
     * @param observer the object waiting for you to select file
     * @param filter   a filter to apply when browsing
     * @param parent   a parent Component for chooser dialog
     */
    public FileCompareAction(Observer observer, FileFilter filter, Component parent) {
        super(observer, "Compare with...", filter, parent);
    }

    @Override
    protected int showDialog() {
        return fileChooser.showDialog(parent, "Compare with");
    }

    @Override
    protected RupsEvent getEvent() {
        return new CompareWithFileEvent(getFile());
    }
}
