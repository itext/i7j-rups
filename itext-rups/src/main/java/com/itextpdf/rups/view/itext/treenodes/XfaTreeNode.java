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

import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.rups.io.OutputStreamResource;

import javax.swing.tree.TreeNode;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * This is the root tree node for the different parts of the XFA resource; it's a child
 * of the root in the FormTree.
 * This resource can be one XDP stream (in which case this root will only have one child)
 * or different streams with individual packets comprising the XML Data Package.
 */
public class XfaTreeNode extends FormTreeNode implements OutputStreamResource {

    /**
     * Creates the root node of the XFA tree.
     * This will be a child of the FormTree root node.
     *
     * @param xfa the XFA node in the PdfTree (a child of the AcroForm node in the PDF catalog)
     */
    public XfaTreeNode(PdfObjectTreeNode xfa) {
        super(xfa);
    }

    /**
     * Writes (part of) the XFA resource to an OutputStream.
     * If key is {@code null}, the complete resource is written;
     * if key refers to an individual package, this package only is
     * written to the OutputStream.
     *
     * @param os the OutputStream to which the XML is written.
     * @throws IOException usual exception when there's a problem writing to an OutputStream
     */
    @SuppressWarnings("unchecked")
    public void writeTo(OutputStream os) throws IOException {
        final Enumeration<TreeNode> children = this.children();
        FormTreeNode node;
        PdfStream stream;
        while (children.hasMoreElements()) {
            node = (FormTreeNode) children.nextElement();
            stream = (PdfStream) node.getCorrespondingPdfObjectNode().getPdfObject();
            os.write(stream.getBytes());
        }
        os.flush();
        os.close();
    }

    /**
     * Adds a child node to the XFA root.
     * The child node either corresponds with the complete XDP stream
     * (if the XFA root only has one child) or with individual packet.
     *
     * @param key   the name of the packet
     * @param value the corresponding stream node in the PdfTree
     */
    public void addPacket(String key, PdfObjectTreeNode value) {
        final FormTreeNode node = new FormTreeNode(value);
        node.setUserObject(key);
        this.add(node);
    }
}
