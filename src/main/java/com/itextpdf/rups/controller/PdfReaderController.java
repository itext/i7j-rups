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

import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.rups.io.listeners.PdfTreeNavigationListener;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.rups.model.TreeNodeFactory;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.RupsMenuBar;
import com.itextpdf.rups.view.contextmenu.PdfTreeContextMenu;
import com.itextpdf.rups.view.contextmenu.PdfTreeContextMenuMouseListener;
import com.itextpdf.rups.view.itext.*;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import java.awt.Color;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.TreeSelectionListener;

/**
 * Controls the GUI components that get their content from iText's PdfReader.
 */
public class PdfReaderController extends Observable implements Observer {

    /**
     * Treeview of the PDF file.
     */
    protected PdfTree pdfTree;
    /**
     * Tabbed Pane containing other components.
     */
    protected JTabbedPane navigationTabs;
    /**
     * JTable with all the pages and their labels.
     */
    protected PagesTable pages;
    /**
     * Treeview of the outlines.
     */
    protected OutlineTree outlines;
    /**
     * Treeview of the structure.
     */
    protected StructureTree structure;
    /**
     * Treeview of the form.
     */
    protected FormTree form;
    /**
     * JTable corresponding with the CrossReference table.
     */
    protected XRefTable xref;
    /**
     * A panel that will show PdfObjects.
     */
    protected PdfObjectPanel objectPanel;
    /**
     * Tabbed Pane containing other components.
     */
    protected JTabbedPane editorTabs;
    /**
     * A panel that will show a stream.
     */
    protected SyntaxHighlightedStreamPane streamPane;

    /**
     * The factory producing tree nodes.
     */
    protected TreeNodeFactory nodes;

    /**
     * Constructs the PdfReaderController.
     * This is an Observable object to which all iText related GUI components
     * are added as Observers.
     *
     * @param treeSelectionListener when somebody selects a tree node, this listener listens to the event
     * @param pageSelectionListener when somebody changes a page, this listener changes accordingly
     */
    public PdfReaderController(TreeSelectionListener treeSelectionListener,
                               PageSelectionListener pageSelectionListener) {
        pdfTree = new PdfTree();

        pdfTree.addTreeSelectionListener(treeSelectionListener);
        JPopupMenu menu = PdfTreeContextMenu.getPopupMenu(pdfTree);
        pdfTree.add(menu);
        pdfTree.addMouseListener(new PdfTreeContextMenuMouseListener(menu, pdfTree));
        addObserver(pdfTree);

        pages = new PagesTable(this, pageSelectionListener);
        addObserver(pages);
        outlines = new OutlineTree(this);
        addObserver(outlines);
        structure = new StructureTree(this);
        addObserver(structure);
        form = new FormTree(this);
        addObserver(form);
        xref = new XRefTable(this);
        addObserver(xref);

        navigationTabs = new JTabbedPane();
        navigationTabs.addTab("Pages", null, new JScrollPane(pages), "Pages");
        navigationTabs.addTab("Outlines", null, new JScrollPane(outlines), "Outlines (Bookmarks)");
        navigationTabs.addTab("Structure", null, new JScrollPane(structure), "Structure tree");
        navigationTabs.addTab("Form", null, new JScrollPane(form), "Interactive Form");
        navigationTabs.addTab("XFA", null, new JScrollPane(form.getXfaTree()), "Tree view of the XFA form");
        navigationTabs.addTab("XRef", null, new JScrollPane(xref), "Cross-reference table");

        objectPanel = new PdfObjectPanel();
        addObserver(objectPanel);
        streamPane = new SyntaxHighlightedStreamPane();
        addObserver(streamPane);
        editorTabs = new JTabbedPane();
        editorTabs.addTab("Stream", null, streamPane, "Stream");
        editorTabs.addTab("XFA", null, form.getXfaTextArea(), "XFA Form XML file");
    }

    /**
     * Getter for the PDF Tree.
     *
     * @return the PdfTree object
     */
    public PdfTree getPdfTree() {
        return pdfTree;
    }

    /**
     * Getter for the tabs that allow you to navigate through
     * the PdfTree quickly (pages, form, outlines, xref table).
     *
     * @return a JTabbedPane
     */
    public JTabbedPane getNavigationTabs() {
        return navigationTabs;
    }

    /**
     * Getter for the panel that will show the contents
     * of a PDF Object (except for PdfStreams: only the
     * Stream Dictionary will be shown; the content stream
     * is shown in a StreamTextArea object).
     *
     * @return the PdfObjectPanel
     */
    public PdfObjectPanel getObjectPanel() {
        return objectPanel;
    }

    /**
     * Getter for the tabs with the editor windows
     * (to which the Console window will be added).
     */
    public JTabbedPane getEditorTabs() {
        return editorTabs;
    }

    /**
     * Getter for the object that holds the TextPane
     * with the content stream of a PdfStream object.
     *
     * @return a StreamTextArea
     */
    public SyntaxHighlightedStreamPane getStreamPane() {
        return streamPane;
    }

    /**
     * Starts loading the PDF Objects in background.
     *
     * @param file the wrapper object that holds the PdfReader as member variable
     */
    public void startObjectLoader(PdfFile file) {
        setChanged();
        notifyObservers();
        setChanged();
        new ObjectLoader(this, file.getPdfDocument(), file.getFilename());
    }

    /**
     * The GUI components that show the internals of a PDF file,
     * can only be shown if all objects are loaded into the
     * IndirectObjectFactory using the ObjectLoader.
     * As soon as this is done, the GUI components are notified.
     *
     * @param    obj    in this case the Object should be an ObjectLoader
     * @see java.util.Observable#notifyObservers(java.lang.Object)
     */
    @Override
    public void notifyObservers(Object obj) {
        if (obj instanceof ObjectLoader) {
            ObjectLoader loader = (ObjectLoader) obj;
            nodes = loader.getNodes();
            PdfTrailerTreeNode root = pdfTree.getRoot();
            root.setTrailer(loader.getDocument().getTrailer());
            root.setUserObject("PDF Object Tree (" + loader.getLoaderName() + ")");
            nodes.expandNode(root);
        }
        if (obj instanceof CompareTool.CompareResult) {
            highlightChanges((CompareTool.CompareResult) obj);
            setChanged();
        }
        super.notifyObservers(obj);
    }

    /**
     * Selects a node in the PdfTree.
     *
     * @param node a node in the PdfTree
     */
    public void selectNode(PdfObjectTreeNode node) {
        pdfTree.selectNode(node);
    }

    /**
     * Selects a node in the PdfTree.
     *
     * @param objectNumber a number of a node in the PdfTree
     */
    public void selectNode(int objectNumber) {
        selectNode(nodes.getNode(objectNumber));
    }

    /**
     * Renders the syntax of a PdfObject in the objectPanel.
     * If the object is a PDF Stream, then the stream is shown
     * in the streamArea too.
     */
    public void render(PdfObject object) {
        objectPanel.render(object);
        streamPane.render(object);
        if (object instanceof PdfStream) {
            editorTabs.setSelectedComponent(streamPane);
        } else {
            editorTabs.setSelectedIndex(editorTabs.getComponentCount() - 1);
        }
    }

    /**
     * Selects the row in the pageTable that corresponds with
     * a certain page number.
     *
     * @param pageNumber the page number that needs to be selected
     */
    public void gotoPage(int pageNumber) {
        pageNumber--;
        if (pages == null || pages.getSelectedRow() == pageNumber)
            return;
        if (pageNumber < pages.getRowCount())
            pages.setRowSelectionInterval(pageNumber, pageNumber);
    }

    /**
     * Forwards updates from the RupsController to the Observers of this class.
     *
     * @param    observable    this should be the RupsController
     * @param    obj    the object that has to be forwarded to the observers of PdfReaderController
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (RupsMenuBar.CLOSE.equals(obj)) {
            setChanged();
            notifyObservers(null);


            if (navigationTabs.indexOfTab("PlainText") != -1) {
                navigationTabs.removeTabAt(navigationTabs.indexOfTab("PlainText"));
            }

            nodes = null;
        }
        if (obj instanceof PdfObjectTreeNode) {
            PdfObjectTreeNode node = (PdfObjectTreeNode) obj;
            nodes.expandNode(node);

            if (node.isRecursive()) {
                boolean keyboardNav = false;

                KeyListener[] listeners = pdfTree.getKeyListeners();

                for (int i = 0; i < listeners.length; i++) {
                    KeyListener listener = listeners[i];
                    if (listener instanceof PdfTreeNavigationListener) {
                        keyboardNav = ((PdfTreeNavigationListener) listener).isLastActionKeyboardNavigation();
                    }
                }

                if (!keyboardNav) {
                    pdfTree.selectNode(node.getAncestor());
                    return;
                }
            }
            render(node.getPdfObject());
        }
        if (obj instanceof CompareTool.CompareResult) {
            setChanged();
            notifyObservers(obj);
        }
    }

    /**
     * Adds tabs that don't need to be an observer. Also removes them when they are present to avoid duplication.
     *
     * @param file
     */
    public void addNonObserverTabs(PdfFile file) {
        if (navigationTabs.indexOfTab("PlainText") != -1)
            navigationTabs.remove(navigationTabs.indexOfTab("PlainText"));
        navigationTabs.addTab("PlainText", null, new JScrollPane(new JTextArea(file.getRawContent())), "Plain text representation of the PDF");
    }

    protected void highlightChanges(CompareTool.CompareResult compareResult) {
        for (CompareTool.ObjectPath path : compareResult.getDifferences().keySet()) {
            PdfObjectTreeNode currentNode = nodes.getNode(path.getBaseOutObject().getObjNumber());
            while (!path.getPath().empty() && currentNode != null) {
                nodes.expandNode(currentNode);
                CompareTool.ObjectPath.PathItem item = path.getPath().pop();
                if (item instanceof CompareTool.ObjectPath.DictPathItem) {
                    currentNode = nodes.getChildNode(currentNode, ((CompareTool.ObjectPath.DictPathItem) item).getKey());
                } else if (item instanceof CompareTool.ObjectPath.ArrayPathItem) {
                    int index = ((CompareTool.ObjectPath.ArrayPathItem) item).getIndex();
                    currentNode = (PdfObjectTreeNode) currentNode.getChildAt(index);
                }
            }
            if (currentNode != null) {
                currentNode.setCustomTextColor(Color.ORANGE);
            }
        }
    }
}
