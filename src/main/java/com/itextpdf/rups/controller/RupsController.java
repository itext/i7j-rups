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

import com.itextpdf.rups.RupsConfiguration;
import com.itextpdf.rups.event.AllFilesClosedEvent;
import com.itextpdf.rups.event.OpenFileEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.RupsTabbedPane;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * This class controls all the GUI components that are shown in
 * the RUPS application: the menu bar, the panels,...
 */
public class RupsController extends Observable
        implements Observer, IRupsController {

    private final RupsTabbedPane rupsTabbedPane;

    private final Dimension dimension;

    /**
     * Constructs the GUI components of the RUPS application.
     *
     * @param dimension      The dimension of the screen
     * @param rupsTabbedPane The main tabbed pane
     */
    public RupsController(Dimension dimension, RupsTabbedPane rupsTabbedPane) {
        this.rupsTabbedPane = rupsTabbedPane;

        this.dimension = dimension;
    }

    /**
     * Getter for the master component.
     *
     * @return the master component
     */
    @Override
    public Component getMasterComponent() {
        return rupsTabbedPane.getJTabbedPane();
    }

    @Override
    public final void update(Observable o, Object arg) {
        //Events that have come from non observable classes: ObjectLoader and FileChooserAction
        if (o == null && arg instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) arg;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                    this.closeCurrentFile();
                    break;
                case RupsEvent.OPEN_FILE_EVENT:
                    this.openNewFile((File) event.getContent());
                    break;
                case RupsEvent.COMPARE_WITH_FILE_EVENT:
                    break;
                case RupsEvent.SAVE_TO_FILE_EVENT:
                    this.rupsTabbedPane.saveCurrentFile((File) event.getContent());
                    break;
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                default:
                    setChanged();
                    super.notifyObservers(event);
                    break;
            }
        } else {
            setChanged();
            super.notifyObservers(arg);
        }
    }

    @Override
    public final void closeCurrentFile() {
        final boolean lastOne = this.rupsTabbedPane.closeCurrentFile();
        if (lastOne) {
            this.update(this, new AllFilesClosedEvent());
        }
    }

    @Override
    public final PdfFile getCurrentFile() {
        return this.rupsTabbedPane.getCurrentFile();
    }

    @Override
    public final void openNewFile(File file) {
        if (file != null) {
            if (!RupsConfiguration.INSTANCE.canOpenDuplicateFiles() && this.rupsTabbedPane.isFileAlreadyOpen(file)) {
                LoggerHelper.info(Language.DUPLICATE_FILES_OFF.getString(), this.getClass());
                return;
            }

            this.rupsTabbedPane.openNewFile(file, false);
            this.update(this, new OpenFileEvent(file));
        }
    }
}
