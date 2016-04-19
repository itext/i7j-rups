package com.itextpdf.rups;

import com.itextpdf.kernel.Version;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.model.SwingHelper;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.InputStream;
import javax.swing.JComponent;
import javax.swing.JFrame;

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
     *
     * @param f a file that should be opened on launch
     */
    public static Rups startNewApplication(File f, final int onCloseOperation) {
        final Rups rups = new Rups();
        SwingHelper.invokeSync(new Runnable() {
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
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                RupsController controller = new RupsController(size, frame, true);
                comp.add(controller.getMasterComponent());
                rups.setController(controller);
            }
        });
        return rups;
    }

    public void loadDocumentFromFile(final File f, final boolean readOnly) {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                getController().loadFile(f, readOnly);
            }
        });
        getController().waitForLoader();
    }

    public void loadDocumentFromStream(final InputStream inputStream, final String name, final File directory, final boolean readOnly) {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                getController().loadFileFromStream(inputStream, name, directory, readOnly);
            }
        });
        getController().waitForLoader();
    }

    public void loadDocumentFromRawContent(final byte[] bytes, final String name, final File directory, final boolean readOnly) {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                getController().loadRawContent(bytes, name, directory, readOnly);
            }
        });
        getController().waitForLoader();
    }

    public void closeDocument() {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                getController().closeRoutine();
            }
        });
    }

    public void saveDocumentAs(final File f) {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                getController().saveFile(f);
            }
        });
    }

    public void compareWithDocument(final PdfDocument document) {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                getController().compareWithDocument(document);
            }
        });
    }

    public void compareWithFile(final File file) {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                getController().compareWithFile(file);           }
        });
    }

    public void compareWithStream(final InputStream is) {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                getController().compareWithStream(is);           }
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
        frame.setSize((int) (screen.getWidth() * .90), (int) (screen.getHeight() * .90));
        frame.setLocation((int) (screen.getWidth() * .05), (int) (screen.getHeight() * .05));
        frame.setResizable(true);
    }
}