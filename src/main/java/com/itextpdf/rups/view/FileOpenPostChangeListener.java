package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsController;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class FileOpenPostChangeListener implements PropertyChangeListener {

    private RupsController controller;

    public FileOpenPostChangeListener(RupsController controller) {
        this.controller = controller;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.controller.update(evt);
    }
}
