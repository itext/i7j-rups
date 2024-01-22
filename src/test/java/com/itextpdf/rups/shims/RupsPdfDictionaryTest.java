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
@Tag("Dictionary")
public class RupsPdfDictionaryTest {

    @Test
    public void BasicNameDictionaryTest(){
        PdfDictionary initialDict = new PdfDictionary();
        PdfName initialKeyOne = new PdfName("KeyOne");
        PdfName initialValueOne = new PdfName("ValueOne");
        PdfName initialKeyTwo = new PdfName("KeyTwo");
        PdfName initialValueTwo = new PdfName("ValueTwo");

        initialDict.put(initialKeyOne, initialValueOne);
        initialDict.put(initialKeyTwo, initialValueTwo);

        RupsPdfDictionary pdfDictionary = new RupsPdfDictionary(initialDict);

        Assertions.assertEquals("<</KeyOne /ValueOne/KeyTwo /ValueTwo>>", pdfDictionary.toString(), "PDF Dictionary fails to Serialize correctly.");
    }
    @Test
    public void BasicStringDictionaryTest(){
        PdfDictionary initialDict = new PdfDictionary();
        PdfName initialKeyOne = new PdfName("KeyOne");
        PdfString initialValueOne = new PdfString("ValueOne");
        PdfName initialKeyTwo = new PdfName("KeyTwo");
        PdfString initialValueTwo = new PdfString("ValueTwo");

        initialDict.put(initialKeyOne, initialValueOne);
        initialDict.put(initialKeyTwo, initialValueTwo);

        RupsPdfDictionary pdfDictionary = new RupsPdfDictionary(initialDict);

        Assertions.assertEquals("<</KeyOne (ValueOne)/KeyTwo (ValueTwo)>>", pdfDictionary.toString(), "PDF Dictionary fails to Serialize correctly.");
    }
    @Test
    public void HexStringDictionaryTest(){
        PdfDictionary initialDict = new PdfDictionary();
        PdfName initialKeyOne = new PdfName("KeyOne");
        byte[] hexArrayOne = "ValueOne".getBytes();
        PdfString initialValueOne = new PdfString(hexArrayOne);
        initialValueOne.setHexWriting(true);
        PdfName initialKeyTwo = new PdfName("KeyTwo");
        byte[] hexArrayTwo = "ValueTwo".getBytes();
        PdfString initialValueTwo = new PdfString(hexArrayTwo);
        initialValueTwo.setHexWriting(true);

        initialDict.put(initialKeyOne, initialValueOne);
        initialDict.put(initialKeyTwo, initialValueTwo);

        String hexStringOne = "<";
        for (byte b: hexArrayOne)
            hexStringOne = hexStringOne.concat(Integer.toHexString(b));
        hexStringOne = hexStringOne.concat(">");

        String hexStringTwo = "<";
        for (byte b: hexArrayTwo)
            hexStringTwo = hexStringTwo.concat(Integer.toHexString(b));
        hexStringTwo = hexStringTwo.concat(">");

        RupsPdfDictionary pdfDictionary = new RupsPdfDictionary(initialDict);

        Assertions.assertEquals(String.format("<</KeyOne %1$s/KeyTwo %2$s>>", hexStringOne, hexStringTwo), pdfDictionary.toString(), "PDF Dictionary fails to Serialize correctly.");
    }
    @Test
    public void HexStringArrayDictionaryTest(){
        PdfDictionary initialDict = new PdfDictionary();
        PdfArray initialValue = new PdfArray();

        PdfName initialKeyOne = new PdfName("KeyOne");

        byte[] hexArrayOne = "ValueOne".getBytes();
        PdfString hexValueOne = new PdfString(hexArrayOne);
        hexValueOne.setHexWriting(true);
        initialValue.add(hexValueOne);

        byte[] hexArrayTwo = "ValueTwo".getBytes();
        PdfString hexValueTwo = new PdfString(hexArrayTwo);
        hexValueTwo.setHexWriting(true);
        initialValue.add(hexValueTwo);


        initialDict.put(initialKeyOne, initialValue);

        String hexStringOne = "";
        for (byte b: hexArrayOne)
            hexStringOne = hexStringOne.concat(Integer.toHexString(b));

        String hexStringTwo = "";
        for (byte b: hexArrayTwo)
            hexStringTwo = hexStringTwo.concat(Integer.toHexString(b));

        RupsPdfDictionary pdfDictionary = new RupsPdfDictionary(initialDict);

        Assertions.assertEquals(String.format("<</KeyOne [<%1$s><%2$s>]>>", hexStringOne, hexStringTwo), pdfDictionary.toString(), "PDF Dictionary fails to Serialize correctly.");
    }

    @Test
    public void NameAndNameArrayDictionaryTest(){
        PdfDictionary initialDict = new PdfDictionary();
        PdfArray subArray = new PdfArray();
        PdfName initialKeyOne = new PdfName("KeyOne");
        PdfName initialValueOne = new PdfName("ValueOne");
        PdfName initialKeyTwo = new PdfName("KeyTwo");
        PdfName initialValueTwo = new PdfName("ValueTwo");

        subArray.add(initialValueOne);
        subArray.add(initialValueTwo);

        initialDict.put(initialKeyOne, initialValueOne);
        initialDict.put(initialKeyTwo, subArray);

        RupsPdfDictionary pdfDictionary = new RupsPdfDictionary(initialDict);

        Assertions.assertEquals("<</KeyOne /ValueOne/KeyTwo [/ValueOne/ValueTwo]>>", pdfDictionary.toString(), "PDF Dictionary fails to Serialize correctly.");
    }
    @Test
    public void NestedNameDictionaryTest(){
        PdfDictionary initialDict = new PdfDictionary();
        PdfDictionary subDictOne = new PdfDictionary();
        PdfDictionary subDictTwo = new PdfDictionary();
        PdfName initialKeyOne = new PdfName("KeyOne");
        PdfName initialValueOne = new PdfName("ValueOne");
        PdfName initialKeyTwo = new PdfName("KeyTwo");
        PdfName initialValueTwo = new PdfName("ValueTwo");

        subDictOne.put(initialKeyOne, initialValueOne);
        subDictOne.put(initialKeyTwo, initialValueTwo);

        subDictTwo.put(initialKeyOne, initialValueOne);
        subDictTwo.put(initialKeyTwo, initialValueTwo);

        initialDict.put(initialKeyOne, subDictOne);
        initialDict.put(initialKeyTwo, subDictTwo);

        RupsPdfDictionary pdfDictionary = new RupsPdfDictionary(initialDict);

        Assertions.assertEquals("<</KeyOne <</KeyOne /ValueOne/KeyTwo /ValueTwo>>/KeyTwo <</KeyOne /ValueOne/KeyTwo /ValueTwo>>>>", pdfDictionary.toString(), "PDF Dictionary fails to Serialize correctly.");
    }

}
