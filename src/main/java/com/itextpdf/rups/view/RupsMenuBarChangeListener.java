package com.itextpdf.rups.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Class responsible for manipulating a {@link com.itextpdf.rups.view.RupsMenuBar RupsMenuBar} instance.
 * It listens to fired events, such as "FILE_LOADED", etc.
 */
public class RupsMenuBarChangeListener implements PropertyChangeListener {

    private final String FILE_LOADED_EVENT = "FILE_LOADED";

    private RupsMenuBar menuBar;

    public RupsMenuBarChangeListener(RupsMenuBar menuBar) {
        this.menuBar = menuBar;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ( this.FILE_LOADED_EVENT.equals(evt.getPropertyName())) {
            this.menuBar.enableItems(true);
        } else {
            this.menuBar.enableItems(false);
        }
    }
}
