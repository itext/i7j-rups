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

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
@Tag("Array")
public class RupsPdfArrayTest {

    @Test
    public void BasicNameArrayTest(){
        PdfArray initialArray = new PdfArray();

        PdfName initialValueOne = new PdfName("ValueOne");
        PdfName initialValueTwo = new PdfName("ValueTwo");
        initialArray.add(initialValueOne);
        initialArray.add(initialValueTwo);

        RupsPdfArray pdfArray = new RupsPdfArray(initialArray);

        Assertions.assertEquals("[/ValueOne/ValueTwo]", pdfArray.toString(), "PDF Array fails to Serialize correctly.");


    }
    @Test
    public void BasicNestedNameArrayTest(){
        PdfArray initialArray = new PdfArray();

        PdfArray subArrayOne = new PdfArray();
        PdfArray subArrayTwo = new PdfArray();

        PdfName initialValueOne = new PdfName("ValueOne");
        PdfName initialValueTwo = new PdfName("ValueTwo");

        subArrayOne.add(initialValueOne);
        subArrayOne.add(initialValueTwo);

        subArrayTwo.add(initialValueOne);
        subArrayTwo.add(initialValueTwo);

        initialArray.add(subArrayOne);
        initialArray.add(subArrayTwo);

        RupsPdfArray pdfArray = new RupsPdfArray(initialArray);

        Assertions.assertEquals("[[/ValueOne/ValueTwo][/ValueOne/ValueTwo]]", pdfArray.toString(), "PDF Array fails to Serialize correctly.");


    }
    @Test
    public void MixedStringArrayTest(){
        PdfArray initialArray = new PdfArray();
        String valueOne = "ValueOne";
        byte[] hexArrayOne = valueOne.getBytes();
        PdfString initialValueOne = new PdfString(valueOne);
        PdfString hexValueOne = new PdfString(hexArrayOne);
        hexValueOne.setHexWriting(true);

        String valueTwo = "ValueTwo";
        byte[] hexArrayTwo = valueTwo.getBytes();
        PdfString initialValueTwo = new PdfString(valueTwo);
        PdfString hexValueTwo = new PdfString(hexArrayTwo);
        hexValueTwo.setHexWriting(true);
        String hexStringOne = "";
        for (byte b: hexArrayOne)
            hexStringOne = hexStringOne.concat(Integer.toHexString(b));

        String hexStringTwo = "";
        for (byte b: hexArrayTwo)
            hexStringTwo = hexStringTwo.concat(Integer.toHexString(b));

        initialArray.add(initialValueOne);
        initialArray.add(hexValueOne);

        initialArray.add(initialValueTwo);
        initialArray.add(hexValueTwo);

        RupsPdfArray pdfArray = new RupsPdfArray(initialArray);

        Assertions.assertEquals(String.format("[(%1$s)<%2$s>(%3$s)<%4$s>]", initialValueOne, hexStringOne, initialValueTwo, hexStringTwo), pdfArray.toString(), "PDF Array fails to Serialize correctly.");
    }


    @Test
    public void HexStringDictionaryArrayTest(){
        PdfArray initialArray = new PdfArray();

        PdfDictionary  initialDictOne = new PdfDictionary();
        PdfDictionary  initialDictTwo = new PdfDictionary();

        PdfName initialKeyOne = new PdfName("KeyOne");
        PdfName initialKeyTwo = new PdfName("KeyTwo");

        byte[] hexArrayOne = "ValueOne".getBytes();
        PdfString hexValueOne = new PdfString(hexArrayOne);
        hexValueOne.setHexWriting(true);
        initialDictOne.put(initialKeyOne, hexValueOne);


        byte[] hexArrayTwo = "ValueTwo".getBytes();
        PdfString hexValueTwo = new PdfString(hexArrayTwo);
        hexValueTwo.setHexWriting(true);

        initialDictTwo.put(initialKeyTwo, hexValueTwo);

        initialArray.add(initialDictOne);
        initialArray.add(initialDictTwo);

        String hexStringOne = "";
        for (byte b: hexArrayOne)
            hexStringOne = hexStringOne.concat(Integer.toHexString(b));

        String hexStringTwo = "";
        for (byte b: hexArrayTwo)
            hexStringTwo = hexStringTwo.concat(Integer.toHexString(b));

        RupsPdfArray pdfArray = new RupsPdfArray(initialArray);

        Assertions.assertEquals(String.format("[<</KeyOne <%1$s>>><</KeyTwo <%2$s>>>]", hexStringOne, hexStringTwo), pdfArray.toString(), "PDF Array fails to Serialize correctly.");
    }
}
