package com.itextpdf.rups.io.filters;

import com.itextpdf.rups.view.Language;
import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Category(UnitTest.class)
public class PdfFilterTest {

    @Test
    public void normalTest() {
        PdfFilter filter = new PdfFilter();
        File file = new File("file.pdf");

        boolean actual = filter.accept(file);

        Assert.assertTrue(actual);
    }

    @Test
    public void noPdfExtension() {
        PdfFilter filter = new PdfFilter();
        File file = new File("file.xml");

        boolean actual = filter.accept(file);

        Assert.assertFalse(actual);
    }

    @Test
    public void directoryTest() {
        PdfFilter filter = new PdfFilter();
        File file = new File("file/");

        boolean actual = filter.accept(file);

        Assert.assertFalse(actual);
    }

    @Test
    public void nullTest() {
        PdfFilter filter = new PdfFilter();

        boolean actual = filter.accept(null);

        Assert.assertFalse(actual);
    }

    @Test
    public void descriptionTest() {
        PdfFilter filter = new PdfFilter();
        Assert.assertEquals(Language.FILE_FILTER_DESCRIPTION.getString(), filter.getDescription());
    }

}
