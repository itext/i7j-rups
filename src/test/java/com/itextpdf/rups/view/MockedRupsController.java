package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsController;

import java.awt.Dimension;
import java.awt.Frame;

public class MockedRupsController extends RupsController {

    private MockedRupsController() {
        super(null, null, false);
    }

    /**
     * Constructs the GUI components of the RUPS application.
     *
     * @param dimension  the dimension
     * @param frame      the frame
     * @param pluginMode the plugin mode
     */
    public MockedRupsController(Dimension dimension, Frame frame, boolean pluginMode) {
        super(dimension, frame, pluginMode);
    }
}
