/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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

import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.rups.Rups;
import com.itextpdf.rups.event.CloseDocumentEvent;
import com.itextpdf.rups.event.PostCompareEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.event.TreeNodeClickedEvent;
import com.itextpdf.rups.model.IPdfFile;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.model.ProgressDialog;
import com.itextpdf.rups.view.Console;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.Snackbar;
import com.itextpdf.rups.view.contextmenu.ConsoleContextMenu;
import com.itextpdf.rups.view.contextmenu.ContextMenuMouseListener;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * RupsInstanceController is the controller in charge of an individual tab in RUPS. It controls
 * the actions that happen on the loaded file in that tab. It owns a {@link com.itextpdf.rups.view.RupsPanel RupsPanel}
 * which will display the view of the loaded file.
 * <p>
 * A RupsInstanceController instance will be owned by a {@link com.itextpdf.rups.view.RupsTabbedPane RupsTabbedPane}
 * instance. Which in turn is controlled by a {@link com.itextpdf.rups.controller.RupsController RupsController}
 * instance.
 */
public class RupsInstanceController extends Observable
        implements TreeSelectionListener, PageSelectionListener, Observer {

    private final JPanel ownerPanel;

    /**
     * Object with the GUI components for iText.
     *
     * @since iText 5.0.0 (renamed from reader which was confusing because reader is normally used for a PdfReader
     * instance)
     */
    private final PdfReaderController readerController;

    /**
     * Contains all other components: the page panel, the outline tree, etc.
     */
    private final JSplitPane masterComponent;

    /**
     * The Pdf file that is currently open in the application.
     */
    private IPdfFile pdfFile;

    private ObjectLoader loader;

    // constructor

    /**
     * Constructs the GUI components of the RUPS application.
     *
     * @param dimension the dimension
     * @param owner     the jpanel
     */
    public RupsInstanceController(Dimension dimension, JPanel owner) {
        // creating components and controllers
        this.ownerPanel = owner;
        final Console console = Console.getInstance();
        addObserver(console);
        console.addObserver(this);
        readerController = new PdfReaderController(this, this);
        addObserver(readerController);

        // creating the master component
        masterComponent = new JSplitPane();
        masterComponent.setOrientation(JSplitPane.VERTICAL_SPLIT);
        masterComponent.setDividerLocation((int) (dimension.getHeight() * .70));
        masterComponent.setDividerSize(2);

        final JSplitPane content = new JSplitPane();
        masterComponent.add(content, JSplitPane.TOP);
        final JSplitPane info = new JSplitPane();
        masterComponent.add(info, JSplitPane.BOTTOM);

        content.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        content.setDividerLocation((int) (dimension.getWidth() * .6));
        content.setDividerSize(1);
        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.add(new JScrollPane(readerController.getPdfTree()), BorderLayout.CENTER);
        content.add(treePanel, JSplitPane.LEFT);
        content.add(readerController.getNavigationTabs(), JSplitPane.RIGHT);

        info.setDividerLocation((int) (dimension.getWidth() * .3));
        info.setDividerSize(1);
        info.add(readerController.getObjectPanel(), JSplitPane.LEFT);
        final JTabbedPane editorPane = readerController.getEditorTabs();
        final JScrollPane cons = new JScrollPane(console.getTextArea());
        console.getTextArea().addMouseListener(
                new ContextMenuMouseListener(ConsoleContextMenu.getPopupMenu(console.getTextArea()),
                        console.getTextArea()));
        editorPane.addTab(Language.CONSOLE.getString(), null, cons, Language.CONSOLE_TOOL_TIP.getString());
        editorPane.setSelectedComponent(cons);
        info.add(editorPane, JSplitPane.RIGHT);

        ownerPanel.add(masterComponent, BorderLayout.CENTER);
    }

    public final void update(Observable o, Object arg) {
        //Events that have come from non observable classes: ObjectLoader and FileChooserAction
        if (o == null && arg instanceof RupsEvent) {
            final RupsEvent event = (RupsEvent) arg;
            switch (event.getType()) {
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    setChanged();
                    super.notifyObservers(event);
                    break;
            }
        }
        //Events from observable classes
        if (o != null && arg instanceof RupsEvent) {
            final RupsEvent event = (RupsEvent) arg;
            if (RupsEvent.CONSOLE_WRITE_EVENT == event.getType()) {
                readerController.getEditorTabs()
                        .setSelectedIndex(readerController.getEditorTabs().getComponentCount() - 1);
            }
        }
    }

    /**
     * Load a file into memory and start processing it.
     *
     * @param file     the file to load
     */
    public void loadFile(File file) {
        loadFile(file, false);
    }

    public void loadFile(File file, boolean requireEditable) {
        closeRoutine();
        try {
            if (requireEditable) {
                pdfFile = PdfFile.openAsOwner(file);
            } else {
                pdfFile = PdfFile.open(file);
            }
            startObjectLoader();
            readerController.getParser().setDocument(pdfFile.getPdfDocument());
        } catch (IOException | PdfException | com.itextpdf.io.exceptions.IOException ioe) {
            Snackbar.make(Rups.getMainFrame(), ioe.getMessage()).show();
        }
    }

    /**
     * Saves the pdf to the disk.
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

            final ByteArrayOutputStream bos = pdfFile.getByteArrayOutputStream();
            pdfFile.getPdfDocument().setFlushUnusedObjects(false);
            closeRoutine();
            if (bos != null) {
                bos.close();
                fos = Files.newOutputStream(localFile.toPath());
                bos.writeTo(fos);
            }

            JOptionPane.showMessageDialog(masterComponent, Language.SAVE_SUCCESS.getString(),
                    Language.DIALOG.getString(), JOptionPane.INFORMATION_MESSAGE);
            loadFile(localFile);
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

    public final void closeRoutine() {
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
        readerController.getParser().setDocument(null);
    }

    public final CompareTool.CompareResult compareWithDocument(PdfDocument document) {
        if (getPdfFile() == null || getPdfFile().getPdfDocument() == null) {
            LoggerHelper.warn(Language.ERROR_NO_OPEN_DOCUMENT_COMPARE.getString(), getClass());
        } else if (document == null) {
            LoggerHelper.warn(Language.ERROR_COMPARED_DOCUMENT_NULL.getString(), getClass());
        } else if (document.isClosed()) {
            LoggerHelper.warn(Language.ERROR_COMPARED_DOCUMENT_CLOSED.getString(), getClass());
        } else {
            final CompareTool compareTool =
                    new CompareTool().setCompareByContentErrorsLimit(100).disableCachedPagesComparison();
            return compareTool.compareByCatalog(getPdfFile().getPdfDocument(), document);
        }
        return null;
    }

    public final CompareTool.CompareResult compareWithFile(File file) {
        try (PdfReader readerPdf = new PdfReader(file.getAbsolutePath());
                final PdfDocument cmpDocument = new PdfDocument(readerPdf)) {
            return compareWithDocument(cmpDocument);
        } catch (IOException e) {
            LoggerHelper.warn(Language.ERROR_COMPARE_DOCUMENT_CREATION.getString(), e, getClass());
            return null;
        }
    }

    public final CompareTool.CompareResult compareWithStream(InputStream is) {
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
                new ProgressDialog(this.ownerPanel, Language.PDF_READING.getString(), null);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.setVisible(true);
            }
        });
        loader = new ObjectLoader(
                this, pdfFile, pdfFile.getOriginalFile().getName(), dialog
        );
        loader.start();
    }

    // tree selection

    @Override
    public void valueChanged(TreeSelectionEvent evt) {
        final Object selectedNode = readerController.getPdfTree().getLastSelectedPathComponent();
        if (selectedNode instanceof PdfTrailerTreeNode) {
            readerController.getPdfTree().clearSelection();
            return;
        }
        if (selectedNode instanceof PdfObjectTreeNode) {
            readerController.update(this, new TreeNodeClickedEvent((PdfObjectTreeNode) selectedNode));
        }
    }

    // page navigation

    @Override
    public int gotoPage(int pageNumber) {
        readerController.gotoPage(pageNumber);
        return pageNumber;
    }

    /**
     * Getter for the pdfFile
     *
     * @return pdfFile
     */
    public IPdfFile getPdfFile() {
        return pdfFile;
    }

}
