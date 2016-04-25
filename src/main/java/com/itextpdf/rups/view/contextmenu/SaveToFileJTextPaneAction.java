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

import com.itextpdf.rups.model.LoggerMessages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom action to save raw bytes of a stream to a file from the stream panel.
 * This allows selections to be saved as well.
 *
 * @author Michael Demey
 */
public class SaveToFileJTextPaneAction extends AbstractRupsAction {

    /** Serial version uid. */
	private static final long serialVersionUID = -5984892284970574660L;

	public SaveToFileJTextPaneAction(String name) {
        super(name);
    }

    public SaveToFileJTextPaneAction(String name, Component invoker) {
        super(name, invoker);
    }

    public void actionPerformed(ActionEvent event) {

        // get saving location
        JFileChooser fileChooser = new JFileChooser();

        int choice = fileChooser.showSaveDialog(null);
        String path = null;

        if (choice == JFileChooser.APPROVE_OPTION) {
            path = fileChooser.getSelectedFile().getPath();


            boolean nothingSelected = false;
            JTextPane textPane = (JTextPane) invoker;

            if (textPane.getSelectedText() == null || textPane.getSelectedText().trim().length() == 0) {
                nothingSelected = true;
                textPane.selectAll();
            }

            BufferedWriter writer = null;

            try {
                writer = new BufferedWriter(new FileWriter(path));
                writer.write(textPane.getSelectedText());

            } catch (IOException e) { //TODO
                Logger logger = LoggerFactory.getLogger(SaveToFileJTextPaneAction.class);
                logger.warn(LoggerMessages.WRITING_FILE_ERROR, e);
            } finally {
                try {
                    if (writer != null)
                        writer.close();
                } catch (IOException e) { //TODO
                    Logger logger = LoggerFactory.getLogger(SaveToFileJTextPaneAction.class);
                    logger.warn(LoggerMessages.CLOSING_STREAM_ERROR, e);
                }
            }

            if (nothingSelected) {
                textPane.select(0, 0);
            }
        }
    }
}