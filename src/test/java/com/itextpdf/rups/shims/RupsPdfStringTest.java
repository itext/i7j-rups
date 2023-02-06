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

        Assert.assertEquals("String is () Delimited.","(Test)", pdfString.toString());
    }
}
