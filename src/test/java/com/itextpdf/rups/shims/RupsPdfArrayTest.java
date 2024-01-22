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
