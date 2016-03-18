package com.itextpdf.rups;

import com.itextpdf.kernel.Version;
import com.itextpdf.rups.controller.RupsController;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;

public class Rups {

    private RupsController controller;

    protected Rups() {
        this.controller = null;
    }

    protected RupsController getController() {
        return controller;
    }

    protected void setController(RupsController controller) {
        this.controller = controller;
    }

    /**
     * Initializes the main components of the Rups application.
     * @param f a file that should be opened on launch
     */
    public static Rups startNewApplication(final File f, final int onCloseOperation) {
        final Rups rups = new Rups();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                // defines the size and location
                initFrameDim(frame);
                RupsController controller = new RupsController(frame.getSize());
                initApplication(frame, controller, onCloseOperation);
                if (null != f && f.canRead()) {
                    controller.loadFile(f);
                }
                rups.setController(controller);
            }
        });
        return rups;
    }

    public static Rups startNewPlugin(final JComponent comp, final Dimension size) {
        final Rups rups = new Rups();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                RupsController controller = new RupsController(comp.getSize());
                comp.add(controller.getMasterComponent());
                rups.setController(controller);
            }
        });
        return rups;
    }

    public void loadDocumentFromFile(final File f) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.loadFile(f);
            }
        });
    }

    public void loadDocumentFromStream(final InputStream inputStream, final String name, final File directory) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.loadFileFromStream(inputStream, name, directory);
            }
        });
    }

    public void loadDocumentFromRawContent(final byte[] bytes, final String name, final File directory) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.loadRawContent(bytes, name, directory);
            }
        });
    }

    public void closeDocument() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.closeRoutine();
            }
        });
    }

    public void saveDocumentAs(final File f) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.saveFile(f);
            }
        });
    }

    protected static void initApplication(JFrame frame, RupsController controller, final int onCloseOperation) {
        // title bar
        frame.setTitle("iText RUPS " + Version.getInstance().getVersion());
        frame.setDefaultCloseOperation(onCloseOperation);
        // the content
        frame.setJMenuBar(controller.getMenuBar());
        frame.getContentPane().add(controller.getMasterComponent(), java.awt.BorderLayout.CENTER);
        frame.setVisible(true);
    }

    protected static void initFrameDim(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screen.getWidth() * .90), (int)(screen.getHeight() * .90));
        frame.setLocation((int)(screen.getWidth() * .05), (int)(screen.getHeight() * .05));
        frame.setResizable(true);
    }
}