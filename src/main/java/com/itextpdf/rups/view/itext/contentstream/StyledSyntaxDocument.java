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
package com.itextpdf.rups.view.itext.contentstream;


import com.itextpdf.commons.exceptions.ITextException;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.canvas.parser.util.PdfCanvasParser;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Language;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Swing document representation of a PDF content stream.
 */
public class StyledSyntaxDocument extends DefaultStyledDocument implements IMixedContentInterface {

    private static final int INLINE_IMAGE_EXPECTED_TOKEN_COUNT = 2;

    private static final String INDENTATION_PREFIX = "    ";

    /**
     * Highlight operands according to their operator.
     */
    private boolean matchingOperands = false;

    /**
     * Create an empty styled syntax document.
     */
    public StyledSyntaxDocument() {
        // nothing to initialise
    }

    /**
     * Set matching operands mode. In matching operands mode, operands are
     * marked up in the same style as their operators. The default is {@code false}.
     *
     * @param matchingOperands new setting value
     */
    public void setMatchingOperands(boolean matchingOperands) {
        this.matchingOperands = matchingOperands;
    }

    /**
     * Get matching operands mode. In matching operands mode, operands are
     * marked up in the same style as their operators. The default is {@code false}.
     */
    public boolean isMatchingOperands() {
        return matchingOperands;
    }

    /**
     * Check if a position in the textual (on-screen) representation of the document actually
     * points to textual stream content. This covers everything except strings with unknown
     * encodings and inline images.
     *
     * @param pos the position in the textual representation of the document to check
     * @return {@code false} if binary stream content, {@code true} otherwise
     */
    public boolean isTextual(int pos) {
        return getCharacterElement(pos)
                .getAttributes()
                .getAttribute(ContentStreamStyleConstants.BINARY_CONTENT) == null;
    }

    /**
     * Check if a position in the textual (on-screen) representation of the document
     * points to editable binary content that is represented hexadecimally.
     * This currently only applies to string objects with unknown encodings.
     *
     * <p>
     * Note that this does not apply to strings that use PDF-native hex syntax, but
     * only to cases where the in-document representation uses the string syntax
     * with parentheses.
     *
     * @param pos the position in the textual representation of the document to check
     * @return {@code false} if hex-editable stream content, {@code true} otherwise
     */
    public boolean isHexEditable(int pos) {
        return getCharacterElement(pos)
                .getAttributes()
                .getAttribute(ContentStreamStyleConstants.HEX_EDIT) != null;
    }

    /**
     * Process a content stream and add its operators to the document.
     *
     * @param streamContent the stream content
     */
    public void processContentStream(byte[] streamContent) {
        setSmartEditLock(false);
        final ArrayList<PdfObject> tokens = new ArrayList<>();

        final PdfCanvasParser ps = ContentStreamHandlingUtils.createCanvasParserFor(streamContent);
        try {
            int indentLevel = 0;
            while (!ps.parse(tokens).isEmpty()) {
                final String operator = (tokens.get(tokens.size() - 1)).toString();
                if (operator.equals("Q") || operator.equals("ET")) {
                    // Do not let indentation become negative
                    if (indentLevel > 0) {
                        indentLevel--;
                    }
                }
                appendGraphicsOperator(tokens, indentLevel);
                if (operator.equals("q") || operator.equals("BT")) {
                    indentLevel++;
                }
            }
        } catch (IOException | BadLocationException e) {
            throw new ITextException(Language.ERROR_BUILDING_CONTENT_STREAM.getString(), e);
        }
        setSmartEditLock(true);
    }

    /**
     * Get the tooltip text (if any) at the given position in the document.
     *
     * @param pos the position for which to fetch the tooltip text
     * @return a string, or {@code null} if there is no tooltip.
     */
    public String getToolTipAt(int pos) {
        final AttributeSet charAttrs = getCharacterElement(pos).getAttributes();
        if (charAttrs.getAttribute(ContentStreamStyleConstants.HEX_EDIT) != null) {
            return Language.TOOLTIP_HEX.getString();
        }
        final String encoding = (String) charAttrs.getAttribute(ContentStreamStyleConstants.ENCODING);
        if (encoding != null) {
            return String.format(Language.TOOLTIP_ENCODING.getString(), encoding);
        }
        return null;
    }

    /**
     * Append a PDF object to the content stream document.
     *
     * @param obj   the object to add
     * @param attrs the attributes to add
     */
    protected void appendPdfObject(PdfObject obj, AttributeSet attrs) throws BadLocationException {
        final AttributeSet effAttrs = attrs == null ? SimpleAttributeSet.EMPTY : attrs;
        switch (obj.getType()) {
            case PdfObject.STRING:
                appendPdfString((PdfString) obj, effAttrs);
                break;
            case PdfObject.DICTIONARY:
                appendPdfDictionary((PdfDictionary) obj, effAttrs);
                break;
            case PdfObject.ARRAY:
                appendPdfArray((PdfArray) obj, effAttrs);
                break;
            default:
                appendText(obj + " ", effAttrs);
        }
    }

    /**
     * Append graphics operators to the document. A graphics operator is specified as a list of
     * {@link PdfObject}s, where the last element of the list is the operator literal,
     * and the preceding elements represent the operands.
     *
     * @param tokens the representation of the graphics operator with its operands
     * @throws BadLocationException if an error occurs while modifying the document
     */
    protected  void appendGraphicsOperator(java.util.List<PdfObject> tokens) throws BadLocationException {
        appendGraphicsOperator(tokens, 0);
    }

    /**
     * Append graphics operators to the document, indenting by specified amount.
     * A graphics operator is specified as a list of {@link PdfObject}s,
     * where the last element of the list is the operator literal,
     * and the preceding elements represent the operands.
     *
     * @param tokens the representation of the graphics operator with its operands
     * @param indentLevel the number of levels to indent the operator by when displaying
     * @throws BadLocationException if an error occurs while modifying the document
     */
    protected void appendGraphicsOperator(java.util.List<PdfObject> tokens, int indentLevel) throws BadLocationException {
        // operator is at the end
        final String operator = (tokens.get(tokens.size() - 1)).toString();
        // Inline images are parsed as stream + EI
        if ("EI".equals(operator)
                && tokens.size() == INLINE_IMAGE_EXPECTED_TOKEN_COUNT
                && tokens.get(0) instanceof PdfStream) {
            appendInlineImage((PdfStream) tokens.get(0), indentLevel);
            return;
        }
        appendDisplayOnlyIndent(indentLevel);
        final AttributeSet attributes = getStyleAttributes(operator);
        for (int i = 0; i < tokens.size() - 1; i++) {
            appendPdfObject(tokens.get(i), matchingOperands ? attributes : null);
        }
        appendText(operator + "\n", attributes);
    }

    /**
     * Get the styling attributes for a given operator.
     *
     * @param operator the PDF graphics operator to get the attributes for
     * @return {@link AttributeSet} containing the attributes
     */
    protected AttributeSet getStyleAttributes(String operator) {
        return ContentStreamStyleConstants.getStyleAttributesFor(operator);
    }

    /**
     * Append an inline image to the content stream document, indenting relative to
     * the provided indent level.
     *
     * @param stm the {@link PdfStream} containing the image data
     * @param indentLevel the base indentation level to use when displaying
     */
    protected void appendInlineImage(final PdfStream stm, int indentLevel) throws BadLocationException {
        appendDisplayOnlyIndent(indentLevel);
        appendText("BI\n", getStyleAttributes("BI"));

        for (final PdfName key : stm.keySet()) {
            appendDisplayOnlyIndent(indentLevel + 1);
            appendPdfObject(key, null);
            appendPdfObject(stm.get(key, false), null);
            appendText("\n", null);
        }
        appendDisplayOnlyIndent(indentLevel + 1);
        appendText("ID\n", getStyleAttributes("ID"));

        try {
            // inline image parser takes care of expanding abbreviations
            final BufferedImage img = new PdfImageXObject(stm).getBufferedImage();
            insertAndRenderInlineImage(img, stm.getBytes(false), indentLevel + 1);
            appendText("\n", null);
            appendDisplayOnlyIndent(indentLevel);
            appendText("EI\n", getStyleAttributes("EI"));
        } catch (IOException | ITextException e) {
            LoggerHelper.error(Language.ERROR_UNEXPECTED_EXCEPTION.getString(), e, getClass());

            // display error message backed by raw image data
            appendDisplayOnlyIndent(indentLevel + 1);
            final MutableAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(ContentStreamStyleConstants.BINARY_CONTENT, stm.getBytes(false));
            appendText(Language.ERROR_PROCESSING_IMAGE.getString(), attrs);
            // in this case we don't add extra whitespace before EI
            // since iText potentially gobbled it when it attempted to parse the
            // (corrupt?) image
            appendDisplayOnlyIndent(indentLevel);
            appendText("EI\n", getStyleAttributes("EI"));
        }
    }

    /**
     * Set the document's smart edit lock. This restricts textual edits to areas of
     * the content stream that are unambiguously encoded, while also allowing hexadecimally
     * represented binary data to be edited. Initially, the edit lock is inactive.
     *
     * @param active the new state of the lock
     */
    protected void setSmartEditLock(boolean active) {
        if (active) {
            setDocumentFilter(new ControlledBinaryEditsFilter(this));
        } else {
            setDocumentFilter(null);
        }
    }

    private void appendSmartTextString(byte[] b, AttributeSet attrs) throws BadLocationException {
        /*
        We want to preserve the encoding of string objects in content streams
        if the string contains Unicode text (UTF-8 / UTF-16BE).
        We also want to preserve readable PDFDocEncoding strings, and somehow
        render binary-text-as-strings legible on-screen. All of this has to be
        compatible with stream editing capabilities (except in the binary case,
        possibly; that's more tricky to reconstruct).
        */
        if (ContentStreamHandlingUtils.hasUtf16beBom(b)) {
            appendEncodedTextString(b, PdfEncodings.UNICODE_BIG, attrs);
        } else if (ContentStreamHandlingUtils.hasUtf8Bom(b)) {
            appendEncodedTextString(b, PdfEncodings.UTF8, attrs);
        } else if (ContentStreamHandlingUtils.isMaybePdfDocEncodedText(b)) {
            appendEncodedTextString(b, PdfEncodings.PDF_DOC_ENCODING, attrs);
        } else {
            appendBinaryTextString(b, attrs);
        }
    }

    private void appendBinaryTextString(byte[] b, AttributeSet attrs) throws BadLocationException {
        // display the string in marked-up hex, and make sure it's backed by the "true" binary value
        final StringBuilder sb = new StringBuilder();
        ContentStreamHandlingUtils.hexlify(b, sb);
        final MutableAttributeSet binAttrs = new SimpleAttributeSet();
        binAttrs.addAttributes(attrs);
        binAttrs.addAttributes(ContentStreamStyleConstants.EDITABLE_HEX_CONTENT_ATTRS);
        binAttrs.addAttribute(ContentStreamStyleConstants.BINARY_CONTENT, ContentStreamHandlingUtils.ensureEscaped(b));
        binAttrs.addAttribute(ContentStreamStyleConstants.HEX_EDIT, Boolean.TRUE);

        final MutableAttributeSet delimAttrs = new SimpleAttributeSet();
        delimAttrs.addAttributes(attrs);
        delimAttrs.addAttributes(ContentStreamStyleConstants.EDITABLE_HEX_CONTENT_ATTRS);
        delimAttrs.addAttributes(ContentStreamStyleConstants.DISPLAY_ONLY_ATTRS);
        appendText("(", attrs);
        appendText("{", delimAttrs);
        appendText(sb.toString(), binAttrs);
        appendText("}", delimAttrs);
        appendText(") ", attrs);
    }

    private void appendEncodedTextString(byte[] b, String encoding, AttributeSet attrs) throws BadLocationException {
        final MutableAttributeSet encAttrs = new SimpleAttributeSet();
        encAttrs.addAttributes(attrs);
        encAttrs.addAttribute(ContentStreamStyleConstants.ENCODING, encoding);
        appendText("(", attrs);
        appendText(PdfEncodings.convertToString(b, encoding), encAttrs);
        appendText(") ", attrs);
    }

    private void appendPdfString(PdfString str, AttributeSet attrs) throws BadLocationException {
        if (str.isHexWriting()) {
            final StringBuilder sb = new StringBuilder();
            sb.append('<');
            ContentStreamHandlingUtils.hexlify(str.getValueBytes(), sb);
            sb.append("> ");
            appendText(sb.toString(), attrs);
        } else {
            // note: this always hits the code path with encoding = null in our case,
            // so this should always return the underlying string as-is.
            final byte[] b = str.getValueBytes();
            appendSmartTextString(b, attrs);
        }
    }

    private void appendPdfDictionary(PdfDictionary dict, AttributeSet attrs) throws BadLocationException {
        appendText("<<", attrs);
        for (final PdfName key : dict.keySet()) {
            appendPdfObject(key, attrs);
            appendPdfObject(dict.get(key, false), attrs);
        }
        appendText(">> ", attrs);
    }

    private void appendPdfArray(Iterable<PdfObject> arr, AttributeSet attrs) throws BadLocationException {
        appendText("[", attrs);
        for (final PdfObject item : arr) {
            appendPdfObject(item, attrs);
        }
        appendText("] ", attrs);
    }

    private void appendText(String s, AttributeSet attr) throws BadLocationException {
        insertString(getLength(), s, attr == null ? SimpleAttributeSet.EMPTY : attr);
    }

    private void appendDisplayOnlyIndent(int indentLevel) throws BadLocationException {
        appendText(INDENTATION_PREFIX.repeat(indentLevel), ContentStreamStyleConstants.DISPLAY_ONLY_ATTRS);
    }

    private void appendDisplayOnlyNewline() throws BadLocationException {
        insertString(getLength(), "\n", ContentStreamStyleConstants.DISPLAY_ONLY_ATTRS);
    }

    private void insertAndRenderInlineImage(final BufferedImage img, byte[] rawBytes, int indentLevel) throws BadLocationException {
        // add the image
        final AttributeSet imageAttrs = ContentStreamStyleConstants.getImageAttributes(img, rawBytes);
        appendDisplayOnlyIndent(indentLevel);
        insertString(getLength(), " ", imageAttrs);
        appendDisplayOnlyNewline();

        // add the button
        final AttributeSet buttonAttrs = ContentStreamStyleConstants.getImageSaveButtonAttributes(img);
        appendDisplayOnlyIndent(indentLevel);
        insertString(getLength(), " ", buttonAttrs);
        appendDisplayOnlyNewline();
    }

}
