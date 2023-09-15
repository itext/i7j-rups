package com.itextpdf.rups.shims;

import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class RupsPdfStringTest {

    @Test
    public void toStringBasicTest() {
        RupsPdfString pdfString = new RupsPdfString("Test");

        Assert.assertEquals("String is () Delimited.", "(Test)", pdfString.toString());
    }

    @Test
    public void toStringHexTest() {
        byte[] hexArray = "Test".getBytes();
        StringBuilder hexString = new StringBuilder("<");
        for (byte b : hexArray) {
            hexString.append(Integer.toHexString(b));
        }
        hexString.append(">");

        RupsPdfString pdfString = new RupsPdfString(hexArray);
        pdfString.setHexWriting(true);

        Assert.assertEquals("String is <> Delimited.", hexString.toString(), pdfString.toString());
    }

    @Test
    public void toStringBalancedTest(){
        String balanced = "Test (of paretheses)";
        RupsPdfString pdfString = new RupsPdfString(balanced);
        Assert.assertEquals("Balanced parens are escaped:", "(Test \\(of paretheses\\))", pdfString.toString());
        // Note: This is optional, but performed this way in iText to avoid too much overhead evaluating the balance of symbols.
    }

    @Test
    public void toStringUnbalancedTest() {
        String unbalanced = "Test :-)";
        RupsPdfString pdfString = new RupsPdfString(unbalanced);
        Assert.assertEquals("Unbalanced parens are escaped:", "(Test :-\\))", pdfString.toString());
    }

    @Test
    public void toStringUnbalancedTest_Two() {
        String unbalanced_two = ")Test :-(";
        RupsPdfString pdfString_two = new RupsPdfString(unbalanced_two);
        Assert.assertEquals("Unbalanced parens are escaped:", "(\\)Test :-\\()", pdfString_two.toString());
    }

    @Test
    public void toStringUnbalancedTest_Three(){
        String unbalanced_three = "@<----(( Robin Hood Test";
        RupsPdfString pdfString_three = new RupsPdfString(unbalanced_three);
        Assert.assertEquals("Unbalanced parens are escaped:", "(@<----\\(\\( Robin Hood Test)", pdfString_three.toString());
    }

}

