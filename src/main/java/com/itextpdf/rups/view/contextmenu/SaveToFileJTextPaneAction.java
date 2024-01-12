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
package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Language;

import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Custom action to save raw bytes of a stream to a file from the stream panel.
 * This allows selections to be saved as well.
 */
public class SaveToFileJTextPaneAction extends AbstractRupsAction {

    public SaveToFileJTextPaneAction(String name, Component invoker) {
        super(name, invoker);
    }

    public void actionPerformed(ActionEvent event) {
        final Runnable saveRunnable = new Runnable() {
            @Override public void run() {
                // get saving location
                final JFileChooser fileChooser = new JFileChooser();

                final int choice = fileChooser.showSaveDialog(null);
                final String path;

                if (choice == JFileChooser.APPROVE_OPTION) {
                    path = fileChooser.getSelectedFile().getPath();

                    boolean nothingSelected = false;
                    final JTextPane textPane = (JTextPane) invoker;

                    if (textPane.getSelectedText() == null || textPane.getSelectedText().trim().length() == 0) {
                        nothingSelected = true;
                        textPane.selectAll();
                    }

                    BufferedWriter writer = null;

                    try {
                        writer = new BufferedWriter(new FileWriter(path));
                        writer.write(textPane.getSelectedText());

                    } catch (IOException e) { //TODO
                        LoggerHelper.warn(Language.ERROR_WRITING_FILE.getString(), e, getClass());
                    } finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        } catch (IOException e) { //TODO
                            LoggerHelper.error(Language.ERROR_CLOSING_STREAM.getString(), e, getClass());
                        }
                    }

                    if (nothingSelected) {
                        textPane.select(0, 0);
                    }
                }
            }
        };

        SwingUtilities.invokeLater(saveRunnable);
    }
}
