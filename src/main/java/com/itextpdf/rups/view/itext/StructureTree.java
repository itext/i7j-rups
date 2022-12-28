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
package com.itextpdf.rups.view.itext;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.TreeNodeFactory;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.icons.IconTreeCellRenderer;
import com.itextpdf.rups.view.itext.contentstream.MarkedContentInfoGatherer;
import com.itextpdf.rups.view.itext.contentstream.MarkedContentInfo;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;
import com.itextpdf.rups.view.itext.treenodes.StructureTreeNode;

import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

/**
 * A JTree visualizing information about the structure tree of
 * the PDF file (if any).
 */
public class StructureTree extends JTree implements TreeSelectionListener, Observer {

    private static final String BULLET_GO_ICON = "bullet_go.png";

    private static final String CHART_ORG_ICON = "chart_organisation.png";

    /**
     * Nodes in the FormTree correspond with nodes in the main PdfTree.
     */
    protected PdfReaderController controller;

    protected ObjectLoader loader;

    protected boolean loaded = false;

    private transient SwingWorker<TreeModel, Integer> worker;

    private final Map<PdfIndirectReference, Map<Integer, MarkedContentInfo>> mciByPage
            = new ConcurrentHashMap<>();

    public StructureTree(PdfReaderController controller) {
        super();
        this.controller = controller;
        setCellRenderer(new IconTreeCellRenderer());
        setModel(new DefaultTreeModel(new StructureTreeNode()));
        addTreeSelectionListener(this);
    }

    public void update(Observable observable, Object obj) {
        if (observable instanceof PdfReaderController && obj instanceof RupsEvent) {
            final RupsEvent event = (RupsEvent) obj;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                    setLoader(null);
                    setModel(new DefaultTreeModel(new StructureTreeNode()));
                    repaint();
                    break;
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    setLoader((ObjectLoader) event.getContent());
                    break;
                case RupsEvent.OPEN_STRUCTURE_EVENT:
                    if (loader == null || loaded) {
                        break;
                    }
                    loaded = true;
                    setModel(new DefaultTreeModel(new DefaultMutableTreeNode(Language.LOADING.getString())));
                    worker = new TreeUpdateWorker();
                    worker.execute();
                    break;
            }
        }
    }


    /**
     * Recalculates the tree model backing the structure tree view.
     *
     * @return the new tree model
     */
    TreeModel recalculateTreeModel() {
        final TreeNodeFactory factory = loader.getNodes();
        final PdfTrailerTreeNode trailer = controller.getPdfTree().getRoot();
        final PdfObjectTreeNode catalog = factory.getChildNode(trailer, PdfName.Root);
        final PdfObjectTreeNode structuretree =
                factory.getChildNode(catalog, PdfName.StructTreeRoot);
        if (structuretree == null) {
            return new DefaultTreeModel(new StructureTreeNode());
        }
        final StructureTreeNode root = new StructureTreeNode();
        final PdfObjectTreeNode kids = factory.getChildNode(structuretree, PdfName.K);
        loadKids(factory, root, kids, null);
        return new DefaultTreeModel(root);
    }

    private Map<Integer, MarkedContentInfo> indexMarkedContentOnPage(PdfDictionary page) {
        final PdfIndirectReference ref = page.getIndirectReference();
        Map<Integer, MarkedContentInfo> result = this.mciByPage.get(ref);
        if (result != null) {
            return result;
        }
        final MarkedContentInfoGatherer gatherer = new MarkedContentInfoGatherer();
        gatherer.processPageContent(this.loader.getFile().getPdfDocument().getPage(page));
        result = gatherer.getMarkedContentIndex();
        this.mciByPage.put(ref, result);
        return result;
    }

    private static void ensureContentStreamsExpanded(PdfObjectTreeNode objNode, TreeNodeFactory factory) {
        final PdfObjectTreeNode pgNode = factory.getChildNode(objNode, PdfName.Pg);
        factory.expandNode(pgNode);
        final PdfObject contents = ((PdfDictionary) pgNode.getPdfObject())
                .get(PdfName.Contents, false);
        if (contents != null) {
            final PdfObjectTreeNode contentsNode = factory.getChildNode(pgNode, PdfName.Contents);
            factory.expandNode(contentsNode);
            if (contents.isArray()) {
                for (int i = 0; i < contentsNode.getChildCount(); i++) {
                    factory.expandNode((PdfObjectTreeNode) contentsNode.getChildAt(i));
                }
            }

        }
    }

    private static StructureTreeNode attemptMcidNode(
            PdfObjectTreeNode mcidNode, TreeNodeFactory factory,
            Map<Integer, MarkedContentInfo> mciIndex) {
        MarkedContentInfo mci = null;
        final PdfObject mcidObj = mcidNode.getPdfObject();
        if (mcidObj.isNumber() && mciIndex != null) {
            mci = mciIndex.get(((PdfNumber) mcidObj).intValue());
        }
        if (mci == null) {
            // can't make it work -> fall back to default node constructor
            return new StructureTreeNode(mcidNode, BULLET_GO_ICON);
        } else {
            final PdfIndirectReference streamRef = mci.getStreamRef();
            // make the structure tree node jump to the relevant content stream
            return new StructureTreeNode(
                    factory.getNode(streamRef.getObjNumber()), BULLET_GO_ICON,
                    mci.getExtractedText(), mcidNode.getPdfObject()
            );
        }
    }


    private void loadKids(TreeNodeFactory factory, StructureTreeNode structureNode,
            PdfObjectTreeNode objectNode, Map<Integer, MarkedContentInfo> mciIndex) {
        if (objectNode == null) {
            return;
        }
        factory.expandNode(objectNode);
        if (objectNode.isDictionary()) {
            loadDictionaryKids(factory, structureNode, objectNode, mciIndex);
        } else if (objectNode.isArray()) {
            final Enumeration<TreeNode> children = objectNode.children();
            while (children.hasMoreElements()) {
                loadKids(factory, structureNode, (PdfObjectTreeNode) children.nextElement(), mciIndex);
            }
        } else if (objectNode.isIndirectReference()) {
            loadKids(factory, structureNode, (PdfObjectTreeNode) objectNode.getFirstChild(), mciIndex);
        } else {
            structureNode.add(attemptMcidNode(objectNode, factory, mciIndex));
        }
    }

    private void loadDictionaryKids(TreeNodeFactory factory, StructureTreeNode structureNode,
            PdfObjectTreeNode objectNode, Map<Integer, MarkedContentInfo> mciIndex) {
        final PdfName dictType = objectNode.getPdfDictionaryType();
        if (PdfName.MCR.equals(dictType)) {
            final PdfObjectTreeNode mcidNode = factory.getChildNode(objectNode, PdfName.MCID);
            structureNode.add(attemptMcidNode(mcidNode, factory, mciIndex));
            return;
        }
        if (PdfName.OBJR.equals(dictType)) {
            // for objrefs, the tree node to jump to is not the one we're formatting in the tree
            final PdfObjectTreeNode refTarget = factory.getChildNode(objectNode, PdfName.Obj);
            structureNode.add(
                    new StructureTreeNode(refTarget, BULLET_GO_ICON, null, objectNode.getPdfObject()));
            return;
        }
        final PdfDictionary dict = (PdfDictionary) objectNode.getPdfObject();
        Map<Integer, MarkedContentInfo> newMciIndex = null;
        final PdfDictionary page = dict.getAsDictionary(PdfName.Pg);
        if (page != null) {
            final boolean unseen = !mciByPage.containsKey(page.getIndirectReference());
            newMciIndex = indexMarkedContentOnPage(page);
            if (unseen) {
                ensureContentStreamsExpanded(objectNode, factory);
            }
        }
        final StructureTreeNode leaf = new StructureTreeNode(objectNode, CHART_ORG_ICON);
        structureNode.add(leaf);
        final PdfObjectTreeNode kids = factory.getChildNode(objectNode, PdfName.K);
        loadKids(factory, leaf, kids, newMciIndex == null ? mciIndex : newMciIndex);
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (controller == null) {
            return;
        }
        final StructureTreeNode selectednode = (StructureTreeNode) this.getLastSelectedPathComponent();
        if (selectednode == null) {
            return;
        }
        final PdfObjectTreeNode node = selectednode.getCorrespondingPdfObjectNode();
        if (node != null) {
            controller.selectNode(node);
        }
    }

    /**
     * Sets the object loader and cancel any running background loading tasks as appropriate.
     *
     * @param loader the new object loader
     */
    void setLoader(ObjectLoader loader) {
        this.loader = loader;
        if (worker != null) {
            worker.cancel(true);
            worker = null;
        }
        loaded = false;
    }

    private final class TreeUpdateWorker extends SwingWorker<TreeModel, Integer> {
        TreeUpdateWorker() {

        }

        @Override
        protected TreeModel doInBackground() {
            return recalculateTreeModel();
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    final TreeModel model = this.get();
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
    }
}
