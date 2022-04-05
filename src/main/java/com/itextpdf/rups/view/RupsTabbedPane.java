package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsInstanceController;
import com.itextpdf.rups.model.PdfFile;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class RupsTabbedPane extends JTabbedPane {

    private Dimension dimension;
    private JPanel defaultTab;
    private static final String DEFAULT_TAB_TEXT = "Drag and drop to open new file(s).";
    private static final String DEFAULT_TAB_TITLE = "New File";

    public RupsTabbedPane(Dimension dimension) {
        setDropTarget(new RupsDropTarget(this));
        defaultTab = new JPanel();
        defaultTab.add(new JLabel(DEFAULT_TAB_TEXT));
        super.addTab(DEFAULT_TAB_TITLE, defaultTab);

        this.dimension = dimension;
    }

    public void openNewFile(File file, boolean readonly) {
        if ( this.defaultTab.equals(getSelectedComponent())) {
            removeTabAt(getSelectedIndex());
        }

        RupsPanel rupsPanel = new RupsPanel();
        RupsInstanceController rupsInstanceController = new RupsInstanceController(this.dimension, rupsPanel, false);
        rupsPanel.setRupsInstanceController(rupsInstanceController);
        rupsInstanceController.loadFile(file, readonly);
        this.addTab(file.getName(), null, rupsPanel);
        this.setSelectedComponent(rupsPanel);
    }

    public void closeCurrentFile() {
        this.removeTabAt(this.getSelectedIndex());

        if ( getTabCount() == 0 ) {
            this.addTab(DEFAULT_TAB_TITLE, this.defaultTab);
        }
    }

    public PdfFile getCurrentFile() {
        Component currentComponent = this.getSelectedComponent();
        RupsPanel currentRupsPanel = (RupsPanel) currentComponent;
        return currentRupsPanel.getPdfFile();
    }

    public void saveCurrentFile(File file) {
        RupsPanel currentRupsPanel = (RupsPanel) this.getSelectedComponent();
        currentRupsPanel.getRupsInstanceController().saveFile(file);
    }
}
