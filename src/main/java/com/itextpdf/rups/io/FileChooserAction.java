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
import com.itextpdf.rups.event.RupsEvent;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Allows you to browse the file system and forwards the file
 * to the object that is waiting for you to choose a file.
 */
public abstract class FileChooserAction extends AbstractAction {
	
	/** An object that is expecting the result of the file chooser action. */
	protected Observer observer;
	/** A file filter to apply when browsing for a file. */
	protected FileFilter filter;
	/** The file that was chosen. */
	protected File file;
	/** A parent Component for chooser dialog */
	protected Component parent;

	protected JFileChooser fileChooser;
	
	private static File lastSelectedFolder;
	
	/**
	 * Creates a new file chooser action.
	 * @param observer	the object waiting for you to select file
	 * @param caption	a description for the action
	 * @param filter	a filter to apply when browsing
	 * @param parent    a parent Component for chooser dialog
	 */
	public FileChooserAction(Observer observer, String caption, FileFilter filter, Component parent) {
		super(caption);
		this.observer = observer;
		this.filter = filter;
		this.parent = parent;
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
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(lastSelectedFolder);
		fileChooser.setSelectedFile(file);
		
		if (filter != null) {
			fileChooser.setFileFilter(filter);
		}
		int okCancel = showDialog();
		if (okCancel == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			lastSelectedFolder = fileChooser.getCurrentDirectory();
			observer.update(null, getEvent());
        }
	}

	protected abstract int showDialog();

	protected abstract RupsEvent getEvent();

	/** A serial version UID. */
	private static final long serialVersionUID = 2225830878098387118L;

}
