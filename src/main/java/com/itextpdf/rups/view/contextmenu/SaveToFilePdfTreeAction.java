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
package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.LoggerMessages;
import com.itextpdf.rups.view.itext.PdfTree;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfObject;
//import com.itextpdf.text.pdf.PdfReader;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom action to save raw bytes of a stream to a file from the PdfTree view.
 *
 * @author Michael Demey
 */
public class SaveToFilePdfTreeAction extends AbstractRupsAction {

    /** Serial version uid */
	private static final long serialVersionUID = -4071226227368629514L;
	
	private boolean saveRawBytes;

    public SaveToFilePdfTreeAction(String name) {
        super(name);
    }

    public SaveToFilePdfTreeAction(String name, Component invoker) {
        super(name, invoker);
    }

    public SaveToFilePdfTreeAction(String name, Component invoker, boolean raw) {
        super(name, invoker);
        saveRawBytes = raw;
    }

    public void actionPerformed(ActionEvent event) {

        // get saving location
        JFileChooser fileChooser = new JFileChooser();

        if (saveRawBytes) {
            fileChooser.setDialogTitle(fileChooser.getDialogTitle() + " raw bytes");
        }

        int choice = fileChooser.showSaveDialog(null);
        String path = null;

        if (choice == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().getPath();

            // get the stream
            PdfTree tree = (PdfTree) invoker;
            TreeSelectionModel selectionModel = tree.getSelectionModel();
            TreePath[] paths = selectionModel.getSelectionPaths();
            PdfObjectTreeNode lastPath = (PdfObjectTreeNode) paths[0].getLastPathComponent();
            PdfObject object = lastPath.getPdfObject();
            PdfStream stream = (PdfStream) object;

            // get the bytes and write away
            try {
                byte[] array = null;

                if (saveRawBytes) {
                    array = stream.getBytes(false);
                } else {
                    array = stream.getBytes();
                }

                FileOutputStream fos = new FileOutputStream(path);
                fos.write(array);
                fos.close();
            } catch (IOException e) { // TODO : Catch this exception properly
                LoggerHelper.error(LoggerMessages.WRITING_FILE_ERROR, e, getClass());
            } catch (com.itextpdf.io.IOException e) { // TODO : Catch this exception properly
                LoggerHelper.error(LoggerMessages.GETTING_PDF_STREAM_BYTES_ERROR, e, getClass());
            }
        }
    }
}
