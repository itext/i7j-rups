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

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.kernel.utils.objectpathitems.ArrayPathItem;
import com.itextpdf.kernel.utils.objectpathitems.DictPathItem;
import com.itextpdf.kernel.utils.objectpathitems.IndirectPathItem;
import com.itextpdf.kernel.utils.objectpathitems.LocalPathItem;
import com.itextpdf.kernel.utils.objectpathitems.ObjectPath;
import com.itextpdf.rups.event.NodeAddArrayChildEvent;
import com.itextpdf.rups.event.NodeAddDictChildEvent;
import com.itextpdf.rups.event.NodeDeleteArrayChildEvent;
import com.itextpdf.rups.event.NodeDeleteDictChildEvent;
import com.itextpdf.rups.event.OpenPlainTextEvent;
import com.itextpdf.rups.event.OpenStructureEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.io.listeners.PdfTreeNavigationListener;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfSyntaxParser;
import com.itextpdf.rups.model.TreeNodeFactory;
import com.itextpdf.rups.view.DebugView;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.contextmenu.PdfTreeContextMenu;
import com.itextpdf.rups.view.contextmenu.PdfTreeContextMenuMouseListener;
import com.itextpdf.rups.view.icons.IconTreeNode;
import com.itextpdf.rups.view.itext.FormTree;
import com.itextpdf.rups.view.itext.OutlineTree;
import com.itextpdf.rups.view.itext.PdfObjectPanel;
import com.itextpdf.rups.view.itext.PdfTree;
import com.itextpdf.rups.view.itext.PlainText;
import com.itextpdf.rups.view.itext.StructureTree;
import com.itextpdf.rups.view.itext.SyntaxHighlightedStreamPane;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;

import java.awt.Color;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Controls the components that get their content from iText's PdfReader.
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
     * The PlainText representation.
     */
    protected PlainText text;

    private Stack<IconTreeNode> highlights = new Stack<>();

    private PdfSyntaxParser parser = new PdfSyntaxParser();

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
        pdfTree.setComponentPopupMenu(menu);
        pdfTree.addMouseListener(new PdfTreeContextMenuMouseListener(menu, pdfTree));
        addObserver(pdfTree);
        outlines = new OutlineTree(this);
        addObserver(outlines);
        structure = new StructureTree(this);
        addObserver(structure);
        form = new FormTree(this);
        addObserver(form);
        text = new PlainText();
        addObserver(text);

        navigationTabs = new JTabbedPane();
        final String pagesString = Language.PAGES.getString();
        navigationTabs.addTab(Language.OUTLINES.getString(), null, new JScrollPane(outlines),
                Language.OUTLINES_BOOKMARKS.getString());
        navigationTabs.addTab(Language.STRUCTURE.getString(), null, new JScrollPane(structure),
                Language.STRUCTURE_TREE.getString());
        navigationTabs.addTab(Language.FORM.getString(), null, new JScrollPane(form),
                Language.FORM_INTERACTIVE.getString());
        navigationTabs.addTab(Language.FORM_XFA.getString(), null, new JScrollPane(form.getXfaTree()),
                Language.FORM_XFA_DESCRIPTION.getString());
        navigationTabs.addTab(Language.PLAINTEXT.getString(), null, new JScrollPane(text),
                Language.PLAINTEXT_DESCRIPTION.getString());
        navigationTabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (navigationTabs.getSelectedIndex() != -1) {
                    final String title = navigationTabs.getTitleAt(navigationTabs.getSelectedIndex());

                    if (Language.STRUCTURE.getString().equals(title)) {
                        structure.update(PdfReaderController.this, new OpenStructureEvent());
                    } else if (Language.PLAINTEXT.getString().equals(title)) {
                        text.update(PdfReaderController.this, new OpenPlainTextEvent());
                    } else {
                        // Intentionally empty
                    }
                }
            }
        });

        objectPanel = new PdfObjectPanel();
        addObserver(objectPanel);
        objectPanel.addObserver(this);
        streamPane = new SyntaxHighlightedStreamPane(this);
        addObserver(streamPane);
        JScrollPane debug = new JScrollPane(DebugView.getInstance().getTextArea());
        editorTabs = new JTabbedPane();
        editorTabs.addTab(Language.STREAM.getString(), null, streamPane, Language.STREAM.getString());
        editorTabs.addTab(Language.FORM_XFA.getString(), null, form.getXfaTextArea(),
                Language.FORM_XFA_LONG_FORM.getString());
        editorTabs.addTab(Language.DEBUG_INFO.getString(), null, debug, Language.DEBUG_INFO_DESCRIPTION.getString());
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
    public JPanel getObjectPanel() {
        return objectPanel.getPanel();
    }

    /**
     * Getter for the tabs with the editor windows
     * (to which the Console window will be added).
     *
     * @return the tabs with the editor windows
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

    public PdfSyntaxParser getParser() {
        return parser;
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (observable != null && obj instanceof RupsEvent) {
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
                    root.setUserObject(String.format(Language.PDF_OBJECT_TREE.getString(), loader.getLoaderName()));
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

                        for (KeyListener listener : listeners) {
                            if (listener instanceof PdfTreeNavigationListener) {
                                keyboardNav = ((PdfTreeNavigationListener) listener).isLastActionKeyboardNavigation();
                            }
                        }

                        if (!keyboardNav) {
                            pdfTree.selectNode(node.getAncestor());
                            return;
                        }
                    }
                    render(node);
                    break;
                case RupsEvent.NODE_DELETE_DICT_CHILD_EVENT:
                    deleteTreeNodeDictChild(((NodeDeleteDictChildEvent.Content) event.getContent()).parent,
                            ((NodeDeleteDictChildEvent.Content) event.getContent()).key);
                    break;
                case RupsEvent.NODE_ADD_DICT_CHILD_EVENT:
                    addTreeNodeDictChild(((NodeAddDictChildEvent.Content) event.getContent()).parent,
                            ((NodeAddDictChildEvent.Content) event.getContent()).key,
                            ((NodeAddDictChildEvent.Content) event.getContent()).index);
                    break;
                case RupsEvent.NODE_ADD_ARRAY_CHILD_EVENT:
                    addTreeNodeArrayChild(((NodeAddArrayChildEvent.Content) event.getContent()).parent,
                            ((NodeAddArrayChildEvent.Content) event.getContent()).index);
                    break;
                case RupsEvent.NODE_DELETE_ARRAY_CHILD_EVENT:
                    deleteTreeChild(((NodeDeleteArrayChildEvent.Content) event.getContent()).parent,
                            ((NodeDeleteArrayChildEvent.Content) event.getContent()).index);
                    break;
                case RupsEvent.POST_NEW_INDIRECT_OBJECT_EVENT:
                    nodes.addNewIndirectObject((PdfObject) event.getContent());
                    setChanged();
                    notifyObservers(event);
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
        pdfTree.clearSelection();
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
     *
     * @param node the pdfobject treenode
     */
    public void render(PdfObjectTreeNode node) {
        PdfObject object = node.getPdfObject();
        if (object instanceof PdfStream) {
            editorTabs.setSelectedComponent(streamPane);
        } else {
            editorTabs.setSelectedIndex(editorTabs.getComponentCount() - 1);
        }
        objectPanel.render(node, parser);
        streamPane.render(node);
    }

    /**
     * Selects the row in the pageTable that corresponds with
     * a certain page number.
     *
     * @param pageNumber the page number that needs to be selected
     */
    public void gotoPage(int pageNumber) {
        /**pageNumber--;

        if (pages == null
                || pages.getSelectedRow() == pageNumber) {
            return;
        }

        if (pageNumber < pages.getRowCount()) {
            pages.setRowSelectionInterval(pageNumber, pageNumber);
        }*/
    }

    protected void highlightChanges(CompareTool.CompareResult compareResult) {
        clearHighlights();
        if (compareResult == null) {
            return;
        }
        for (ObjectPath path : compareResult.getDifferences().keySet()) {
            PdfObjectTreeNode currentNode;
            final Stack<IndirectPathItem> indirectPath = path.getIndirectPath();
            while (!indirectPath.empty()) {
                final IndirectPathItem indirectPathItem = indirectPath.pop();
                currentNode = nodes.getNode(indirectPathItem.getOutObject().getObjNumber());
                if (currentNode != null) {
                    nodes.expandNode(currentNode);
                }
            }
            final Stack<LocalPathItem> localPath = path.getLocalPath();
            currentNode = nodes.getNode(path.getBaseOutObject().getObjNumber());
            while (!localPath.empty() && currentNode != null) {
                nodes.expandNode(currentNode);
                final LocalPathItem item = localPath.pop();
                if (item instanceof DictPathItem) {
                    currentNode = nodes.getChildNode(currentNode, ((DictPathItem) item).getKey());
                } else if (item instanceof ArrayPathItem) {
                    int index = ((ArrayPathItem) item).getIndex();
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

    public int deleteTreeNodeDictChild(PdfObjectTreeNode parent, PdfName key) {
        PdfObjectTreeNode child = parent.getDictionaryChildNode(key);
        int index = parent.getIndex(child);
        return deleteTreeChild(parent, index);
    }

    //Returns index of the added child
    public int addTreeNodeDictChild(PdfObjectTreeNode parent, PdfName key, int index) {
        PdfObjectTreeNode child = PdfObjectTreeNode.getInstance((PdfDictionary) parent.getPdfObject(), key);
        return addTreeNodeChild(parent, child, index);
    }

    //Returns index of the added child
    public int addTreeNodeArrayChild(PdfObjectTreeNode parent, int index) {
        PdfObjectTreeNode child = PdfObjectTreeNode.getInstance(((PdfArray) parent.getPdfObject()).get(index, false));
        return addTreeNodeChild(parent, child, index);
    }

    public int deleteTreeChild(PdfObjectTreeNode parent, int index) {
        parent.remove(index);
        ((DefaultTreeModel) pdfTree.getModel()).reload(parent);
        return index;
    }

    //Returns index of the added child
    public int addTreeNodeChild(PdfObjectTreeNode parent, PdfObjectTreeNode child, int index) {
        parent.insert(child, index);
        nodes.expandNode(child);
        ((DefaultTreeModel) pdfTree.getModel()).reload(parent);
        return index;
    }
}
