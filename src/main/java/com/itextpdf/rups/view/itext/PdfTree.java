/*
    * $Id$

    This file is part of the iText (R) project.
    Copyright (c) 2007-2019 iText Group NV
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
package com.itextpdf.rups.view.itext;

import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.io.listeners.PdfTreeExpansionListener;
import com.itextpdf.rups.io.listeners.PdfTreeNavigationListener;
import com.itextpdf.rups.view.icons.IconTreeCellRenderer;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Observable;
import java.util.Observer;

/**
 * A JTree that shows the object hierarchy of a PDF document.
 */
public class PdfTree extends JTree implements Observer {

    /**
     * The root of the PDF tree.
     */
    protected PdfTrailerTreeNode root;

    /**
     * Constructs a PDF tree.
     */
    public PdfTree() {
        super();
        root = new PdfTrailerTreeNode();
        PdfTreeNavigationListener listener = new PdfTreeNavigationListener();
        addKeyListener(listener);
        addMouseListener(listener);
        setCellRenderer(new IconTreeCellRenderer());
        addTreeExpansionListener(new PdfTreeExpansionListener());
        setModel(new DefaultTreeModel(root));
        repaint();
    }

    /**
     * Getter for the root node
     *
     * @return the PDF Trailer node
     */
    public PdfTrailerTreeNode getRoot() {
        return root;
    }

    /**
     * Updates the PdfTree when a file is closed or when a ObjectLoader
     * has finished loading objects.
     *
     * @param observable the Observable class that started the update
     * @param obj        the object that has all the updates
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (observable instanceof PdfReaderController && obj instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) obj;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                    root = new PdfTrailerTreeNode();
                    break;
            }
        }
        setModel(new DefaultTreeModel(root));
        repaint();
    }

    /**
     * Select a specific node in the tree.
     * Typically this method will be called from a different tree,
     * such as the pages, outlines or form tree.
     *
     * @param    node    the node that has to be selected
     */
    public void selectNode(PdfObjectTreeNode node) {
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            setSelectionPath(path);
            scrollPathToVisible(path);
        }
    }

    /**
     * a serial version UID
     */
    private static final long serialVersionUID = 7545804447512085734L;

}
