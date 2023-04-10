/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

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
package com.itextpdf.rups.view.itext.contentstream;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.util.Locale;

/**
 * {@link DocumentFilter} that prevents accidental mangling of binary content.
 */
public class ControlledBinaryEditsFilter extends DocumentFilter {

    private final IMixedContentInterface doc;

    /**
     * Create a {@link ControlledBinaryEditsFilter} over the given a document
     * that contains mixed text/binary content.
     *
     * @param doc the {@link IMixedContentInterface} to the document
     */
    public ControlledBinaryEditsFilter(IMixedContentInterface doc) {
        super();
        this.doc = doc;
    }

    private boolean bothTextual(int start, int end) {
        return doc.isTextual(start) && doc.isTextual(end);
    }

    private boolean contiguousHexEditableRegion(int start, int len) {
        for (int i = 0; i < len; i++) {
            if (!doc.isHexEditable(start + i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        // Deletion is OK if the binary object is encompassed fully, so we only check
        // if start/end fall inside a binary object
        if (bothTextual(offset, offset + length) || contiguousHexEditableRegion(offset, length)) {
            super.remove(fb, offset, length);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (doc.isTextual(offset)) {
            super.insertString(fb, offset, string, attr);
        } else if (doc.isHexEditable(offset) || (offset > 0 && doc.isHexEditable(offset - 1))) {
            // note: we need to give special treatment to the (offset - 1) case to be able to insert
            // stuff at the end of a hex string.
            final String hex = ensureHex(string);
            if (hex != null) {
                super.insertString(fb, offset, hex, hexContentAttrs());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        // Replacement is OK if the binary object is encompassed fully, so we only check
        // if start/end fall inside a binary object
        if (bothTextual(offset, offset + length)) {
            super.replace(fb, offset, length, text, attrs);
        } else if (contiguousHexEditableRegion(offset, length)) {
            final String hex = ensureHex(text);
            if (hex == null) {
                super.remove(fb, offset, length);
            } else {
                super.replace(fb, offset, length, hex, hexContentAttrs());
            }
        }
    }

    private static String ensureHex(String text) {
        if (text == null) {
            return null;
        }
        final String hex = ContentStreamHandlingUtils
                .dropNonHexDigits(text.toLowerCase(Locale.ROOT));
        return hex.isEmpty() ? null : hex;
    }

    private static AttributeSet hexContentAttrs() {
        final MutableAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttributes(ContentStreamStyleConstants.EDITABLE_HEX_CONTENT_ATTRS);
        attrs.addAttribute(ContentStreamStyleConstants.HEX_EDIT, Boolean.TRUE);
        return attrs;
    }
}
