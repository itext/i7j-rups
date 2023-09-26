package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsController;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class FileOpenChangeListener implements PropertyChangeListener {

    private RupsController controller;

    public FileOpenChangeListener(RupsController controller) {
        this.controller = controller;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("FILE_OPEN".equals(evt.getPropertyName())
                && evt.getNewValue() != null
                && evt.getNewValue() instanceof File
        ) {
            this.controller.openNewFile((File)evt.getNewValue());
        }
    }
}
