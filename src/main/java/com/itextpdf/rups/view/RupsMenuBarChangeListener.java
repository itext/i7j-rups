package com.itextpdf.rups.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RupsMenuBarChangeListener implements PropertyChangeListener {

    private RupsMenuBar menuBar;

    public RupsMenuBarChangeListener(RupsMenuBar menuBar) {
        this.menuBar = menuBar;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ( "FILE_LOADED".equals(evt.getPropertyName())) {
            this.menuBar.enableItems(true);
        } else {
            this.menuBar.enableItems(false);
        }
    }
}
