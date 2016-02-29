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
package com.itextpdf.rups.io;

import com.itextpdf.rups.controller.RupsController;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Observable;

/**
 * Allows you to browse the file system and forwards the file
 * to the object that is waiting for you to choose a file.
 */
public class FileChooserAction extends AbstractAction {
	
	/** An object that is expecting the result of the file chooser action. */
	protected Observable observable;
	/** A file filter to apply when browsing for a file. */
	protected FileFilter filter;
	/** Indicates if you're browsing to create a new or an existing file. */
	protected boolean newFile;
	/** The file that was chosen. */
	protected File file;
	
	private File lastSelectedFolder;
	
	/**
	 * Creates a new file chooser action.
	 * @param observable	the object waiting for you to select file
	 * @param caption	a description for the action
	 * @param filter	a filter to apply when browsing
	 * @param newFile	indicates if you should browse for a new or existing file
	 */
	public FileChooserAction(Observable observable, String caption, FileFilter filter, boolean newFile) {
		super(caption);
		this.observable = observable;
		this.filter = filter;
		this.newFile = newFile;
	}
	
	/**
	 * Getter for the file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(lastSelectedFolder);
		fc.setSelectedFile(file);
		
		if (filter != null) {
			fc.setFileFilter(filter);
		}
		int okCancel;
		if (newFile) {
			okCancel = fc.showSaveDialog(((RupsController) observable).getMasterComponent());
		}
		else {
			okCancel = fc.showOpenDialog(((RupsController) observable).getMasterComponent());
		}
		if (okCancel == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			lastSelectedFolder = fc.getCurrentDirectory();
			observable.notifyObservers(this);
        }
	}

    /**
     * Is this FileChooserAction opened to save the file or to open one
     * @return boolean
     */
    public boolean isNewFile() {
        return newFile;
    }

	/** A serial version UID. */
	private static final long serialVersionUID = 2225830878098387118L;

}
