/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
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

import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.rups.event.CloseDocumentEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.event.TreeNodeClickedEvent;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.model.ProgressDialog;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.dock.InfoDockPanel;
import com.itextpdf.rups.view.dock.XRefTable;
import com.itextpdf.rups.view.dock.PagesTable;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
    private final JPanel masterComponent;

    /**
     * The Pdf file that is currently open in the application.
     */
    private PdfFile pdfFile;

    private ObjectLoader loader;

    private Map<Class, Component> dockedComponentsMap;

    // constructor

    /**
     * Constructs the GUI components of the RUPS application.
     *
     * @param owner     the jpanel
     */
    public RupsInstanceController(JPanel owner) {
        // creating components and controllers
        this.ownerPanel = owner;
        this.dockedComponentsMap = new HashMap<>();
        this.readerController = new PdfReaderController(this, this);

        addObserver(readerController);

        this.masterComponent = new JPanel(new BorderLayout());
        this.masterComponent.add(new JScrollPane(readerController.getPdfTree()), BorderLayout.CENTER);

        ownerPanel.add(this.masterComponent, BorderLayout.CENTER);
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
    }

    /**
     * Load a file into memory and start processing it.
     *
     * @param file     the file to load
     * @param readOnly open the file read only or not
     */
    public void loadFile(File file, boolean readOnly) {
        try {
            final Path filePath = Paths.get(file.toURI());
            final byte[] contents = Files.readAllBytes(filePath);
            loadRawContent(contents, file.getName(), file.getParentFile(), readOnly);

            // TODO refactor this!
            InfoDockPanel infoDockPanel = new InfoDockPanel(pdfFile);
            this.dockedComponentsMap.put(InfoDockPanel.class, infoDockPanel);

            XRefTable xRefTable = new XRefTable(readerController, loader);
            this.dockedComponentsMap.put(XRefTable.class, xRefTable);

            PagesTable pagesTable = new PagesTable(readerController, this, loader);
            this.dockedComponentsMap.put(PagesTable.class, pagesTable);
            this.readerController.addObserver(pagesTable);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), Language.DIALOG.getString(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public final void loadRawContent(byte[] contents, String fileName, File directory, boolean readOnly) {
        closeRoutine();
        try {
            pdfFile = new PdfFile(contents, readOnly);
            pdfFile.setFilename(fileName);
            pdfFile.setDirectory(directory);
            startObjectLoader();
            readerController.getParser().setDocument(pdfFile.getPdfDocument());
        } catch (IOException | PdfException | com.itextpdf.io.exceptions.IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), Language.DIALOG.getString(),
                    JOptionPane.ERROR_MESSAGE);
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

    private void startObjectLoader() {
        final ProgressDialog dialog =
                new ProgressDialog(this.ownerPanel, Language.PDF_READING.getString(), null);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.setVisible(true);
            }
        });
        loader = new ObjectLoader(this, pdfFile, pdfFile.getFilename(), dialog);
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
    public PdfFile getPdfFile() {
        return pdfFile;
    }

    public Component getDockedComponent(Class infoDockPanelClass) {
        return this.dockedComponentsMap.get(infoDockPanelClass);
    }
}
