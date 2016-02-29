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

import com.itextpdf.io.source.*;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.rups.io.FileChooserAction;
import com.itextpdf.rups.io.FileCloseAction;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.view.Console;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.RupsMenuBar;
import com.itextpdf.rups.view.contextmenu.ConsoleContextMenu;
import com.itextpdf.rups.view.contextmenu.ContextMenuMouseListener;
import com.itextpdf.rups.view.itext.PdfTree;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;
import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.*;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.Observable;
import java.util.StringTokenizer;

/**
 * This class controls all the GUI components that are shown in
 * the RUPS application: the menu bar, the panels,...
 */
public class RupsController extends Observable
	implements TreeSelectionListener, PageSelectionListener {

	// member variables

	/* file and controller */
	/** The Pdf file that is currently open in the application. */
	protected PdfFile pdfFile;
    protected StringBuilder rawText = new StringBuilder();
	/**
	 * Object with the GUI components for iText.
	 * @since	iText 5.0.0 (renamed from reader which was confusing because reader is normally used for a PdfReader instance)
	 */
	protected PdfReaderController readerController;

	/* main components */
	/** The JMenuBar for the RUPS application. */
	protected RupsMenuBar menuBar;
	/** Contains all other components: the page panel, the outline tree, etc. */
	protected JSplitPane masterComponent;


	// constructor
	/**
	 * Constructs the GUI components of the RUPS application.
	 */
	public RupsController(Dimension dimension) {
		// creating components and controllers
        menuBar = new RupsMenuBar(this);
        addObserver(menuBar);
		Console console = Console.getInstance();
		addObserver(console);
		readerController = new PdfReaderController(this, this);
		addObserver(readerController);

        // creating the master component
		masterComponent = new JSplitPane();
		masterComponent.setOrientation(JSplitPane.VERTICAL_SPLIT);
		masterComponent.setDividerLocation((int)(dimension.getHeight() * .70));
		masterComponent.setDividerSize(2);
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

                    if ( files == null || files.size() != 1 ) {
                        JOptionPane.showMessageDialog(masterComponent, "You can only open one file!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        loadFile(files.get(0));
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(masterComponent, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }

                dtde.dropComplete(true);
            }
        });

		JSplitPane content = new JSplitPane();
		masterComponent.add(content, JSplitPane.TOP);
		JSplitPane info = new JSplitPane();
		masterComponent.add(info, JSplitPane.BOTTOM);

		content.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		content.setDividerLocation((int)(dimension.getWidth() * .6));
		content.setDividerSize(1);
        content.add(new JScrollPane(readerController.getPdfTree()), JSplitPane.LEFT);
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

	}

	/**
	 *
	 */
	public RupsController(Dimension dimension, File f) {
		this(dimension);
		loadFile(f);
	}
	/** Getter for the menubar. */
	public RupsMenuBar getMenuBar() {
		return menuBar;
	}

	/** Getter for the master component. */
	public Component getMasterComponent() {
		return masterComponent;
	}

	// Observable

	/**
	 * @see java.util.Observable#notifyObservers(java.lang.Object)
	 */
	@Override
	public void notifyObservers(Object obj) {
		if (obj instanceof FileChooserAction) {
			File file = ((FileChooserAction)obj).getFile();

            /* save check */
            if ( ((FileChooserAction)obj).isNewFile() ) {
                saveFile(file);
            } else {
                loadFile(file);
            }
			return;
		}
		if (obj instanceof FileCloseAction) {
            close();
			return;
		}
	}

	/**
	 * @param file the file to load
	 */
	public void loadFile(File file) {
        close();
		try {
            byte[] contents = readFileToByteArray(file);

            pdfFile = new PdfFile(contents);
            pdfFile.setDirectory(file.getParentFile());
            pdfFile.setFilename(file.getName());

            setChanged();
			super.notifyObservers(RupsMenuBar.OPEN);
			readerController.startObjectLoader(pdfFile);
            readerController.addNonObserverTabs(pdfFile);
		}
		catch(IOException ioe) {
			JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
		}
		catch (PdfException de) {
			JOptionPane.showMessageDialog(masterComponent, de.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
        catch (com.itextpdf.io.IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
	}

    public void loadDocument(PdfDocument document) {
        close();
        pdfFile = new PdfFile(document);
        pdfFile.setFilename(document.getDocumentInfo().getTitle());
        pdfFile.setDirectory(null);

        setChanged();
        super.notifyObservers(RupsMenuBar.OPEN);
        readerController.startObjectLoader(pdfFile);
        readerController.addNonObserverTabs(pdfFile);
    }

    /**
     * Reads a File to a byte[]
     *
     * @param file java.io.File
     * @return the file as a byte array
     * @throws IOException
     */
    private byte[] readFileToByteArray(File file) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = null;
        InputStream inputStream = null;
        try {
            byte[] buffer = new byte[4096];
            byteArrayOutputStream = new ByteArrayOutputStream();
            inputStream = new FileInputStream(file);
            int read = 0;
            while ( (read = inputStream.read(buffer)) != -1 ) {
                byteArrayOutputStream.write(buffer, 0, read);
            }
        } finally {
            try {
                if ( byteArrayOutputStream != null )
                    byteArrayOutputStream.close();
            } catch ( IOException e) {
                e.printStackTrace(); // log to console
            }

            try {
                if ( inputStream != null )
                    inputStream.close();
            } catch ( IOException e) {
                e.printStackTrace(); // log to console
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Saves the pdf to the disk
     * @param file java.io.File file to save
     */
    public void saveFile(File file) {
        try {
            if ( !file.getName().endsWith(".pdf") ) {
                file = new File(file.getPath() + ".pdf");
            }

            if ( file.exists() ) {
                int choice = JOptionPane.showConfirmDialog(masterComponent, "File already exists, would you like to overwrite file?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
                if ( choice == JOptionPane.NO_OPTION || choice == JOptionPane.CANCEL_OPTION ) {
                    return;
                }
            }

            ByteArrayOutputStream bos = pdfFile.getByteArrayOutputStream();
            pdfFile.getPdfDocument().setFlushUnusedObjects(false);
            close();
            if (bos != null) {
                bos.writeTo(new FileOutputStream(file));
            }

            JOptionPane.showMessageDialog(masterComponent, "File saved.", "Dialog", JOptionPane.INFORMATION_MESSAGE);


            loadFile(file);
        } catch (PdfException de) {
            JOptionPane.showMessageDialog(masterComponent, de.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        } catch (com.itextpdf.io.IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(masterComponent, ioe.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void close() {
        PdfDocument docToClose = null;
        if (pdfFile != null && pdfFile.getPdfDocument() != null)
            docToClose = pdfFile.getPdfDocument();
        pdfFile = null;
        setChanged();
        super.notifyObservers(RupsMenuBar.CLOSE);
        if(docToClose != null) {
            docToClose.close();
        }
    }

	// tree selection

	/**
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent evt) {
		Object selectednode = readerController.getPdfTree().getLastSelectedPathComponent();
		if (selectednode instanceof PdfTrailerTreeNode) {
			menuBar.update(this, RupsMenuBar.FILE_MENU);
			return;
		}
		if (selectednode instanceof PdfObjectTreeNode) {
			readerController.update(this, selectednode);
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
     * @return pdfFile
     */
    public PdfFile getPdfFile() {
        return pdfFile;
    }
}
