package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsController;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class FileCloseChangeListener implements PropertyChangeListener {

    private RupsController controller;

    public FileCloseChangeListener(RupsController controller) {
        this.controller = controller;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("FILE_CLOSE".equals(evt.getPropertyName())) {
            this.controller.closeCurrentFile();
        }
    }
}
