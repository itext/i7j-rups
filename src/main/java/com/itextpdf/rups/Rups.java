package com.itextpdf.rups;

import com.itextpdf.kernel.Version;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.SwingHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;

public class Rups {

    private RupsController controller;

    private volatile CompareTool.CompareResult lastCompareResult = null;

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
     *
     * @param f                a file that should be opened on launch
     * @param onCloseOperation the close operation
     * @return a new RUPS application
     */
    public static Rups startNewApplication(File f, final int onCloseOperation) {
        final Rups rups = new Rups();
        SwingHelper.invoke(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                // defines the size and location
                initFrameDim(frame);
                RupsController controller = new RupsController(frame.getSize(), frame, false);
                initApplication(frame, controller, onCloseOperation);
                rups.setController(controller);
            }
        });
        if (null != f && f.canRead()) {
            rups.loadDocumentFromFile(f, false);
        }
        return rups;
    }

    public static Rups startNewPlugin(final JComponent comp, final Dimension size, final Frame frame) {
        final Rups rups = new Rups();
        SwingHelper.invoke(new Runnable() {
            public void run() {
                RupsController controller = new RupsController(size, frame, true);
                comp.add(controller.getMasterComponent());
                rups.setController(controller);
            }
        });
        return rups;
    }

    public void loadDocumentFromFile(final File f, final boolean readOnly) {
        SwingHelper.invoke(new Runnable() {
            public void run() {
                getController().loadFile(f, readOnly);
            }
        });
        getController().waitForLoader();
    }

    public void loadDocumentFromStream(final InputStream inputStream, final String name, final File directory, final boolean readOnly) {
        SwingHelper.invoke(new Runnable() {
            public void run() {
                getController().loadFileFromStream(inputStream, name, directory, readOnly);
            }
        });
        getController().waitForLoader();
    }

    public void loadDocumentFromRawContent(final byte[] bytes, final String name, final File directory, final boolean readOnly) {
        SwingHelper.invoke(new Runnable() {
            public void run() {
                getController().loadRawContent(bytes, name, directory, readOnly);
            }
        });
        getController().waitForLoader();
    }

    public void closeDocument() {
        SwingHelper.invoke(new Runnable() {
            public void run() {
                getController().closeRoutine();
            }
        });
    }

    public void saveDocumentAs(final File f) {
        SwingHelper.invoke(new Runnable() {
            public void run() {
                getController().saveFile(f);
            }
        });
    }

    public boolean compareWithDocument(final PdfDocument document) {
        return compareWithDocument(document, false);
    }

    public boolean compareWithDocument(final PdfDocument document, final boolean showResults) {
        SwingHelper.invoke(new Runnable() {
            public void run() {
                lastCompareResult = getController().compareWithDocument(document);
                if (!showResults) {
                    getController().highlightChanges(lastCompareResult);
                }
            }
        });
        return isEqual();
    }

    public boolean compareWithFile(final File file) {
        return compareWithFile(file, false);
    }

    public boolean compareWithFile(final File file, final boolean showResults) {
        SwingHelper.invoke(new Runnable() {
            public void run() {
                lastCompareResult = getController().compareWithFile(file);
                if (!showResults) {
                    getController().highlightChanges(lastCompareResult);
                }
            }
        });
        return isEqual();
    }

    public boolean compareWithStream(final InputStream is) {
        return compareWithStream(is, false);
    }

    public boolean compareWithStream(final InputStream is, final boolean showResults) {
        SwingHelper.invoke(new Runnable() {
            public void run() {
                lastCompareResult = getController().compareWithStream(is);
                if (!showResults) {
                    getController().highlightChanges(lastCompareResult);
                }
            }
        });
        return isEqual();
    }

    public void highlightLastSavedChanges() {
        SwingHelper.invoke(new Runnable() {
            public void run() {
                getController().highlightChanges(lastCompareResult);
            }
        });
    }

    public void clearHighlights() {
        SwingHelper.invoke(new Runnable() {
            @Override
            public void run() {
                getController().highlightChanges(null);
            }
        });
    }

    public void logToConsole(final String message) {
        SwingHelper.invoke(new Runnable() {
            @Override
            public void run() {
                LoggerHelper.info(message, getClass());
            }
        });
    }

    protected static void initApplication(JFrame frame, RupsController controller, final int onCloseOperation) {
        // title bar
        frame.setTitle("iText RUPS " + Version.getInstance().getVersion());
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Rups.class.getResource("logo.png")));
        frame.setDefaultCloseOperation(onCloseOperation);
        // the content
        frame.setJMenuBar(controller.getMenuBar());
        frame.getContentPane().add(controller.getMasterComponent(), java.awt.BorderLayout.CENTER);
        frame.setVisible(true);
    }

    protected static void initFrameDim(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screen.getWidth() * .90), (int) (screen.getHeight() * .90));
        frame.setLocation((int) (screen.getWidth() * .05), (int) (screen.getHeight() * .05));
        frame.setResizable(true);
    }

    private boolean isEqual() {
        return lastCompareResult != null && lastCompareResult.isOk();
    }
}