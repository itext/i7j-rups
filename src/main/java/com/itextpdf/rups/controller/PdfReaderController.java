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
import com.itextpdf.rups.event.OpenPlainTextEvent;
import com.itextpdf.rups.event.OpenStructureEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.io.listeners.PdfTreeNavigationListener;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.TreeNodeFactory;
import com.itextpdf.rups.view.DebugView;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.contextmenu.ConsoleContextMenu;
import com.itextpdf.rups.view.contextmenu.ContextMenuMouseListener;
import com.itextpdf.rups.view.contextmenu.PdfTreeContextMenu;
import com.itextpdf.rups.view.contextmenu.PdfTreeContextMenuMouseListener;
import com.itextpdf.rups.view.icons.IconTreeNode;
import com.itextpdf.rups.view.itext.*;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import java.awt.Color;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

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

    protected PlainText text;

    private Stack<IconTreeNode> highlights = new Stack<IconTreeNode>();

    /**
     * Constructs the PdfReaderController.
     * This is an Observable object to which all iText related GUI components
     * are added as Observers.
     *
     * @param treeSelectionListener when somebody selects a tree node, this listener listens to the event
     * @param pageSelectionListener when somebody changes a page, this listener changes accordingly
     */
    public PdfReaderController(TreeSelectionListener treeSelectionListener,
                               PageSelectionListener pageSelectionListener, boolean pluginMode) {
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
        text = new PlainText();
        addObserver(text);

        navigationTabs = new JTabbedPane();
        navigationTabs.addTab("Pages", null, new JScrollPane(pages), "Pages");
        navigationTabs.addTab("Outlines", null, new JScrollPane(outlines), "Outlines (Bookmarks)");
        navigationTabs.addTab("Structure", null, new JScrollPane(structure), "Structure tree");
        navigationTabs.addTab("Form", null, new JScrollPane(form), "Interactive Form");
        navigationTabs.addTab("XFA", null, new JScrollPane(form.getXfaTree()), "Tree view of the XFA form");
        navigationTabs.addTab("XRef", null, new JScrollPane(xref), "Cross-reference table");
        navigationTabs.addTab("PlainText", null, new JScrollPane(text), "Plain text representation of the PDF");
        navigationTabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (navigationTabs.getSelectedIndex() != -1) {
                    String title = navigationTabs.getTitleAt(navigationTabs.getSelectedIndex());
                    if ("Structure".equals(title)) {
                        structure.update(PdfReaderController.this, new OpenStructureEvent());
                    } else if ("PlainText".equals(title)) {
                        text.update(PdfReaderController.this, new OpenPlainTextEvent());
                    }
                }
            }
        });

        objectPanel = new PdfObjectPanel(pluginMode);
        addObserver(objectPanel);
        streamPane = new SyntaxHighlightedStreamPane();
        addObserver(streamPane);
        JScrollPane debug = new JScrollPane(DebugView.getInstance().getTextArea());
        editorTabs = new JTabbedPane();
        editorTabs.addTab("Stream", null, streamPane, "Stream");
        editorTabs.addTab("XFA", null, form.getXfaTextArea(), "XFA Form XML file");
        editorTabs.addTab("Debug info", null, debug, "Various debug-specific information");
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
     * is shown in a SyntaxHighlightedStreamPane object).
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
     * @return a SyntaxHighlightedStreamPane
     */
    public SyntaxHighlightedStreamPane getStreamPane() {
        return streamPane;
    }

    /**
     * Forwards updates from the RupsController to the Observers of this class.
     *
     * @param    observable    this should be the RupsController
     * @param    obj    the object that has to be forwarded to the observers of PdfReaderController
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (observable instanceof RupsController && obj instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) obj;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                    nodes = null;
                    setChanged();
                    super.notifyObservers(event);
                    break;
                case RupsEvent.COMPARE_POST_EVENT:
                    highlightChanges((CompareTool.CompareResult) event.getContent());
                    pdfTree.repaint();
                    break;
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    ObjectLoader loader = (ObjectLoader) event.getContent();
                    nodes = loader.getNodes();
                    PdfTrailerTreeNode root = pdfTree.getRoot();
                    root.setTrailer(loader.getFile().getPdfDocument().getTrailer());
                    root.setUserObject("PDF Object Tree (" + loader.getLoaderName() + ")");
                    nodes.expandNode(root);
                    navigationTabs.setSelectedIndex(0);
                    setChanged();
                    super.notifyObservers(event);
                    break;
                case RupsEvent.TREE_NODE_CLICKED_EVENT:
                    PdfObjectTreeNode node = (PdfObjectTreeNode) event.getContent();
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
                    break;
            }
        }
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
        if (object instanceof PdfStream) {
            editorTabs.setSelectedComponent(streamPane);
        } else {
            editorTabs.setSelectedIndex(editorTabs.getComponentCount() - 1);
        }
        objectPanel.render(object);
        streamPane.render(object);
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

    protected void highlightChanges(CompareTool.CompareResult compareResult) {
        clearHighlights();
        for (CompareTool.ObjectPath path : compareResult.getDifferences().keySet()) {
            PdfObjectTreeNode currentNode = null;
            Stack<CompareTool.ObjectPath.IndirectPathItem> indirectPath = (Stack<CompareTool.ObjectPath.IndirectPathItem>)path.getIndirectPath().clone();
            while (!indirectPath.empty()) {
                CompareTool.ObjectPath.IndirectPathItem indirectPathItem = indirectPath.pop();
                currentNode = nodes.getNode(indirectPathItem.getOutObject().getObjNumber());
                if (currentNode != null) {
                    nodes.expandNode(currentNode);
                }
            }
            Stack<CompareTool.ObjectPath.LocalPathItem> localPath = (Stack<CompareTool.ObjectPath.LocalPathItem>)path.getLocalPath().clone();
            currentNode = nodes.getNode(path.getBaseOutObject().getObjNumber());
            while (!localPath.empty() && currentNode != null) {
                nodes.expandNode(currentNode);
                CompareTool.ObjectPath.LocalPathItem item = localPath.pop();
                if (item instanceof CompareTool.ObjectPath.DictPathItem) {
                    currentNode = nodes.getChildNode(currentNode, ((CompareTool.ObjectPath.DictPathItem) item).getKey());
                } else if (item instanceof CompareTool.ObjectPath.ArrayPathItem) {
                    int index = ((CompareTool.ObjectPath.ArrayPathItem) item).getIndex();
                    currentNode = (PdfObjectTreeNode) currentNode.getChildAt(index);
                }
            }
            if (currentNode != null) {
                pdfTree.expandPath(new TreePath(currentNode.getPath()));
                currentNode.setCustomTextColor(Color.ORANGE);
                highlights.add(currentNode);
            }
        }
    }

    protected void clearHighlights() {
        while (!highlights.empty()) {
            highlights.pop().restoreDefaultTextColor();
        }
    }
}