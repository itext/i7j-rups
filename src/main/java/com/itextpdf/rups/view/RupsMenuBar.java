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
package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.io.*;
import com.itextpdf.rups.io.filters.PdfFilter;
import com.itextpdf.rups.model.PdfFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class RupsMenuBar extends JMenuBar implements Observer {

	/** Caption for the file menu. */
	public static final String FILE_MENU = "File";
	/** Caption for "Open file". */
	public static final String OPEN = "Open";
    /** Caption for "Open in PDF Viewer". */
    public static final String OPENINVIEWER = "Open in PDF Viewer";
    /** Caption for "Close file". */
	public static final String CLOSE = "Close";
    /** Caption for "Save as..." */
    public static final String SAVE_AS = "Save as...";
	/** Caption for the help menu. */
	public static final String HELP_MENU = "Help";
	/** Caption for "Help about". */
	public static final String ABOUT = "About";

	public static final String COMPARE_WITH = "Compare with...";

	public static final String NEW_INDIRECT = "Add new indirect object";
	/**
	 * Caption for "Help versions".
	 * @since iText 5.0.0 (renamed from VERSIONS)
	 */
	public static final String VERSION = "Version";
	
	/** The RupsController object. */
	protected RupsController controller;
	/** The action needed to open a file. */
	protected FileOpenAction fileOpenAction;
	/** The action needed to save a file. */
	protected FileSaveAction fileSaverAction;
	/** The HashMap with all the actions. */
	protected HashMap<String, JMenuItem> items;

	protected FileCompareAction fileCompareAction;
	/**
	 * Creates a JMenuBar.
	 * @param controller the controller to which this menu bar is added
	 */
	public RupsMenuBar(RupsController controller) {
		this.controller = controller;
		items = new HashMap<String, JMenuItem>();
		fileOpenAction = new FileOpenAction(this.controller, PdfFilter.INSTANCE, this.controller.getMasterComponent());
		fileSaverAction = new FileSaveAction(this.controller, PdfFilter.INSTANCE, this.controller.getMasterComponent());
		fileCompareAction = new FileCompareAction(this.controller, PdfFilter.INSTANCE, this.controller.getMasterComponent());
		MessageAction message = new MessageAction();
		JMenu file = new JMenu(FILE_MENU);
		addItem(file, OPEN, fileOpenAction, KeyStroke.getKeyStroke('O', KeyEvent.CTRL_DOWN_MASK));
		addItem(file, CLOSE, new FileCloseAction(this.controller), KeyStroke.getKeyStroke('W', KeyEvent.CTRL_DOWN_MASK));
        addItem(file, SAVE_AS, fileSaverAction, KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK));
        addItem(file, COMPARE_WITH, fileCompareAction, KeyStroke.getKeyStroke('Q', KeyEvent.CTRL_DOWN_MASK));
		file.addSeparator();
        addItem(file, OPENINVIEWER, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        PdfFile pdfFile = RupsMenuBar.this.controller.getPdfFile();
                        if ( pdfFile != null ) {
                            if ( pdfFile.getDirectory() != null ) {
                                File myFile = new File(pdfFile.getDirectory(), pdfFile.getFilename());
                                Desktop.getDesktop().open(myFile);
                            }
                        }
                    } catch (IOException ex) {
                        // no application registered for PDFs
                    }
                }
            }
        }, KeyStroke.getKeyStroke('E', KeyEvent.CTRL_DOWN_MASK));
        addItem(file, NEW_INDIRECT, new NewIndirectPdfObjectDialog.AddNewIndirectAction(controller), KeyStroke.getKeyStroke('N', KeyEvent.CTRL_DOWN_MASK));
        add(file);
        add(Box.createGlue());
        JMenu help = new JMenu(HELP_MENU);
        addItem(help, ABOUT, message);
        addItem(help, VERSION, message);
        add(help);
		enableItems(false);
	}
	
	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object obj) {
		if (observable instanceof RupsController && obj instanceof RupsEvent) {
			RupsEvent event = (RupsEvent) obj;
			switch (event.getType()) {
				case RupsEvent.CLOSE_DOCUMENT_EVENT:
					enableItems(false);
					break;
				case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
					enableItems(true);
					break;
				case RupsEvent.ROOT_NODE_CLICKED_EVENT:
					fileOpenAction.actionPerformed(null);
			}
		}
	}
	
	/**
	 * Create an item with a certain caption and a certain action,
	 * then add the item to a menu.
	 * @param menu	the menu to which the item has to be added
	 * @param caption	the caption of the item
	 * @param action	the action corresponding with the caption
	 */
	protected void addItem(JMenu menu, String caption, ActionListener action) {
        addItem(menu, caption, action, null);
	}

    protected void addItem(JMenu menu, String caption, ActionListener action, KeyStroke keyStroke) {
        JMenuItem item = new JMenuItem(caption);
        item.addActionListener(action);
        if ( keyStroke != null ) {
            item.setAccelerator(keyStroke);
        }
        menu.add(item);
        items.put(caption, item);
    }
	
	/**
	 * Enables/Disables a series of menu items.
	 * @param enabled	true for enabling; false for disabling
	 */
	protected void enableItems(boolean enabled) {
		enableItem(CLOSE, enabled);
        enableItem(SAVE_AS, enabled);
        enableItem(OPENINVIEWER, enabled);
		enableItem(COMPARE_WITH, enabled);
        enableItem(NEW_INDIRECT, enabled);
	}
	
	/**
	 * Enables/disables a specific menu item
	 * @param caption	the caption of the item that needs to be enabled/disabled
	 * @param enabled	true for enabling; false for disabling
	 */
	protected void enableItem(String caption, boolean enabled) {
		items.get(caption).setEnabled(enabled);
	}
	
	/** A Serial Version UID. */
	private static final long serialVersionUID = 6403040037592308742L;
}