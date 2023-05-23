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
