/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
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

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.TreeNodeFactory;
import com.itextpdf.rups.view.icons.IconTreeCellRenderer;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;
import com.itextpdf.rups.view.itext.treenodes.StructureTreeNode;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

/**
 * A JTree visualizing information about the structure tree of
 * the PDF file (if any).
 */
public class StructureTree extends JTree implements TreeSelectionListener, Observer {

    /**
     * Nodes in the FormTree correspond with nodes in the main PdfTree.
     */
    protected PdfReaderController controller;

    protected ObjectLoader loader;

    protected boolean loaded = false;

    protected SwingWorker<TreeModel, Integer> worker;

    public StructureTree(PdfReaderController controller) {
        super();
        this.controller = controller;
        setCellRenderer(new IconTreeCellRenderer());
        setModel(new DefaultTreeModel(new StructureTreeNode()));
        addTreeSelectionListener(this);
    }

    public void update(Observable observable, Object obj) {
        if (observable instanceof PdfReaderController && obj instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) obj;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                    loader = null;
                    if (worker != null) {
                        worker.cancel(true);
                        worker = null;
                    }
                    setModel(new DefaultTreeModel(new StructureTreeNode()));
                    repaint();
                    loaded = false;
                    break;
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    loader = (ObjectLoader) event.getContent();
                    if (worker != null) {
                        worker.cancel(true);
                        worker = null;
                    }
                    loaded = false;
                    break;
                case RupsEvent.OPEN_STRUCTURE_EVENT:
                    if (loader == null || loaded) {
                        break;
                    }
                    loaded = true;
                    setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Loading...")));
                    worker = new SwingWorker<TreeModel, Integer>() {
                        @Override
                        protected TreeModel doInBackground() {
                            TreeNodeFactory factory = loader.getNodes();
                            PdfTrailerTreeNode trailer = controller.getPdfTree().getRoot();
                            PdfObjectTreeNode catalog = factory.getChildNode(trailer, PdfName.Root);
                            PdfObjectTreeNode structuretree = factory.getChildNode(catalog, PdfName.StructTreeRoot);
                            if (structuretree == null) {
                                return new DefaultTreeModel(new StructureTreeNode());
                            }
                            StructureTreeNode root = new StructureTreeNode();
                            PdfObjectTreeNode kids = factory.getChildNode(structuretree, PdfName.K);
                            loadKids(factory, root, kids);
                            return new DefaultTreeModel(root);
                        }

                        @Override
                        protected void done() {
                            try {
                                if (!isCancelled()) {
                                    TreeModel model = this.get();
                                    StructureTree.this.setModel(model);
                                }
                            } catch (InterruptedException any) {
                                StructureTree.this.setModel(new DefaultTreeModel(new StructureTreeNode()));
                                Thread.currentThread().interrupt();
                            } catch (ExecutionException any) {
                                StructureTree.this.setModel(new DefaultTreeModel(new StructureTreeNode()));
                            }
                            super.done();
                        }
                    };
                    worker.execute();
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadKids(TreeNodeFactory factory, StructureTreeNode structure_node, PdfObjectTreeNode object_node) {
        if (object_node == null) {
            return;
        }
        factory.expandNode(object_node);
        if (object_node.isDictionary()) {
            PdfDictionary dict = (PdfDictionary) object_node.getPdfObject();
            if (PdfName.MCR.equals(dict.getAsName(PdfName.Type))) {
                structure_node.add(new StructureTreeNode(factory.getChildNode(object_node, PdfName.MCID), "bullet_go.png"));
                return;
            }
            if (PdfName.OBJR.equals(dict.getAsName(PdfName.Type))) {
                structure_node.add(new StructureTreeNode(factory.getChildNode(object_node, PdfName.Obj), "bullet_go.png"));
                return;
            }
            StructureTreeNode leaf = new StructureTreeNode(object_node, "chart_organisation.png");
            structure_node.add(leaf);
            PdfObjectTreeNode kids = factory.getChildNode(object_node, PdfName.K);
            loadKids(factory, leaf, kids);
        } else if (object_node.isArray()) {
            Enumeration<TreeNode> children = object_node.children();
            while (children.hasMoreElements()) {
                loadKids(factory, structure_node, (PdfObjectTreeNode) children.nextElement());
            }
        } else if (object_node.isIndirectReference()) {
            loadKids(factory, structure_node, (PdfObjectTreeNode) object_node.getFirstChild());
        } else {
            StructureTreeNode leaf = new StructureTreeNode(object_node, "bullet_go.png");
            structure_node.add(leaf);
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (controller == null)
            return;
        StructureTreeNode selectednode = (StructureTreeNode) this.getLastSelectedPathComponent();
        if (selectednode == null)
            return;
        PdfObjectTreeNode node = selectednode.getCorrespondingPdfObjectNode();
        if (node != null)
            controller.selectNode(node);
    }
}
