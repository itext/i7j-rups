package com.itextpdf.rups.view.icons;

import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.swing.Icon;

@Category(UnitTest.class)
public class IconFetcherTest {

    @Test
    public void fileNameNullTest() {
        Assert.assertNull(IconFetcher.getIcon(null));
    }

    @Test
    public void iconFoundTest() {
        Icon icon = IconFetcher.getIcon("add.png");
        Assert.assertNotNull(icon);
    }

    @Test
    public void iconNotFoundTest() {
        Icon icon = IconFetcher.getIcon("notfound.png");
        Assert.assertNull(icon);
    }

}
