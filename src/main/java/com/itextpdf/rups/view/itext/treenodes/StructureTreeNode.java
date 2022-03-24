/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
package com.itextpdf.rups.view.itext.treenodes;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.rups.view.icons.IconTreeNode;

public class StructureTreeNode extends IconTreeNode {

    /**
     * The corresponding tree node in the PdfTree.
     */
    protected PdfObjectTreeNode object_node;

    /**
     * Creates the root node for the structure tree.
     */
    public StructureTreeNode() {
        super("chart_organisation.png", "Structure Tree");
    }

    /**
     * @param node the pdfobject treenode
     * @param icon the icon name
     */
    public StructureTreeNode(PdfObjectTreeNode node, String icon) {
        super(icon);
        this.object_node = node;
        if (node.isDictionary()) {
            PdfDictionary dict = (PdfDictionary) node.getPdfObject();
            //if (dict.get(PdfName.TYPE) == null || dict.checkType(PdfName.STRUCTELEM)) {
            if (dict.get(PdfName.Type, false) == null || dict.get(PdfName.Type, false).equals(PdfName.StructElem)) {
                StringBuilder buf = new StringBuilder();
                if (dict.get(PdfName.S, false) != null)
                    buf.append(PdfObjectTreeNode.getCaption(dict.get(PdfName.S, false)));
                if (dict.get(PdfName.T, false) != null) {
                    buf.append(" -> ");
                    buf.append(PdfObjectTreeNode.getCaption(dict.get(PdfName.T, false)));
                }
                this.setUserObject(buf.toString());
                return;
            }
        }
        this.setUserObject(node);
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
}
