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
package com.itextpdf.rups.model;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfPagesTreeNode;

import java.util.ArrayList;

/**
 * A factory that creates TreeNode objects corresponding with PDF objects.
 */
public class TreeNodeFactory {

    /**
     * The factory that can produce all indirect objects.
     */
    protected IndirectObjectFactory objects;
    /**
     * An list containing the nodes of every indirect object.
     */
    protected ArrayList<PdfObjectTreeNode> nodes = new ArrayList<PdfObjectTreeNode>();

    /**
     * Creates a factory that can produce TreeNode objects
     * corresponding with PDF objects.
     *
     * @param objects a factory that can produce all the indirect objects of a PDF file.
     */
    public TreeNodeFactory(IndirectObjectFactory objects) {
        this.objects = objects;
        for (int i = 0; i < objects.size(); i++) {
            int ref = objects.getRefByIndex(i);
            nodes.add(PdfObjectTreeNode.getInstance(PdfNull.PDF_NULL, ref));
        }
    }

    /**
     * Gets a TreeNode for an indirect objects.
     *
     * @param ref the reference number of the indirect object.
     * @return the TreeNode representing the PDF object
     */
    public PdfObjectTreeNode getNode(int ref) {
        int idx = objects.getIndexByRef(ref);
        PdfObjectTreeNode node = nodes.get(idx);
        if (node.getPdfObject().isNull()) {
            node = PdfObjectTreeNode.getInstance(objects.loadObjectByReference(ref), ref);
            nodes.set(idx, node);
        }
        return node;
    }

    protected void associateIfIndirect(PdfObjectTreeNode node) {
        PdfIndirectReference ref = null;
        if (node != null && node.getPdfObject() != null) {
            ref = node.getPdfObject().getIndirectReference();
        }
        if (ref != null) {
            int idx = objects.getIndexByRef(ref.getObjNumber());
            nodes.set(idx, node);
        }
    }

    /**
     * Creates the Child TreeNode objects for a PDF object TreeNode.
     *
     * @param node the parent node
     */
    public void expandNode(PdfObjectTreeNode node) {
        if (node.getChildCount() > 0) return;
        PdfObject object = node.getPdfObject();
        PdfObjectTreeNode leaf;

        switch (object.getType()) {
            case PdfObject.INDIRECT_REFERENCE:
                PdfIndirectReference ref = (PdfIndirectReference) object;
                leaf = getNode(ref.getObjNumber());
                addNodes(node, leaf);
                if (leaf instanceof PdfPagesTreeNode)
                    expandNode(leaf);
                break;
            case PdfObject.ARRAY:
                PdfArray array = (PdfArray) object;
                for (int i = 0; i < array.size(); ++i) {
                    leaf = PdfObjectTreeNode.getInstance(array.get(i, false));
                    associateIfIndirect(leaf);
                    addNodes(node, leaf);
                    expandNode(leaf);
                }
                break;
            case PdfObject.DICTIONARY:
            case PdfObject.STREAM:
                PdfDictionary dict = (PdfDictionary) object;
                for (PdfName key : dict.keySet()) {
                    leaf = PdfObjectTreeNode.getInstance(dict, key);
                    associateIfIndirect(leaf);
                    addNodes(node, leaf);
                    expandNode(leaf);
                }
                break;
        }
    }

    /**
     * Finds a specific child of dictionary node.
     * This method will follow indirect references and expand nodes if necessary
     *
     * @param node the node with a dictionary among its children
     * @param key  the key of the item corresponding with the node we need
     * @return a specific child of dictionary node
     */
    @SuppressWarnings("unchecked")
    public PdfObjectTreeNode getChildNode(PdfObjectTreeNode node, PdfName key) {
        PdfObjectTreeNode child = node.getDictionaryChildNode(key);
        if (child != null && child.isDictionaryNode(key)) {
            if (child.isIndirectReference()) {
                expandNode(child);
                child = (PdfObjectTreeNode) child.getFirstChild();
            }
            expandNode(child);
            return child;
        }
        return null;
    }

    /**
     * Tries adding a child node to a parent node without
     * throwing an exception. Normally, if the child node is already
     * added as one of the ancestors, an IllegalArgumentException is
     * thrown (to avoid an endless loop). Loops like this are allowed
     * in PDF, not in a JTree.
     *
     * @param parent the parent node
     * @param child  a child node
     */
    private void addNodes(PdfObjectTreeNode parent, PdfObjectTreeNode child) {
        try {
            parent.add(child);
        } catch (IllegalArgumentException iae) {
            parent.setRecursive(true);
        }
    }

    public void addNewIndirectObject(PdfObject object) {
        objects.addNewIndirectObject(object);
        nodes.add(PdfObjectTreeNode.getInstance(object, object.getIndirectReference().getObjNumber()));
        LoggerHelper.info("Tree node was successfully created for new indirect object", getClass());
    }
}
