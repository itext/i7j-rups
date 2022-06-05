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
package com.itextpdf.rups.view.itext.contentstream;

import com.itextpdf.rups.RupsConfiguration;
import com.itextpdf.rups.view.contextmenu.SaveImageAction;
import com.itextpdf.rups.view.Language;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Constants for dealing with markup in content streams.
 */
public final class ContentStreamStyleConstants {

    /**
     * Binary content attribute marker.
     */
    public static final Attribute BINARY_CONTENT = new Attribute("binary-content");

    /**
     * Encoding attribute marker.
     */
    public static final Attribute ENCODING = new Attribute("encoding");

    /**
     * Hex editable attribute marker.
     */
    public static final Attribute HEX_EDIT = new Attribute("hex-editable");

    /**
     * Syntax highlight attributes for operators.
     */
    static final Map<String, AttributeSet> ATTRIBUTE_MAP = initAttributes();

    /**
     * Default attribute set.
     */
    static final AttributeSet DEFAULT_ATTRS;

    /**
     * Attribute set for hex-editable binary content.
     */
    static final AttributeSet EDITABLE_HEX_CONTENT_ATTRS;

    /**
     * Attribute set for display-only content that is not reflected in the
     * reserialised (binary) output.
     */
    static final AttributeSet DISPLAY_ONLY_ATTRS;

    static {
        final MutableAttributeSet mas = new SimpleAttributeSet();
        mas.addAttribute(StyleConstants.Foreground, Color.BLACK);
        mas.addAttribute(StyleConstants.Background, Color.WHITE);
        DEFAULT_ATTRS = mas;

        final MutableAttributeSet editableHexContent = new SimpleAttributeSet();
        editableHexContent.addAttribute(StyleConstants.Underline, Boolean.TRUE);
        editableHexContent.addAttribute(StyleConstants.Foreground, Color.GRAY);
        editableHexContent.addAttribute(StyleConstants.FontFamily, "Monospaced");
        EDITABLE_HEX_CONTENT_ATTRS = editableHexContent;

        final MutableAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(ContentStreamStyleConstants.BINARY_CONTENT, new byte[0]);
        DISPLAY_ONLY_ATTRS = attrs;
    }

    private ContentStreamStyleConstants() {

    }

    /**
     * Initialize the syntax highlighting attributes.
     * This could be read from a configuration file, but is hard coded for now
     */
    static Map<String, AttributeSet> initAttributes() {
        final Map<String, AttributeSet> attributeMap = new HashMap<>();

        addPathOps(attributeMap);
        addGraphicsOps(attributeMap);

        final MutableAttributeSet xObject = new SimpleAttributeSet();
        xObject.addAttribute(StyleConstants.Foreground, Color.BLACK);
        xObject.addAttribute(StyleConstants.Background, Color.YELLOW);
        attributeMap.put("Do", xObject);

        final MutableAttributeSet inlineImage = new SimpleAttributeSet();
        inlineImage.addAttribute(StyleConstants.Foreground, Color.BLACK);
        inlineImage.addAttribute(StyleConstants.Background, Color.YELLOW);
        inlineImage.addAttribute(StyleConstants.Italic, Boolean.TRUE);
        attributeMap.put("BI", inlineImage);
        attributeMap.put("EI", inlineImage);

        addTextOps(attributeMap);

        final MutableAttributeSet markedContent = new SimpleAttributeSet();
        markedContent.addAttribute(StyleConstants.Foreground, Color.MAGENTA);
        markedContent.addAttribute(StyleConstants.Background, Color.WHITE);
        attributeMap.put("BMC", markedContent);
        attributeMap.put("BDC", markedContent);
        attributeMap.put("EMC", markedContent);
        return attributeMap;
    }

    private static void addPathOps(Map<String, AttributeSet> attributeMap) {
        final MutableAttributeSet opConstructionPainting = new SimpleAttributeSet();
        final Color darkorange = new Color(255, 140, 0);
        opConstructionPainting.addAttribute(StyleConstants.Foreground, darkorange);
        opConstructionPainting.addAttribute(StyleConstants.Background, Color.WHITE);
        attributeMap.put("m", opConstructionPainting);
        attributeMap.put("l", opConstructionPainting);
        attributeMap.put("c", opConstructionPainting);
        attributeMap.put("v", opConstructionPainting);
        attributeMap.put("y", opConstructionPainting);
        attributeMap.put("h", opConstructionPainting);
        attributeMap.put("re", opConstructionPainting);
        attributeMap.put("S", opConstructionPainting);
        attributeMap.put("s", opConstructionPainting);
        attributeMap.put("f", opConstructionPainting);
        attributeMap.put("F", opConstructionPainting);
        attributeMap.put("f*", opConstructionPainting);
        attributeMap.put("B", opConstructionPainting);
        attributeMap.put("B*", opConstructionPainting);
        attributeMap.put("b", opConstructionPainting);
        attributeMap.put("b*", opConstructionPainting);
        attributeMap.put("n", opConstructionPainting);
        attributeMap.put("W", opConstructionPainting);
        attributeMap.put("W*", opConstructionPainting);
    }

    private static void addTextOps(Map<String, AttributeSet> attributeMap) {
        final MutableAttributeSet textdelim = new SimpleAttributeSet();
        textdelim.addAttribute(StyleConstants.Foreground, Color.WHITE);
        textdelim.addAttribute(StyleConstants.Background, Color.BLUE);
        textdelim.addAttribute(StyleConstants.Bold, Boolean.TRUE);
        attributeMap.put("BT", textdelim);
        attributeMap.put("ET", textdelim);

        final MutableAttributeSet text = new SimpleAttributeSet();
        text.addAttribute(StyleConstants.Foreground, Color.BLUE);
        text.addAttribute(StyleConstants.Background, Color.WHITE);
        attributeMap.put("ID", text);
        attributeMap.put("Tc", text);
        attributeMap.put("Tw", text);
        attributeMap.put("Tz", text);
        attributeMap.put("TL", text);
        attributeMap.put("Tf", text);
        attributeMap.put("Tr", text);
        attributeMap.put("Ts", text);
        attributeMap.put("Td", text);
        attributeMap.put("TD", text);
        attributeMap.put("Tm", text);
        attributeMap.put("T*", text);
        attributeMap.put("Tj", text);
        attributeMap.put("'", text);
        attributeMap.put("\"", text);
        attributeMap.put("TJ", text);
    }

    private static void addGraphicsOps(Map<String, AttributeSet> attributeMap) {
        final MutableAttributeSet graphicsdelim = new SimpleAttributeSet();
        graphicsdelim.addAttribute(StyleConstants.Foreground, Color.WHITE);
        graphicsdelim.addAttribute(StyleConstants.Background, Color.RED);
        graphicsdelim.addAttribute(StyleConstants.Bold, Boolean.TRUE);
        attributeMap.put("q", graphicsdelim);
        attributeMap.put("Q", graphicsdelim);

        final MutableAttributeSet graphics = new SimpleAttributeSet();
        graphics.addAttribute(StyleConstants.Foreground, Color.RED);
        graphics.addAttribute(StyleConstants.Background, Color.WHITE);
        attributeMap.put("w", graphics);
        attributeMap.put("J", graphics);
        attributeMap.put("j", graphics);
        attributeMap.put("M", graphics);
        attributeMap.put("d", graphics);
        attributeMap.put("ri", graphics);
        attributeMap.put("i", graphics);
        attributeMap.put("gs", graphics);
        attributeMap.put("cm", graphics);
        attributeMap.put("g", graphics);
        attributeMap.put("G", graphics);
        attributeMap.put("rg", graphics);
        attributeMap.put("RG", graphics);
        attributeMap.put("k", graphics);
        attributeMap.put("K", graphics);
        attributeMap.put("cs", graphics);
        attributeMap.put("CS", graphics);
        attributeMap.put("sc", graphics);
        attributeMap.put("SC", graphics);
        attributeMap.put("scn", graphics);
        attributeMap.put("SCN", graphics);
        attributeMap.put("sh", graphics);
    }

    public static AttributeSet getStyleAttributesFor(final String operator) {
        final AttributeSet attr = ATTRIBUTE_MAP.get(operator);
        return attr == null ? DEFAULT_ATTRS : attr;
    }

    public static AttributeSet getImageAttributes(final BufferedImage img, final byte[] rawBytes) {
        final MutableAttributeSet imageAttrs = new SimpleAttributeSet();
        final String alt = String.format(
                RupsConfiguration.INSTANCE.getUserLocale(), Language.INLINE_IMAGE_ALT.getString(), img.getWidth(),
                img.getHeight()
        );
        StyleConstants.setIcon(imageAttrs, new ImageIcon(img, alt));
        imageAttrs.addAttribute(ContentStreamStyleConstants.BINARY_CONTENT, rawBytes);
        return imageAttrs;
    }

    public static AttributeSet getImageSaveButtonAttributes(final BufferedImage img) {
        final MutableAttributeSet buttonAttrs = new SimpleAttributeSet();
        StyleConstants.setComponent(buttonAttrs, SaveImageAction.createSaveImageButton(img));
        buttonAttrs.addAttributes(DISPLAY_ONLY_ATTRS);
        return buttonAttrs;
    }

    /**
     * Type-safe attribute markers for debugging convenience.
     */
    public static final class Attribute {

        private final String name;

        /**
         * Creates a new {@link Attribute} with the specified
         * diagnostic {@code name}.
         *
         * @param name the name of the new {@link Attribute}
         */
        Attribute(String name) {
            this.name = name;
        }

        /**
         * Return the {@code name} of the attribute.
         *
         * @return the attribute's {@code name}
         */
        public String toString() {
            return this.name;
        }
    }
}
