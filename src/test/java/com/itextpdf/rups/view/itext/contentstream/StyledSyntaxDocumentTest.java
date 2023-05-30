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

import com.itextpdf.kernel.pdf.PdfLiteral;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.view.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;

@Tag("UnitTest")
public class StyledSyntaxDocumentTest {

    private static final String SRC_DIR = "./src/test/resources/com/itextpdf/rups/view/itext/contentStreamSnippets";

    @Test
    public void testStringifyCustomAttribute() {
        Assertions.assertEquals("binary-content", ContentStreamStyleConstants.BINARY_CONTENT.toString());
    }

    @Test
    public void testReserializeBaseline() throws Exception {
        checkReserialize("baseline.cmp");
    }

    @Test
    public void testReserializeTJ() throws Exception {
        checkReserialize("simpleTJ.cmp");
    }

    @Test
    public void testReserializeMarkedContent() throws Exception {
        checkReserialize("markedContent.cmp");
    }

    @Test
    public void testReserialiseUtf16be() throws Exception {
        checkReserialize("utf16be.cmp");
    }

    @Test
    public void testPlainStringDecodedInDocument() throws Exception {
        checkDecodedDoc("baseline.cmp", "(Hello world!)");
    }

    @Test
    public void testUtf16beDecodedInDocument() throws Exception {
        checkDecodedDoc("utf16be.cmp", "(こんにちは)");
    }

    @Test
    public void testReserialiseUtf8() throws Exception {
        checkReserialize("utf8.cmp");
    }

    @Test
    public void testUtf8DecodedInDocument() throws Exception {
        checkDecodedDoc("utf8.cmp", "(世界!!!)");
    }

    @Test
    public void testReplaceUtf8InDocument() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "utf8.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf('世');
        doc.replace(start, 2, "違う", doc.getCharacterElement(start).getAttributes());

        byte[] expectedResult = Files.readAllBytes(Paths.get(SRC_DIR, "utf8replaced.cmp"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        byte[] result = baos.toByteArray();
        Assertions.assertArrayEquals(expectedResult, result);
    }

    @Test
    public void testDeleteAndInsertUtf8InDocument() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "utf8.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf('世');
        doc.remove(start, 2);
        doc.insertString(start,"違う", doc.getCharacterElement(start).getAttributes());

        byte[] expectedResult = Files.readAllBytes(Paths.get(SRC_DIR, "utf8replaced.cmp"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        byte[] result = baos.toByteArray();
        Assertions.assertArrayEquals(expectedResult, result);
    }

    @Test
    public void testReserializeStringWithBinaryContent() throws Exception {
        reserializeWithCompareTarget("stringWithBin.cmp", "stringWithBinResult.cmp");
    }

    @Test
    public void testStringWithBinaryContentRendersAsHex() throws Exception {
        checkDecodedDoc("stringWithBin.cmp", "({48656c6c6f00776f726c6421})");
    }

    @Test
    public void testReserialiseInlineImage() throws Exception {
        // Note: this isn't expected to work with abbreviated keys, but that's not a problem,
        // since those shouldn't change the semantics when resolved appropriately.
        checkReserialize("charprocWithInlineImg.cmp");
    }

    @Test
    public void testInlineImageDocContainsPlaceholderWhitespace() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "charprocWithInlineImg.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        Assertions.assertTrue(Pattern.compile("ID\\s+EI").matcher(theText).find());
    }

    @Test
    public void testReserialiseCorruptInlineImage() throws Exception {
        // this inline image contains a totally nonsensical colour space
        // that iText doesn't attempt to process -> we should still be able to reserialise this
        checkReserialize("charprocWithCorruptInlineImg.cmp");
    }

    @Test
    public void testCorruptInlineImageDocContainsPlaceholderErrorText() throws Exception {
        checkDecodedDoc("charprocWithCorruptInlineImg.cmp", "Could not process image content");
    }


    @Test
    public void testBinaryStringDoesNotPermitNonHexReplacement() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("656c6");
        doc.replace(start, 3, "z!!z z", null);
        // this should be treated like a removal since there are no hex chars
        assertSubstring("({48c6c6f00776f726c6421})", doc.getText(0, doc.getLength()));
    }

    @Test
    public void testBinaryStringPermitsRemoval() throws Exception {
        stringRemovalCheck("656c6", 3, "({48c6c6f00776f726c6421})");
    }

    @Test
    public void testBinaryStringPermitsRemovalAtStart() throws Exception {
        stringRemovalCheck("4865", 4, "({6c6c6f00776f726c6421})");
    }

    @Test
    public void testBinaryStringPermitsRemovalAtEnd() throws Exception {
        stringRemovalCheck("6421", 4, "({48656c6c6f00776f726c})");
    }

    @Test
    public void testBinaryStringPermitsNullReplace() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("656c6");
        doc.replace(start, 3, null, null);
        assertSubstring("({48c6c6f00776f726c6421})", doc.getText(0, doc.getLength()));
    }

    @Test
    public void testBinaryStringPermitsNullInsert() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("656c6");
        doc.insertString(start, null, null);
        assertSubstring("({48656c6c6f00776f726c6421})", doc.getText(0, doc.getLength()));
    }

    @Test
    public void testBinaryStringDoesNotPermitNonHexInsertion() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("656c6");
        doc.insertString(start, "quux", null);
        doc.insertString(start, "QUUX", null);
        doc.insertString(start, "z!!z jz", null);
        assertSubstring("({48656c6c6f00776f726c6421})", doc.getText(0, doc.getLength()));
    }

    @Test
    public void testRemovalEndCannotTraverseBinaryString() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("656c6") - 10;
        doc.remove(start, 10);
        assertSubstring("({48656c6c6f00776f726c6421})", doc.getText(0, doc.getLength()));
    }

    @Test
    public void testRemovalStartCannotTraverseBinaryString() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("656c6");
        doc.remove(start, 30);
        assertSubstring("({48656c6c6f00776f726c6421})", doc.getText(0, doc.getLength()));
    }

    @Test
    public void testRemovalCanReplaceBinaryStringEntirely() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("({486");
        doc.replace(start, 29, "(Hello World!) ", null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        String result = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
        assertSubstring("(Hello World!) Tj", result);
    }

    @Test
    public void testMatchingOperandsMode() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "baseline.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.setMatchingOperands(true);
        Assertions.assertTrue(doc.isMatchingOperands());
        doc.processContentStream(origBytes);

        AttributeSet expectedAttributes = doc.getStyleAttributes("Tm");

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("600");
        AttributeSet actualAttributes = doc.getCharacterElement(start).getAttributes();
        Assertions.assertEquals(expectedAttributes, actualAttributes);
    }

    @Test
    public void testAlmostUtf16be() throws Exception {
        checkDecodedDoc("almostUtf16be.cmp", "({fe003053");
    }

    @Test
    public void testReserialiseAlmostUtf16be() throws Exception {
        reserializeWithCompareTarget("almostUtf16be.cmp", "almostUtf16beResult.cmp");
    }

    @Test
    public void testAlmostUtf8_1() throws Exception {
        checkDecodedDoc("almostUtf8_1.cmp", "({ef00bf");
    }

    @Test
    public void testAlmostUtf8_2() throws Exception {
        checkDecodedDoc("almostUtf8_2.cmp", "({efbb00");
    }

    @Test
    public void testBogusEIDoesNotInhibitSyntaxDoc() throws Exception {
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        PdfObject[] bogusOps = new PdfObject[] {
                new PdfString("This makes no sense"), new PdfLiteral("EI")
        };
        PdfObject[] bogusOps2 = new PdfObject[] {
                new PdfString("And neither"),
                new PdfString("does this"),
                new PdfLiteral("EI")
        };
        doc.appendGraphicsOperator(Arrays.asList(bogusOps));
        doc.appendGraphicsOperator(Arrays.asList(bogusOps2));

        byte[] expectedResult = Files.readAllBytes(Paths.get(SRC_DIR, "bogusEI.cmp"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        byte[] result = baos.toByteArray();
        Assertions.assertArrayEquals(expectedResult, result);
    }

    @Test
    public void testBinaryStringEditPaddingTolerance() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        // chop off the final '1', turning the ! into a space
        int start = theText.indexOf("6421");
        doc.remove(start + 3, 1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        String result = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
        assertSubstring("(Hello\\000world )", result);
    }

    @Test
    public void testHexeditSprawlTolerance() throws Exception {
        // this shouldn't arise in normal document editing due to the filter, but let's see how
        // our defenses hold up
        StyledSyntaxDocument doc = new StyledSyntaxDocument();

        PdfObject[] splitString = new PdfObject[] {
                new PdfString("This\000is"),
                new PdfString("\000w31rd"),
                new PdfLiteral("Tj")
        };
        doc.appendGraphicsOperator(Arrays.asList(splitString));

        String theText = doc.getText(0, doc.getLength());
        // editing is not restricted right now!
        // delete the brackets in the middle, merging the strings
        // and insert some garbage chars in between just for fun
        int start = theText.indexOf(") (");
        MutableAttributeSet mattr = new SimpleAttributeSet();
        mattr.addAttribute(ContentStreamStyleConstants.HEX_EDIT, true);
        doc.replace(start, 3, "!.\n\t", mattr);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        String result = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
        Assertions.assertEquals("(This\\000is\\000w31rd) Tj\n\n", result);
    }


    @Test
    public void testHexeditBoundaryTolerance() throws Exception {
        // this shouldn't arise in normal document editing due to the filter, but let's see how
        // our defenses hold up
        StyledSyntaxDocument doc = new StyledSyntaxDocument();

        PdfObject[] splitString = new PdfObject[] {
                new PdfString("This\000is\000w31rd"),
                new PdfLiteral("Tj")
        };
        doc.appendGraphicsOperator(Arrays.asList(splitString));

        String theText = doc.getText(0, doc.getLength());
        // editing is not restricted right now!
        // chop off the bit at the end
        int start = theText.indexOf(") Tj");
        doc.remove(start, 4);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        String result = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
        Assertions.assertEquals("(This\\000is\\000w31rd\n\n", result);
    }

    @Test
    public void testBinaryStringPermitsHexReplacement() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("6f00");
        MutableAttributeSet mattr = new SimpleAttributeSet();
        mattr.addAttribute(ContentStreamStyleConstants.HEX_EDIT, true);
        doc.replace(start, 2, "757575", mattr);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        String result = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
        assertSubstring("(Helluuu\\000world!)", result);
    }

    @Test
    public void testBinaryStringPermitsHexInsertion() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("6f00");
        MutableAttributeSet mattr = new SimpleAttributeSet();
        mattr.addAttribute(ContentStreamStyleConstants.HEX_EDIT, true);
        doc.insertString(start, "6f6f6f", mattr);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        String result = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
        assertSubstring("(Helloooo\\000world!)", result);
    }

    @Test
    public void testBinaryStringPermitsPartialHexInsertion() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("6f00");
        MutableAttributeSet mattr = new SimpleAttributeSet();
        mattr.addAttribute(ContentStreamStyleConstants.HEX_EDIT, true);
        doc.insertString(start, "6fzzzqqq6f!!!6f", mattr);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        String result = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
        assertSubstring("(Helloooo\\000world!)", result);
    }

    @Test
    public void testBinaryStringPermitsHexInsertionAtEnd() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("21}) Tj");
        MutableAttributeSet mattr = new SimpleAttributeSet();
        mattr.addAttribute(ContentStreamStyleConstants.HEX_EDIT, true);
        doc.insertString(start + 2, "31212131", mattr);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        String result = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
        assertSubstring("(Hello\\000world!1!!1)", result);
    }

    @Test
    public void testInlineImageDoesNotPermitInsertion() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "charprocWithInlineImg.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("ID");
        doc.insertString(start + 3, "deadbeef", null);
        Assertions.assertTrue(Pattern.compile("ID\\s+EI").matcher(theText).find());
    }

    @Test
    public void testInlineImageDoesNotPermitInnerRemoval() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "charprocWithInlineImg.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("ID");
        doc.remove(start + 3, 2);
        Assertions.assertTrue(Pattern.compile("ID\\s+EI").matcher(theText).find());
    }

    @Test
    public void testInlineImageDoesNotPermitInnerReplacement() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "charprocWithInlineImg.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf("ID");
        doc.replace(start + 3, 2, "deadbeef", null);
        Assertions.assertTrue(Pattern.compile("ID\\s+EI").matcher(theText).find());
    }

    private void assertSubstring(String expectedSub, String theText) {
        Assertions.assertTrue(
                theText.contains(expectedSub),
                "Text '" + theText + "' did not contain expected string '" + expectedSub + "'"
        );
    }

    private void checkReserialize(String inputFile) throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, inputFile));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        byte[] result = baos.toByteArray();
        Assertions.assertArrayEquals(origBytes, result);
    }

    private void reserializeWithCompareTarget(String src, String cmp) throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, src));
        byte[] expectedResult = Files.readAllBytes(Paths.get(SRC_DIR, cmp));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ContentStreamWriter(baos).write(doc);
        byte[] result = baos.toByteArray();

        Assertions.assertArrayEquals(expectedResult, result);
    }

    private void checkDecodedDoc(String fname, String expectedSubstring) throws Exception {

        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, fname));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        assertSubstring(expectedSubstring, theText);
    }

    private void stringRemovalCheck(String marker, int len, String expectedSub) throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String theText = doc.getText(0, doc.getLength());
        int start = theText.indexOf(marker);
        doc.remove(start, len);
        assertSubstring(expectedSub, doc.getText(0, doc.getLength()));
    }

    @Test
    public void testEncodingTooltip() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "utf16be.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        int start = doc.getText(0, doc.getLength()).indexOf("こんにちは");
        String expected = String.format(Language.TOOLTIP_ENCODING.getString(), "UnicodeBig");
        Assertions.assertEquals(expected, doc.getToolTipAt(start));
    }

    @Test
    public void testHexTooltip() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);


        int start = doc.getText(0, doc.getLength()).indexOf("({") + 3;

        String expected = Language.TOOLTIP_HEX.getString();
        Assertions.assertEquals(expected, doc.getToolTipAt(start));
    }

    @Test
    public void testNoTooltip() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "stringWithBin.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        int start = doc.getText(0, doc.getLength()).indexOf("Tj");
        Assertions.assertNull(doc.getToolTipAt(start));
    }

    @Test
    public void testIndent() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "baseline.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String output = doc.getText(0, doc.getLength());
        String expectedResult = new String(Files.readAllBytes(Paths.get(SRC_DIR, "baselineIndented.cmp")));
        Assertions.assertEquals(expectedResult, output);
    }

    @Test
    public void testIndentNested() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "nested.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String output = doc.getText(0, doc.getLength());
        String expectedResult = new String(Files.readAllBytes(Paths.get(SRC_DIR, "nestedIndented.cmp")));
        Assertions.assertEquals(expectedResult, output);
    }

    @Test
    public void testIndentInlineImage() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "charprocWithInlineImg.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String output = doc.getText(0, doc.getLength());
        String expectedResult = new String(Files.readAllBytes(Paths.get(SRC_DIR, "charprocWithInlineImgIndented.cmp")));
        Assertions.assertEquals(expectedResult, output);
    }

    @Test
    public void testIndentCorruptInlineImage() throws Exception {
        byte[] origBytes = Files.readAllBytes(Paths.get(SRC_DIR, "charprocWithCorruptInlineImg.cmp"));
        StyledSyntaxDocument doc = new StyledSyntaxDocument();
        doc.processContentStream(origBytes);

        String output = doc.getText(0, doc.getLength());
        String expectedResult = new String(Files.readAllBytes(Paths.get(SRC_DIR, "charprocWithCorruptInlineImgIndented.cmp")));
        Assertions.assertEquals(expectedResult, output);
    }
}
