/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.rups.shims;

import com.itextpdf.kernel.pdf.PdfString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
@Tag("String")
public class RupsPdfStringTest {

    @Test
    public void toStringBasicTest() {
        PdfString initialString = new PdfString("Test");
        RupsPdfString pdfString = new RupsPdfString(initialString);
        Assertions.assertEquals("(Test)", pdfString.toString(), "String is () Delimited.");
    }

    @Test
    public void toStringHexTest() {
        byte[] hexArray = "Test".getBytes();
        StringBuilder hexString = new StringBuilder("<");
        for (byte b : hexArray) {
            hexString.append(Integer.toHexString(b));
        }
        hexString.append(">");

        PdfString initialString = new PdfString(hexArray);
        initialString.setHexWriting(true);

        RupsPdfString pdfString = new RupsPdfString(initialString);

        Assertions.assertEquals(hexString.toString(), pdfString.toString(), "String is <> Delimited.");
    }

    @Test
    public void toStringBalancedTest(){
        String balanced = "Test (of paretheses)";
        PdfString initialString = new PdfString(balanced);
        RupsPdfString pdfString = new RupsPdfString(initialString);
        Assertions.assertEquals("(Test \\(of paretheses\\))", pdfString.toString(), "Balanced parens are escaped:");
        // Note: This is optional, but performed this way in iText to avoid too much overhead evaluating the balance of symbols.
    }

    @Test
    public void toStringUnbalancedTest() {
        String unbalanced = "Test :-)";
        PdfString initialString = new PdfString(unbalanced);
        RupsPdfString pdfString = new RupsPdfString(initialString);
        Assertions.assertEquals("(Test :-\\))", pdfString.toString(), "Unbalanced parens are escaped:");
    }

    @Test
    public void toStringUnbalancedTest_Two() {
        String unbalanced_two = ")Test :-(";
        PdfString initialString = new PdfString(unbalanced_two);
        RupsPdfString pdfString_two = new RupsPdfString(initialString);
        Assertions.assertEquals("(\\)Test :-\\()", pdfString_two.toString(), "Unbalanced parens are escaped:");
    }

    @Test
    public void toStringUnbalancedTest_Three(){
        String unbalanced_three = "@<----(( Robin Hood Test";
        PdfString initialString = new PdfString(unbalanced_three);
        RupsPdfString pdfString_three = new RupsPdfString(initialString);
        Assertions.assertEquals("(@<----\\(\\( Robin Hood Test)", pdfString_three.toString(), "Unbalanced parens are escaped:");
    }

}

