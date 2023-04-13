/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.IRupsController;
import com.itextpdf.rups.io.filters.PdfFilter;
import com.itextpdf.rups.model.LoggerHelper;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Drag and drop implementation for RUPS.
 */
public class RupsDropTarget extends DropTarget {
    /**
     * The controller instance that controls the application.
     */
    private final IRupsController rupsController;

    /**
     * Creates a RupsDropTarget
     *
     * @param rupsController the controller instance that controls the application
     */
    public RupsDropTarget(IRupsController rupsController) {
        this.rupsController = rupsController;
    }

    @Override
    public final synchronized void drop(DropTargetDropEvent dtde) {
        if (dtde != null) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

            final List<File> files = extractFilesFromTransferable(dtde.getTransferable());

            for (final File file : files) {
                if (PdfFilter.INSTANCE.accept(file)) {
                    this.rupsController.openNewFile(file);
                }
            }

            dtde.dropComplete(true);
        }
    }

    /**
     * The Transferable contains the dropped file(s). Get them out!
     *
     * @param transferable The dragged object(s)
     *
     * @return a list of Files, empty on errors or if the dragged objects aren't supported
     */
    List<File> extractFilesFromTransferable(Transferable transferable) {
        List<File> files = new ArrayList<>();

        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                LoggerHelper.warn(String.format(Language.ERROR_DRAG_AND_DROP.getString(), e.getMessage()), getClass());
            }
        } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            // fix for Linux
            try {
                final String urls = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                files = new ArrayList<>();

                final StringTokenizer tokens = new StringTokenizer(urls);
                while (tokens.hasMoreTokens()) {
                    final String urlString = tokens.nextToken();
                    final URL url = new URL(urlString);
                    files.add(new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name())));
                }
            } catch (UnsupportedFlavorException | IOException e) {
                LoggerHelper.warn(String.format(Language.ERROR_DRAG_AND_DROP.getString(), e.getMessage()), getClass());
            }
        } else {
            // empty body
        }

        return files;
    }
}
