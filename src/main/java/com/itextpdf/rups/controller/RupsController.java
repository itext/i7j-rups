/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups.controller;

import com.itextpdf.kernel.actions.data.ITextCoreProductData;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.rups.event.CloseDocumentEvent;
import com.itextpdf.rups.event.PostCompareEvent;
import com.itextpdf.rups.event.PostNewIndirectObjectEvent;
import com.itextpdf.rups.event.RootNodeClickedEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.event.TreeNodeClickedEvent;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.model.ProgressDialog;
import com.itextpdf.rups.view.Console;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.NewIndirectPdfObjectDialog;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.RupsMenuBar;
import com.itextpdf.rups.view.contextmenu.ConsoleContextMenu;
import com.itextpdf.rups.view.contextmenu.ContextMenuMouseListener;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

/**
 * This class controls all the GUI components that are shown in
 * the RUPS application: the menu bar, the panels,...
 */
public class RupsController extends Observable
        implements TreeSelectionListener, PageSelectionListener, Observer {

    private static final int COMPARE_BY_CONTENT_MAX_ERRORCOUNT = 100;

    /**
     * The Pdf file that is currently open in the application.
     */
    protected PdfFile pdfFile;
    /**
     * Object with the GUI components for iText.
     *
     * @since iText 5.0.0 (renamed from reader which was confusing because reader is normally used for a PdfReader
     * instance)
     */
    protected PdfReaderController readerController;

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

    private final boolean pluginMode;

    private ObjectLoader loader;

    /**
     * Constructs the GUI components of the RUPS application.
     *
     * @param dimension  the dimension
     * @param frame      the frame
     * @param pluginMode the plugin mode
     */
    public RupsController(Dimension dimension, Frame frame, boolean pluginMode) {
        // creating components and controllers
        this.pluginMode = pluginMode;
        this.ownedFrame = frame;
        menuBar = new RupsMenuBar(this);
        addObserver(menuBar);
        Console console = Console.getInstance();
        addObserver(console);
        console.addObserver(this);
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
        console.getTextArea().addMouseListener(
                new ContextMenuMouseListener(ConsoleContextMenu.getPopupMenu(console.getTextArea()),
                        console.getTextArea()));
        editorPane.addTab(Language.EDITOR_CONSOLE.getString(), null, cons, Language.EDITOR_CONSOLE_TOOLTIP.getString());
        editorPane.setSelectedComponent(cons);
        info.add(editorPane, JSplitPane.RIGHT);

        //application specific features
        if (!pluginMode) {
            masterComponent.setDropTarget(new DropTarget() {
                // drag and drop for opening files
                public synchronized void drop(DropTargetDropEvent dtde) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = dtde.getTransferable();
                    List<File> files = null;

                    try {
                        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        }
                        if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) { // fix for Linux

                            String urls = (String) t.getTransferData(DataFlavor.stringFlavor);
                            files = new LinkedList<>();
                            StringTokenizer tokens = new StringTokenizer(urls);
                            while (tokens.hasMoreTokens()) {
                                String urlString = tokens.nextToken();
                                URL url = new URL(urlString);
                                files.add(new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name())));
                            }
                        }

                        if (files == null || files.size() != 1) {
                            JOptionPane.showMessageDialog(masterComponent,
                                    Language.ERROR_ONLY_OPEN_ONE_FILE.getString(),
                                    Language.ERROR.getString(), JOptionPane.ERROR_MESSAGE);
                        } else {
                            loadFile(files.get(0), false);
                        }
                    } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
                        JOptionPane.showMessageDialog(masterComponent,
                                String.format(Language.ERROR_OPENING_FILE.getString(), e.getMessage()),
                                Language.ERROR.getString(),
                                JOptionPane.ERROR_MESSAGE);
                    }

                    dtde.dropComplete(true);
                }
            });
        }

        masterPanel.add(masterComponent, BorderLayout.CENTER);
    }

    /**
     * Getter for the menubar.
     *
     * @return the menubar
     */
    public RupsMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Getter for the master component.
     *
     * @return the master component
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
                    loadFile((File) event.getContent(), false);
                    break;
                case RupsEvent.SAVE_TO_FILE_EVENT:
                    saveFile((File) event.getContent());
                    break;
                case RupsEvent.COMPARE_WITH_FILE_EVENT:
                    highlightChanges(null);
                    CompareTool.CompareResult result = compareWithFile((File) event.getContent());
                    highlightChanges(result);
                    break;
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    setChanged();
                    super.notifyObservers(event);
                    break;
                case RupsEvent.NEW_INDIRECT_OBJECT_EVENT:
                    showNewIndirectDialog();
                    break;
            }
        }
        //Events from observable classes
        if (o != null && arg instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) arg;
            if (RupsEvent.CONSOLE_WRITE_EVENT == event.getType()) {
                readerController.getEditorTabs()
                        .setSelectedIndex(readerController.getEditorTabs().getComponentCount() - 1);
            }
        }
    }

    /**
     * @param file     the file to load
     * @param readOnly open the file read only or not
     */
    public void loadFile(File file, boolean readOnly) {
        try {
            byte[] contents = readFileToByteArray(file);
            loadRawContent(contents, file.getName(), file.getParentFile(), readOnly);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), Language.DIALOG.getString(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadFileFromStream(InputStream is, String fileName, File directory, boolean readOnly) {
        try {
            byte[] contents = readStreamToByteArray(is);
            loadRawContent(contents, fileName, directory, readOnly);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), Language.DIALOG.getString(),
                    JOptionPane.ERROR_MESSAGE);
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
                ownedFrame.setTitle(
                        String.format(Language.TITLE.getString(), ITextCoreProductData.getInstance().getVersion())
                );
            }
            readerController.getParser().setDocument(pdfFile.getPdfDocument());
        } catch (IOException | PdfException | com.itextpdf.io.exceptions.IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), Language.DIALOG.getString(),
                    JOptionPane.ERROR_MESSAGE);
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
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LoggerHelper.error(Language.ERROR_CLOSING_STREAM.getString(), e, getClass());
            }
        }
        return res;
    }

    private byte[] readStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int read;
        final int bufferSize = 4096;
        final byte[] buffer = new byte[bufferSize];
        try {
            while ((read = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, read);
            }
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                LoggerHelper.error(Language.ERROR_CLOSING_STREAM.getString(), e, getClass());
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Saves the PDF to the disk
     *
     * @param file java.io.File file to save
     */
    public void saveFile(File file) {
        OutputStream fos = null;
        File localFile = file;
        try {
            final String pdfSuffix = ".pdf";
            if (!localFile.getName().endsWith(pdfSuffix)) {
                localFile = new File(localFile.getPath() + pdfSuffix);
            }

            if (localFile.exists()) {
                final int choice = JOptionPane.showConfirmDialog(masterComponent, Language.SAVE_OVERWRITE.getString(),
                        Language.WARNING.getString(), JOptionPane.YES_NO_CANCEL_OPTION);
                if (choice == JOptionPane.NO_OPTION || choice == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }

            ByteArrayOutputStream bos = pdfFile.getByteArrayOutputStream();
            pdfFile.getPdfDocument().setFlushUnusedObjects(false);
            closeRoutine();
            if (bos != null) {
                bos.close();
                fos = Files.newOutputStream(localFile.toPath());
                bos.writeTo(fos);
            }

            JOptionPane.showMessageDialog(masterComponent, Language.SAVE_SUCCESS.getString(),
                    Language.DIALOG.getString(), JOptionPane.INFORMATION_MESSAGE);
            loadFile(file, false);
        } catch (PdfException | IOException | com.itextpdf.io.exceptions.IOException de) {
            JOptionPane.showMessageDialog(masterComponent, de.getMessage(), Language.DIALOG.getString(),
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LoggerHelper.error(Language.ERROR_CLOSING_STREAM.getString(), e, getClass());
            }
        }
    }

    public void closeRoutine() {
        loader = null;
        PdfDocument docToClose = null;
        if (pdfFile != null && pdfFile.getPdfDocument() != null) {
            docToClose = pdfFile.getPdfDocument();
        }
        pdfFile = null;
        setChanged();
        super.notifyObservers(new CloseDocumentEvent());
        if (docToClose != null) {
            docToClose.close();
        }
        if (!pluginMode) {
            ownedFrame.setTitle(
                    String.format(Language.TITLE.getString(), ITextCoreProductData.getInstance().getVersion())
            );
        }
        readerController.getParser().setDocument(null);
    }

    public CompareTool.CompareResult compareWithDocument(PdfDocument document) {
        if (getPdfFile() == null || getPdfFile().getPdfDocument() == null) {
            LoggerHelper.warn(Language.ERROR_NO_OPEN_DOCUMENT_COMPARE.getString(), getClass());
        } else if (document == null) {
            LoggerHelper.warn(Language.ERROR_COMPARED_DOCUMENT_NULL.getString(), getClass());
        } else if (document.isClosed()) {
            LoggerHelper.warn(Language.ERROR_COMPARED_DOCUMENT_CLOSED.getString(), getClass());
        } else {
            final CompareTool compareTool =
                    new CompareTool()
                            .setCompareByContentErrorsLimit(COMPARE_BY_CONTENT_MAX_ERRORCOUNT)
                            .disableCachedPagesComparison();
            return compareTool.compareByCatalog(getPdfFile().getPdfDocument(), document);
        }
        return null;
    }

    public CompareTool.CompareResult compareWithFile(File file) {
        try (PdfReader readerPdf = new PdfReader(file.getAbsolutePath());
                PdfDocument cmpDocument = new PdfDocument(readerPdf)) {
            return compareWithDocument(cmpDocument);
        } catch (IOException e) {
            LoggerHelper.warn(Language.ERROR_COMPARE_DOCUMENT_CREATION.getString(), e, getClass());
            return null;
        }
    }

    public CompareTool.CompareResult compareWithStream(InputStream is) {
        try (PdfReader reader = new PdfReader(is);
                PdfDocument cmpDocument = new PdfDocument(reader)) {
            reader.setCloseStream(false);
            return compareWithDocument(cmpDocument);
        } catch (IOException e) {
            LoggerHelper.warn(Language.ERROR_COMPARE_DOCUMENT_CREATION.getString(), e, getClass());
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
                LoggerHelper.info(Language.COMPARE_EQUAL.getString(), getClass());
            } else {
                LoggerHelper.info(compareResult.getReport(), getClass());
            }
        }
    }

    private void startObjectLoader() {
        final ProgressDialog dialog =
                new ProgressDialog(getMasterComponent(), Language.PDF_READING.getString(), ownedFrame, pluginMode);
        if (!pluginMode) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            });
        }
        loader = new ObjectLoader(this, pdfFile, pdfFile.getFilename(), dialog);
        loader.start();
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

    private void showNewIndirectDialog() {
        final NewIndirectPdfObjectDialog dialog =
                new NewIndirectPdfObjectDialog(ownedFrame, Language.MENU_BAR_NEW_INDIRECT.getString(),
                        readerController.getParser());
        dialog.setVisible(true);
        if (dialog.getResult() != null) {
            setChanged();
            notifyObservers(new PostNewIndirectObjectEvent(dialog.getResult()));
        }
    }
}
