package com.itextpdf.rups.controller;

import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.rups.event.CloseDocumentEvent;
import com.itextpdf.rups.event.PostCompareEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.event.TreeNodeClickedEvent;
import com.itextpdf.rups.model.*;
import com.itextpdf.rups.view.Console;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.contextmenu.ConsoleContextMenu;
import com.itextpdf.rups.view.contextmenu.ContextMenuMouseListener;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.io.*;
import java.util.Observable;
import java.util.Observer;

public class RupsInstanceController extends Observable implements TreeSelectionListener, PageSelectionListener, Observer {
    private final boolean pluginMode;


    /* file and controller */
    /**
     * The Pdf file that is currently open in the application.
     */
    protected PdfFile pdfFile;

    protected StringBuilder rawText = new StringBuilder();
    /**
     * Object with the GUI components for iText.
     *
     * @since iText 5.0.0 (renamed from reader which was confusing because reader is normally used for a PdfReader instance)
     */
    protected PdfReaderController readerController;

    /* main components */
    /**
     * Contains all other components: the page panel, the outline tree, etc.
     */
    protected JSplitPane masterComponent;

    protected JPanel treePanel;

    protected JPanel ownerPanel;
    private ObjectLoader loader;

    // constructor

    /**
     * Constructs the GUI components of the RUPS application.
     *
     * @param dimension the dimension
     * @param owner the jpanel
     * @param pluginMode the plugin mode
     */
    public RupsInstanceController(Dimension dimension, JPanel owner, boolean pluginMode) {
        // creating components and controllers
        this.ownerPanel = owner;
        this.pluginMode = pluginMode;
        Console console = Console.getInstance();
        addObserver(console);
        console.addObserver(this);
        readerController = new PdfReaderController(this, this, pluginMode);
        addObserver(readerController);

        // creating the master component
        masterComponent = new JSplitPane();
        masterComponent.setOrientation(JSplitPane.VERTICAL_SPLIT);
        masterComponent.setDividerLocation((int) (dimension.getHeight() * .70));
        masterComponent.setDividerSize(2);

        JSplitPane content = new JSplitPane();
        masterComponent.add(content, JSplitPane.TOP);
        JSplitPane info = new JSplitPane();
        masterComponent.add(info, JSplitPane.BOTTOM);

        content.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        content.setDividerLocation((int) (dimension.getWidth() * .6));
        content.setDividerSize(1);
        treePanel = new JPanel(new BorderLayout());
        treePanel.add(new JScrollPane(readerController.getPdfTree()), BorderLayout.CENTER);
        content.add(treePanel, JSplitPane.LEFT);
        content.add(readerController.getNavigationTabs(), JSplitPane.RIGHT);

        info.setDividerLocation((int) (dimension.getWidth() * .3));
        info.setDividerSize(1);
        info.add(readerController.getObjectPanel(), JSplitPane.LEFT);
        JTabbedPane editorPane = readerController.getEditorTabs();
        JScrollPane cons = new JScrollPane(console.getTextArea());
        console.getTextArea().addMouseListener(new ContextMenuMouseListener(ConsoleContextMenu.getPopupMenu(console.getTextArea()), console.getTextArea()));
        editorPane.addTab("Console", null, cons, "Console window (System.out/System.err)");
        editorPane.setSelectedComponent(cons);
        info.add(editorPane, JSplitPane.RIGHT);

        ownerPanel.add(masterComponent, BorderLayout.CENTER);
    }

    public RupsInstanceController(Dimension dimension, File f, JPanel owner, boolean pluginMode) {
        this(dimension, owner, pluginMode);
        loadFile(f, false);
    }

    /**
     * Getter for the master component.
     *
     * @return the master component
     */
    public Component getMasterComponent() {
        return ownerPanel;
    }

    public Component getTreePanel() {
        return treePanel;
    }

    public void update(Observable o, Object arg) {
        //Events that have come from non observable classes: ObjectLoader and FileChooserAction
        if (o == null && arg instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) arg;
            switch (event.getType()) {
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    setChanged();
                    super.notifyObservers(event);
                    break;
            }
        }
        //Events from observable classes
        if (o != null && arg instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) arg;
            if (RupsEvent.CONSOLE_WRITE_EVENT == event.getType()) {
                readerController.getEditorTabs().setSelectedIndex(readerController.getEditorTabs().getComponentCount() - 1);
            }
        }
    }

    /**
     * @param file the file to load
     * @param readOnly open the file read only or not
     */
    public void loadFile(File file, boolean readOnly) {
        try {
            byte[] contents = readFileToByteArray(file);
            loadRawContent(contents, file.getName(), file.getParentFile(), readOnly);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadFileFromStream(InputStream is, String fileName, File directory, boolean readOnly) {
        try {
            byte[] contents = readStreamToByteArray(is);
            loadRawContent(contents, fileName, directory, readOnly);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadRawContent(byte[] contents, String fileName, File directory, boolean readOnly) {
        closeRoutine();
        try {
            pdfFile = new PdfFile(contents, readOnly);
            pdfFile.setFilename(fileName);
            pdfFile.setDirectory(directory);
            startObjectLoader();
            if (!pluginMode) {
                String directoryPath = directory == null ? "" : directory.getCanonicalPath() + File.separator;
            }
            readerController.getParser().setDocument(pdfFile.getPdfDocument());
        } catch (IOException | PdfException | com.itextpdf.io.exceptions.IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reads a File to a byte[]
     *
     * @param file java.io.File
     * @return the file as a byte array
     * @throws IOException
     */
    private byte[] readFileToByteArray(File file) throws IOException {
        byte[] res;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            res = readStreamToByteArray(inputStream);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                LoggerHelper.error(LoggerMessages.CLOSING_STREAM_ERROR, e, getClass());
            }
        }
        return res;
    }

    private byte[] readStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int read;
        byte[] buffer = new byte[4096];
        try {
            while ((read = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, read);
            }
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                LoggerHelper.error(LoggerMessages.CLOSING_STREAM_ERROR, e, getClass());
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Saves the pdf to the disk
     *
     * @param file java.io.File file to save
     */
    public void saveFile(File file) {
        FileOutputStream fos = null;
        try {
            if (!file.getName().endsWith(".pdf")) {
                file = new File(file.getPath() + ".pdf");
            }

            if (file.exists()) {
                int choice = JOptionPane.showConfirmDialog(masterComponent, "File already exists, would you like to overwrite file?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
                if (choice == JOptionPane.NO_OPTION || choice == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }

            ByteArrayOutputStream bos = pdfFile.getByteArrayOutputStream();
            pdfFile.getPdfDocument().setFlushUnusedObjects(false);
            closeRoutine();
            if (bos != null) {
                bos.close();
                fos = new FileOutputStream(file);
                bos.writeTo(fos);
            }

            JOptionPane.showMessageDialog(masterComponent, "File saved.", "Dialog", JOptionPane.INFORMATION_MESSAGE);
            loadFile(file, false);
        } catch (PdfException | IOException | com.itextpdf.io.exceptions.IOException de) {
            JOptionPane.showMessageDialog(masterComponent, de.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LoggerHelper.error(LoggerMessages.CLOSING_STREAM_ERROR, e, getClass());
            }
        }
    }

    public void closeRoutine() {
        loader = null;
        PdfDocument docToClose = null;
        if (pdfFile != null && pdfFile.getPdfDocument() != null)
            docToClose = pdfFile.getPdfDocument();
        pdfFile = null;
        setChanged();
        super.notifyObservers(new CloseDocumentEvent());
        if (docToClose != null) {
            docToClose.close();
        }
        readerController.getParser().setDocument(null);
    }

    public CompareTool.CompareResult compareWithDocument(PdfDocument document) {
        if (getPdfFile() == null || getPdfFile().getPdfDocument() == null) {
            LoggerHelper.warn(LoggerMessages.NO_OPEN_DOCUMENT, getClass());
        } else if (document == null) {
            LoggerHelper.warn(LoggerMessages.COMPARED_DOCUMENT_IS_NULL, getClass());
        } else if (document.isClosed()) {
            LoggerHelper.warn(LoggerMessages.COMPARED_DOCUMENT_IS_CLOSED, getClass());
        } else {
            CompareTool compareTool = new CompareTool().setCompareByContentErrorsLimit(100).disableCachedPagesComparison();
            return compareTool.compareByCatalog(getPdfFile().getPdfDocument(), document);
        }
        return null;
    }

    public CompareTool.CompareResult compareWithFile(File file) {
        try (PdfReader readerPdf = new PdfReader(file.getAbsolutePath());
             PdfDocument cmpDocument = new PdfDocument(readerPdf)) {
            return compareWithDocument(cmpDocument);
        } catch (IOException e) {
            LoggerHelper.warn(LoggerMessages.CREATE_COMPARE_DOC_ERROR, e, getClass());
            return null;
        }
    }

    public CompareTool.CompareResult compareWithStream(InputStream is) {
        try (PdfReader reader = new PdfReader(is);
             PdfDocument cmpDocument = new PdfDocument(reader)) {
            reader.setCloseStream(false);
            return compareWithDocument(cmpDocument);
        } catch (IOException e) {
            LoggerHelper.warn(LoggerMessages.CREATE_COMPARE_DOC_ERROR, e, getClass());
            return null;
        }
    }

    /**
     * Clear all previous highlights and highlights the changes from the compare result.
     * If compare result is null will just clear all previous highlights.
     *
     * @param compareResult the compare result
     */
    public void highlightChanges(CompareTool.CompareResult compareResult) {
        readerController.update(this, new PostCompareEvent(compareResult));
        if (compareResult != null) {
            if (compareResult.isOk()) {
                LoggerHelper.info("Documents are equal", getClass());
            } else {
                LoggerHelper.info(compareResult.getReport(), getClass());
            }
        }
    }

    private void startObjectLoader() {
        final ProgressDialog dialog = new ProgressDialog(getMasterComponent(), "Reading PDF document...", null, pluginMode);
        if (!pluginMode) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            });
        }
        loader = new ObjectLoader(this, pdfFile, pdfFile.getFilename(), dialog);
    }

    // tree selection

    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent evt) {
        Object selectednode = readerController.getPdfTree().getLastSelectedPathComponent();
        if (selectednode instanceof PdfTrailerTreeNode) {
            readerController.getPdfTree().clearSelection();
            return;
        }
        if (selectednode instanceof PdfObjectTreeNode) {
            readerController.update(this, new TreeNodeClickedEvent((PdfObjectTreeNode) selectednode));
        }
    }

    // page navigation

    /**
     * @see com.itextpdf.rups.view.PageSelectionListener#gotoPage(int)
     */
    public int gotoPage(int pageNumber) {
        readerController.gotoPage(pageNumber);
        return pageNumber;
    }

    /**
     * Getter for the pdfFile
     *
     * @return pdfFile
     */
    public PdfFile getPdfFile() {
        return pdfFile;
    }

}
