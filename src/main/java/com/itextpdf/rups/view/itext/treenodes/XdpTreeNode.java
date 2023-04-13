/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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

import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.icons.IconFetcher;
import com.itextpdf.rups.view.icons.IconTreeNode;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.Text;

import java.util.List;

public class XdpTreeNode extends IconTreeNode {

    private static final String ATTRIBUTE_ICON = "attribute.png";
    private static final String TEXT_ICON = "text.png";
    private static final String PI_ICON = "pi.png";
    private static final String XFA_ICON = "xfa.png";
    private static final String TAG_ICON = "tag.png";

    /**
     * Constructs an XdpTreeNode
     *
     * @param node the XML node
     */
    @SuppressWarnings("unchecked")
    public XdpTreeNode(Node node) {
        super(null, node);
        if (node instanceof Element) {
            Element element = (Element) node;
            addChildNodes(element.attributes());
        }
        if (node instanceof Branch) {
            Branch branch = (Branch) node;
            addChildNodes(branch.content());
        }

        if (node instanceof Attribute) {
            icon = IconFetcher.getIcon(ATTRIBUTE_ICON);
            return;
        }
        if (node instanceof Text) {
            icon = IconFetcher.getIcon(TEXT_ICON);
            return;
        }
        if (node instanceof ProcessingInstruction) {
            icon = IconFetcher.getIcon(PI_ICON);
            return;
        }
        if (node instanceof Document) {
            icon = IconFetcher.getIcon(XFA_ICON);
            return;
        }
        icon = IconFetcher.getIcon(TAG_ICON);
    }

    private void addChildNodes(List<? extends Node> list) {
        for (Node node : list) {
            if (!(node instanceof Namespace || node instanceof Comment)) {
                this.add(new XdpTreeNode(node));
            }
        }
    }

    public Node getNode() {
        return (Node) getUserObject();
    }

    @Override
    public String toString() {
        Node node = getNode();
        if (node instanceof Element) {
            Element e = (Element) node;
            return e.getName();
        }
        if (node instanceof Attribute) {
            Attribute a = (Attribute) node;
            return a.getName() +
                    "=\"" +
                    a.getValue() +
                    '"';
        }
        if (node instanceof Text) {
            Text t = (Text) node;
            return t.getText();
        }
        if (node instanceof ProcessingInstruction) {
            ProcessingInstruction pi = (ProcessingInstruction) node;
            return "<?" + pi.getName() +
                    ' ' +
                    pi.getText() +
                    "?>";
        }
        if (node instanceof Document) {
            return Language.FORM_XFA_DOCUMENT.getString();
        }
        return getNode().toString();
    }
}
