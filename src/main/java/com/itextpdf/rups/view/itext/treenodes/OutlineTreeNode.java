/*
 * $Id$
 *
 * This file is part of the iText (R) project.
    Copyright (c) 2007-2018 iText Group NV
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
package com.itextpdf.rups.view.itext.treenodes;

import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.rups.view.icons.IconTreeNode;

/**
 * A node in the OutlineTree.
 */
public class OutlineTreeNode extends IconTreeNode {

    /**
     * The corresponding tree node in the PdfTree.
     */
    protected PdfObjectTreeNode object_node;

    /**
     * Creates the root node for the OutlineTree.
     */
    public OutlineTreeNode() {
        super("outline.png", "Bookmarks");
    }

    /**
     * Creates a node corresponding with a node in the PdfTree.
     *
     * @param    node    a corresponding node
     */
    public OutlineTreeNode(PdfObjectTreeNode node) {
        super("outline.png");
        this.object_node = node;
        PdfDictionary dict = (PdfDictionary) node.getPdfObject();
        this.setUserObject(dict.get(PdfName.Title, false));
    }

    /**
     * Gets the node in the PdfTree that corresponds with this
     * OutlineTreeNode.
     *
     * @return a PdfObjectTreeNode in the PdfTree
     */
    public PdfObjectTreeNode getCorrespondingPdfObjectNode() {
        return object_node;
    }

    @Override
    public String toString() {
        if (userObject == null)
            return "";
        if (userObject instanceof PdfObject)
            return PdfObjectTreeNode.getCaption((PdfObject) userObject);
        return userObject.toString();
    }

    /**
     * A serial version uid
     */
    private static final long serialVersionUID = 5437651809665762952L;
}
