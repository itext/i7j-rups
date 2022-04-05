package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsInstanceController;
import com.itextpdf.rups.model.PdfFile;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * The class holding the JTabbedPane that holds the Rups tabs. This class is responsible for loading, closing, and
 * saving PDF files handled by the {@link com.itextpdf.rups.controller.RupsInstanceController rupsInstanceController}
 * object that is tied to a {@link com.itextpdf.rups.view.RupsPanel RupsPanel}, which is the main content pane for the
 * tabs in the JTabbedPane.
 */
public class RupsTabbedPane {

    private final JPanel defaultTab;
    private final JTabbedPane jTabbedPane;

    public RupsTabbedPane() {
        this.jTabbedPane = new JTabbedPane();
        this.defaultTab = new JPanel();
        this.defaultTab.add(new JLabel(Language.DEFAULT_TAB_TEXT.getString()));
        this.jTabbedPane.addTab(Language.DEFAULT_TAB_TITLE.getString(), defaultTab);
    }

    public void openNewFile(File file, Dimension dimension, boolean readonly) {
        if (file != null) {
            if (this.defaultTab.equals(this.jTabbedPane.getSelectedComponent())) {
                this.jTabbedPane.removeTabAt(this.jTabbedPane.getSelectedIndex());
            }

            RupsPanel rupsPanel = new RupsPanel();
            RupsInstanceController rupsInstanceController = new RupsInstanceController(dimension, rupsPanel, false);
            rupsPanel.setRupsInstanceController(rupsInstanceController);
            rupsInstanceController.loadFile(file, readonly);
            this.jTabbedPane.addTab(file.getName(), null, rupsPanel);
            this.jTabbedPane.setSelectedComponent(rupsPanel);
        }
    }

    public boolean closeCurrentFile() {
        boolean isLastTab = this.jTabbedPane.getTabCount() == 1;

        this.jTabbedPane.removeTabAt(this.jTabbedPane.getSelectedIndex());

        if (this.jTabbedPane.getTabCount() == 0) {
            this.jTabbedPane.addTab(Language.DEFAULT_TAB_TITLE.getString(), this.defaultTab);
        }

        return isLastTab;
    }

    public PdfFile getCurrentFile() {
        Component currentComponent = this.jTabbedPane.getSelectedComponent();
        RupsPanel currentRupsPanel = (RupsPanel) currentComponent;
        return currentRupsPanel.getPdfFile();
    }

    public void saveCurrentFile(File file) {
        RupsPanel currentRupsPanel = (RupsPanel) this.jTabbedPane.getSelectedComponent();
        currentRupsPanel.getRupsInstanceController().saveFile(file);
    }

    public Component getJTabbedPane() {
        return this.jTabbedPane;
    }
}
