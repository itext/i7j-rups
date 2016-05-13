/*
 * $Id$
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 2007-2015 iText Group NV
 * Authors: Bruno Lowagie et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.rups.controller;

import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.rups.event.*;
import com.itextpdf.rups.model.LoggerMessages;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.model.ProgressDialog;
import com.itextpdf.rups.view.Console;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.RupsMenuBar;
import com.itextpdf.rups.view.contextmenu.ConsoleContextMenu;
import com.itextpdf.rups.view.contextmenu.ContextMenuMouseListener;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class controls all the GUI components that are shown in
 * the RUPS application: the menu bar, the panels,...
 */
public class RupsController extends Observable
        implements TreeSelectionListener, PageSelectionListener, Observer {

    // member variables

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
     * The JMenuBar for the RUPS application.
     */
    protected RupsMenuBar menuBar;
    /**
     * Contains all other components: the page panel, the outline tree, etc.
     */
    protected JSplitPane masterComponent;

    protected JPanel masterPanel;

    protected JPanel treePanel;

    protected Frame ownedFrame;

    private boolean pluginMode;

    private ObjectLoader loader;

    // constructor

    /**
     * Constructs the GUI components of the RUPS application.
     */
    public RupsController(Dimension dimension, Frame frame, boolean pluginMode) {
        // creating components and controllers
        this.pluginMode = pluginMode;
        this.ownedFrame = frame;
        menuBar = new RupsMenuBar(this);
        addObserver(menuBar);
        Console console = Console.getInstance();
        addObserver(console);
        readerController = new PdfReaderController(this, this, pluginMode);
        addObserver(readerController);
        masterPanel = new JPanel(new BorderLayout());

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
        console.getTextArea().addMouseListener(new ContextMenuMouseListener(ConsoleContextMenu.getPopupMenu(console.getTextArea()), cons));
        editorPane.addTab("Console", null, cons, "Console window (System.out/System.err)");
        editorPane.setSelectedComponent(cons);
        info.add(editorPane, JSplitPane.RIGHT);

        //application specific features
        if (!pluginMode) {
            masterComponent.setDropTarget(new DropTarget() {
                // drag and drop for opening files
                public synchronized void drop(DropTargetDropEvent dtde) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = dtde.getTransferable();
                    java.util.List<File> files = null;

                    try {
                        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            files = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        }
                        if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) { // fix for Linux

                            String urls = (String) t.getTransferData(DataFlavor.stringFlavor);
                            files = new LinkedList();
                            StringTokenizer tokens = new StringTokenizer(urls);
                            while (tokens.hasMoreTokens()) {
                                String urlString = tokens.nextToken();
                                URL url = new URL(urlString);
                                files.add(new File(URLDecoder.decode(url.getFile(), "UTF-8")));
                            }
                        }

                        if (files == null || files.size() != 1) {
                            JOptionPane.showMessageDialog(masterComponent, "You can only open one file!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            loadFile(files.get(0), false);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(masterComponent, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    dtde.dropComplete(true);
                }
            });
        }

        masterPanel.add(masterComponent, BorderLayout.CENTER);
    }

    /**
     *
     */
    public RupsController(Dimension dimension, File f, Frame frame, boolean pluginMode) {
        this(dimension, frame, pluginMode);
        loadFile(f, false);
    }

    /**
     * Getter for the menubar.
     */
    public RupsMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Getter for the master component.
     */
    public Component getMasterComponent() {
        return masterPanel;
    }

    public Component getTreePanel() {
        return treePanel;
    }

    public void update(Observable o, Object arg) {
        //Events that have come from non observable classes: ObjectLoader and FileChooserAction
        if (o == null && arg instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) arg;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                    closeRoutine();
                    break;
                case RupsEvent.OPEN_FILE_EVENT:
                    loadFile((File)event.getContent(), false);
                    break;
                case RupsEvent.SAVE_TO_FILE_EVENT:
                    saveFile((File)event.getContent());
                    break;
                case RupsEvent.COMPARE_WITH_FILE_EVENT:
                    compareWithFile((File)event.getContent());
                    break;
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    setChanged();
                    super.notifyObservers(event);
                    break;
            }
        }
    }

    /**
     * @param file the file to load
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
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        } catch (PdfException de) {
            JOptionPane.showMessageDialog(masterComponent, de.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        } catch (com.itextpdf.io.IOException ioe) {
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
        byte[] res = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            res = readStreamToByteArray(inputStream);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                Logger logger = LoggerFactory.getLogger(RupsController.class);
                logger.warn(LoggerMessages.CLOSING_STREAM_ERROR, e);
            }
        }
        return res;
    }

    private byte[] readStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[4096];
        try {
            while ((read = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, read);
            }
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                Logger logger = LoggerFactory.getLogger(RupsController.class);
                logger.warn(LoggerMessages.CLOSING_STREAM_ERROR, e);
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
        } catch (PdfException de) {
            JOptionPane.showMessageDialog(masterComponent, de.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        } catch (com.itextpdf.io.IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Logger logger = LoggerFactory.getLogger(RupsController.class);
                logger.warn(LoggerMessages.CLOSING_STREAM_ERROR, e);
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
    }

    public CompareTool.CompareResult compareWithDocument(PdfDocument document) {
        CompareTool compareTool = new CompareTool().setCompareByContentErrorsLimit(100).disableCachedPagesComparison();
        try {
            CompareTool.CompareResult compareResult = compareTool.compareByCatalog(getPdfFile().getPdfDocument(), document);
            highlightChanges(compareResult);
            return compareResult;
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(RupsController.class);
            logger.warn(LoggerMessages.COMPARING_ERROR, e);
            return null;
        }
    }

    public CompareTool.CompareResult compareWithFile(File file) {
        PdfDocument cmpDocument = null;
        try {
            cmpDocument = new PdfDocument(new PdfReader(file.getAbsolutePath()));
            return compareWithDocument(cmpDocument);
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(RupsController.class);
            logger.warn(LoggerMessages.CREATE_COMPARE_DOC_ERROR, e);
            return null;
        } finally {
            if (cmpDocument != null) {
                cmpDocument.close();
            }
        }
    }

    public CompareTool.CompareResult compareWithStream(InputStream is) {
        PdfDocument cmpDocument = null;
        try {
            PdfReader reader = new PdfReader(is);
            reader.setCloseStream(false);
            cmpDocument = new PdfDocument(reader);
            return compareWithDocument(cmpDocument);
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(RupsController.class);
            logger.warn(LoggerMessages.CREATE_COMPARE_DOC_ERROR, e);
            return null;
        } finally {
            if (cmpDocument != null) {
                cmpDocument.close();
            }
        }
    }

    public void waitForLoader() {
        if (loader != null) {
            try {
                loader.join();
            } catch (InterruptedException e) {
                Logger logger = LoggerFactory.getLogger(RupsController.class);
                logger.warn(LoggerMessages.WAITING_FOR_LOADER_ERROR, e);
            }
        }
    }

    public void highlightChanges(CompareTool.CompareResult compareResult) {
        Logger logger = LoggerFactory.getLogger(RupsController.class);
        if (compareResult.isOk()) {
            logger.info("Documents are equal");
        } else {
            logger.info(compareResult.getReport());
        }
        readerController.update(this, new PostCompareEvent(compareResult));
    }

    private void startObjectLoader() {
        final ProgressDialog dialog = new ProgressDialog(getMasterComponent(), "Reading PDF docoment...", ownedFrame, pluginMode);
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
            if (!pluginMode) {
                menuBar.update(this, new RootNodeClickedEvent());
            }
            readerController.getPdfTree().clearSelection();
            return;
        }
        if (selectednode instanceof PdfObjectTreeNode) {
            readerController.update(this, new TreeNodeClickedEvent((PdfObjectTreeNode)selectednode));
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
