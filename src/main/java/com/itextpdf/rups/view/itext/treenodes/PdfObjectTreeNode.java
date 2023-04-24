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

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.icons.IconFetcher;
import com.itextpdf.rups.view.icons.IconTreeNode;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

/**
 * Every node in our tree corresponds with a PDF object.
 * This class is the superclass of all tree nodes used.
 */
public class PdfObjectTreeNode extends IconTreeNode {

    private static final String ARRAY_ICON = "array.png";
    private static final String BOOLEAN_ICON = "boolean.png";
    private static final String DICTIONARY_ICON = "dictionary.png";
    private static final String NAME_ICON = "name.png";
    private static final String NULL_ICON = "null.png";
    private static final String NUMBER_ICON = "number.png";
    private static final String REF_ICON = "ref.png";
    private static final String REF_RECURSIVE_ICON = "ref_recursive.png";
    private static final String STREAM_ICON = "stream.png";
    private static final String STRING_ICON = "string.png";

    /**
     * If the object is indirect, the number of the PDF object.
     */
    protected int number = -1;

    /**
     * Indicates if the object is indirect and recursive.
     */
    protected boolean recursive = false;

    /**
     * The key if the parent of this node is a dictionary.
     */
    protected PdfName key = null;

    /**
     * The PDF object corresponding with this node.
     */
    protected PdfObject object;

    /**
     * Creates a tree node for a PDF object.
     *
     * @param object the PDF object represented by this tree node.
     */
    protected PdfObjectTreeNode(PdfObject object) {
        super(null, getCaption(object));
        this.object = object;
        switch (object.getType()) {
            case PdfObject.INDIRECT_REFERENCE:
                if (isRecursive()) {
                    icon = IconFetcher.getIcon(REF_RECURSIVE_ICON);
                } else {
                    icon = IconFetcher.getIcon(REF_ICON);
                }
                break;
            case PdfObject.ARRAY:
                icon = IconFetcher.getIcon(ARRAY_ICON);
                break;
            case PdfObject.DICTIONARY:
                icon = IconFetcher.getIcon(DICTIONARY_ICON);
                break;
            case PdfObject.STREAM:
                icon = IconFetcher.getIcon(STREAM_ICON);
                break;
            case PdfObject.BOOLEAN:
                icon = IconFetcher.getIcon(BOOLEAN_ICON);
                break;
            case PdfObject.NAME:
                icon = IconFetcher.getIcon(NAME_ICON);
                break;
            case PdfObject.LITERAL:
            case PdfObject.NULL:
                icon = IconFetcher.getIcon(NULL_ICON);
                break;
            case PdfObject.NUMBER:
                icon = IconFetcher.getIcon(NUMBER_ICON);
                break;
            case PdfObject.STRING:
                icon = IconFetcher.getIcon(STRING_ICON);
                break;
        }
    }

    /**
     * Creates a tree node for a PDF object.
     *
     * @param icon   the file with the icon
     * @param object the PDF object represented by this tree node.
     */
    protected PdfObjectTreeNode(String icon, PdfObject object) {
        super(icon, getCaption(object));
        this.object = object;
    }

    /**
     * Creates an instance of a tree node for a PDF object.
     *
     * @param object the PDF object represented by this tree node.
     * @return a PdfObjectTreeNode
     */
    public static PdfObjectTreeNode getInstance(PdfObject object) {
        if (object.isDictionary()) {
            if (PdfName.Page.equals(((PdfDictionary) object).get(PdfName.Type, false))) {
                return new PdfPageTreeNode((PdfDictionary) object);
            }
            if (PdfName.Pages.equals(((PdfDictionary) object).get(PdfName.Type, false))) {
                return new PdfPagesTreeNode((PdfDictionary) object);
            }
        }
        return new PdfObjectTreeNode(object);
    }

    /**
     * Creates an instance of a tree node for an indirect object.
     *
     * @param object the PDF object represented by this tree node.
     * @param number the xref number of the indirect object
     * @return a PdfObjectTreeNode
     */
    public static PdfObjectTreeNode getInstance(PdfObject object, int number) {
        final PdfObjectTreeNode node = getInstance(object);
        node.number = number;
        return node;
    }

    /**
     * Creates an instance of a tree node for the object corresponding with a key in a dictionary.
     *
     * @param dict the dictionary that is the parent of this tree node.
     * @param key  the dictionary key corresponding with the PDF object in this tree node.
     * @return a PdfObjectTreeNode
     */
    public static PdfObjectTreeNode getInstance(PdfDictionary dict, PdfName key) {
        final PdfObjectTreeNode node = getInstance(dict.get(key, false));
        node.setUserObject(getDictionaryEntryCaption(dict, key));
        node.key = key;
        return node;
    }

    //TODO: Check if this works...
    /**
     * Creates an instance of a tree node for the object corresponding with a key in a dictionary.
     *
     * @param obj the pdf object that is represented by this tree node.
     * @param key  the dictionary key corresponding with the PDF object in this tree node.
     * @return a PdfObjectTreeNode
     */
    public static PdfObjectTreeNode getInstance(PdfObject obj, PdfName key) {
        final PdfObjectTreeNode node = getInstance(obj);
        node.key = key;
        return node;
    }

    /**
     * Getter for the PDF Object.
     *
     * @return the PDF object represented by this tree node.
     */
    public PdfObject getPdfObject() {
        return object;
    }

    /**
     * Getter for the ObjectTree's key.
     *
     * @return the PDF Name representing this tree node.
     */
    public PdfName getKey(){
        return key;
    }

    /**
     * Getter for the object number in case the object is indirect or if it is reference.
     *
     * @return -1 for direct objects; the object number for indirect objects
     */
    public int getNumber() {
        if (isIndirect() && object.getIndirectReference() != null) {
            return object.getIndirectReference().getObjNumber();
        }
        if (isIndirectReference()) {
            return ((PdfIndirectReference) object).getObjNumber();
        }
        return number;
    }

    /**
     * Tells you if the node contains an indirect reference.
     *
     * @return true if the object is an indirect reference
     */
    public boolean isIndirectReference() {
        return object.isIndirectReference();
    }

    /**
     * Tells you if the object is indirect.
     *
     * @return true for indirect objects; false for direct objects.
     */
    public boolean isIndirect() {
        return object.isIndirect();
    }

    /**
     * Tells you if the node contains an array.
     *
     * @return true if the object is a PdfArray
     */
    public boolean isArray() {
        return object.isArray();
    }

    /**
     * Checks if this node is a dictionary item with a specific key.
     *
     * @param key the key of the node we're looking for
     * @return true if this node is a dictionary item with a specific key
     */
    public boolean isDictionaryNode(PdfName key) {
        return (key != null) && key.equals(this.key);
    }

    /**
     * Gets the ChildNode with specific key if this node is a dictionary. Otherwise return {@code null}
     *
     * @param key key of the node to find
     * @return find node or {@code null}
     */
    public PdfObjectTreeNode getDictionaryChildNode(PdfName key) {
        final Enumeration<TreeNode> children = breadthFirstEnumeration();
        PdfObjectTreeNode child;
        while (children.hasMoreElements()) {
            child = (PdfObjectTreeNode) children.nextElement();
            if (child.isDictionaryNode(key)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Tells you if the node contains a dictionary.
     *
     * @return true if the object is a PdfDictionary
     */
    public boolean isDictionary() {
        return object.isDictionary();
    }

    /**
     * Tells you if the node contains a stream.
     *
     * @return true if the object is a PRStream
     */
    public boolean isStream() {
        return object.isStream();
    }

    /**
     * Set this to true if the object is a reference to a node higher up in the tree.
     *
     * @param recursive true if the object is indirect and recursive
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Tells you if the object is a reference to a node higher up in the tree.
     *
     * @return true if the node is used recursively.
     */
    public final boolean isRecursive() {
        return recursive;
    }

    /**
     * Creates the caption for a PDF object.
     *
     * @param object the object for which a caption has to be created.
     * @return a caption for a PDF object
     */
    public static String getCaption(PdfObject object) {
        if (object == null) {
            return Language.NULL_AS_TEXT.getString();
        }
        PdfName type;
        switch (object.getType()) {
            case PdfObject.INDIRECT_REFERENCE: {
                String reffedCaption = getCaption(((PdfIndirectReference) object).getRefersTo(false));
                return object + " -> " + reffedCaption;
            }
            case PdfObject.ARRAY:
                return Language.ARRAY.getString();
            case PdfObject.STREAM:
                type = ((PdfDictionary) object).getAsName(PdfName.Type);
                if (type == null) {
                    return Language.STREAM.getString();
                }
                return String.format(Language.STREAM_OF_TYPE.getString(), type);
            case PdfObject.STRING:
                return ((PdfString) object).toUnicodeString();
            case PdfObject.DICTIONARY:
                type = ((PdfDictionary) object).getAsName(PdfName.Type);
                if (type == null) {
                    return Language.DICTIONARY.getString();
                }
                return String.format(Language.DICTIONARY_OF_TYPE.getString(), type);
            default:
                return object.toString();
        }
    }

    /**
     * Creates the caption for an object that is a dictionary entry.
     *
     * @param dict a dictionary
     * @param key  a key in the dictionary
     * @return a caption for the object corresponding with the key in the dictionary.
     */
    public static String getDictionaryEntryCaption(PdfDictionary dict, PdfName key) {
        final StringBuilder buf = new StringBuilder(key.toString());
        buf.append(": ");
        final PdfObject valObj = dict.get(key, false);
        buf.append(getCaption(valObj));
        return buf.toString();
    }

    /**
     * Gets the tree path of an ancestor.
     * This only works with recursive references
     *
     * @return the treepath to an ancestor
     */
    public PdfObjectTreeNode getAncestor() {
        try {
            if (isRecursive()) {
                PdfObjectTreeNode node = this;
                while (true) {
                    node = (PdfObjectTreeNode) node.getParent();
                    if (node.isIndirectReference() && node.getNumber() == getNumber()) {
                        return node;
                    }
                }
            }
        } catch (NullPointerException e) {
            LoggerHelper.warn(Language.ERROR_PARENT_NULL.getString(), e, getClass());
        }
        return null;
    }

    /**
     * If this node represents a dictionary with a direct /Type entry of which
     * the value is a name object, return it.
     *
     * @return a {@link PdfName} object or {@code null}
     */
    public PdfName getPdfDictionaryType() {
        PdfObject obj = getPdfObject();
        if(obj.isDictionary()) {
            PdfObject name = ((PdfDictionary) obj).get(PdfName.Type, false);
            if (name instanceof PdfName) {
                return (PdfName) name;
            }
        }
        return null;
    }
}
