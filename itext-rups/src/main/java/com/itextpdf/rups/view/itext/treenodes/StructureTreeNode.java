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
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.icons.IconTreeNode;

public class StructureTreeNode extends IconTreeNode {

    private static final String CHART_ORGANISATION_ICON = "chart_organisation.png";

    /**
     * The corresponding tree node in the PdfTree.
     */
    private final PdfObjectTreeNode objectNode;

    /**
     * Creates the root node for the structure tree.
     */
    public StructureTreeNode() {
        this(null, CHART_ORGANISATION_ICON, Language.STRUCTURE_TREE.getString());
    }

    public StructureTreeNode(PdfObjectTreeNode node, String icon) {
        this(node, icon, null, node.getPdfObject());
    }

    /**
     * @param referenceTarget the pdfObject treeNode to jump to
     * @param icon the icon name
     * @param extractedText the extracted text
     * @param pdfObject the {@link PdfObject} in focus
     */
    public StructureTreeNode(PdfObjectTreeNode referenceTarget, String icon, String extractedText,
                             PdfObject pdfObject) {
        this(referenceTarget, icon, extractUserObject(pdfObject, extractedText, referenceTarget));
    }

    /**
     * @param referenceTarget the pdfObject treeNode to jump to
     * @param icon the icon name
     * @param userObject the user object to use as the node label
     */
    protected StructureTreeNode(PdfObjectTreeNode referenceTarget, String icon, Object userObject) {
        super(icon, userObject);
        this.objectNode = referenceTarget;
    }

    /**
     * Gets the node in the PdfTree that corresponds with this
     * OutlineTreeNode.
     *
     * @return a PdfObjectTreeNode in the PdfTree
     */
    public PdfObjectTreeNode getCorrespondingPdfObjectNode() {
        return objectNode;
    }

    private static Object ingestDictionaryNode(PdfDictionary dict, PdfObjectTreeNode node) {
        final Object userObj;
        final PdfObject dictType = dict.get(PdfName.Type, false);
        if (PdfName.StructElem.equals(dictType)) {
            final StringBuilder buf = new StringBuilder();
            if (dict.get(PdfName.S, false) != null) {
                buf.append(PdfObjectTreeNode.getCaption(dict.get(PdfName.S, false)));
            }
            if (dict.get(PdfName.T, false) != null) {
                buf.append(" -> ");
                buf.append(PdfObjectTreeNode.getCaption(dict.get(PdfName.T, false)));
            }
            final PdfString actualText = dict.getAsString(PdfName.ActualText);
            if (actualText != null) {
                formatExtractedText(buf, actualText.toUnicodeString());
            }
            userObj = buf.toString();
        } else if (PdfName.OBJR.equals(dictType)){
            userObj = "OBJR => " + node.getPdfObject().getIndirectReference();
        } else {
            userObj = node;
        }
        return userObj;
    }

    protected static void formatExtractedText(StringBuilder base, String extractedText) {
        base.append(" [").append(extractedText).append(']');
    }

    protected static Object extractUserObject(
            PdfObject pdfObject, String extractedText, PdfObjectTreeNode referenceTarget) {
        if (pdfObject == null) {
            return null;
        }
        if (pdfObject.isDictionary()) {
            return ingestDictionaryNode((PdfDictionary) pdfObject, referenceTarget);
        } else if (pdfObject.isNumber() && extractedText != null) {
            StringBuilder buf = new StringBuilder().append(pdfObject);
            formatExtractedText(buf, extractedText);
            return buf.toString();
        } else {
            return referenceTarget;
        }
    }
}
