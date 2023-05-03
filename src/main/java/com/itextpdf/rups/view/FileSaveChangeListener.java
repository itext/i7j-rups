package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsController;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class FileSaveChangeListener implements PropertyChangeListener {

    private RupsController controller;

    public FileSaveChangeListener(RupsController controller) {
        this.controller = controller;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ( "FILE_SAVED".equals(evt.getPropertyName())
                && evt.getNewValue() != null
                && evt.getNewValue() instanceof File ) {
            this.controller.saveCurrentFile((File) evt.getNewValue());
        }
    }
}
