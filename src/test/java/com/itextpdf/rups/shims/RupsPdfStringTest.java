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

        Assertions.assertEquals("<Test>", pdfString.toString(), "String is <> Delimited.");
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

