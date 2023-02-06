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
}

