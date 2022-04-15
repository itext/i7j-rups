package com.itextpdf.rups.view;

import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Locale;

@Category(UnitTest.class)
public class LanguageTest extends ExtendedITextTest {
    private final Language key = Language.ERROR;
    private final Locale dutchLocale = Locale.forLanguageTag("nl-NL");
    private final String expectedEnUS = "Error";
    private final String expectedNlNL = "Fout";

    @Test
    public void englishUSLocaleTest() {
        Language.setLocale(Locale.US);
        String actual = key.getString();
        Assert.assertEquals(expectedEnUS, actual);
    }

    @Test
    public void defaultLocaleTest() {
        Language.setLocale(Locale.getDefault());
        String actual = key.getString();
        Assert.assertEquals(expectedEnUS, actual);
    }

    @Test
    public void changeLocale() {
        String enActual = key.getString();
        Assert.assertNotNull(enActual);
        Language.setLocale(dutchLocale);
        String nlActual = key.getString();
        Assert.assertEquals(expectedNlNL, nlActual);
    }

    @Test
    public void notExistingLocaleTest() {
        Language.setLocale(new Locale("gibberish"));
        String actual = Language.ERROR.getString();
        Assert.assertEquals(expectedEnUS, actual);
    }

    @Test
    public void stringFormatOnLocaleTest() {
        Language.setLocale(Language.getLocale());
        String actual = String.format(Language.PAGE_NUMBER.getString(), 1);
        Assert.assertEquals("Page 1", actual);
    }

    @Test
    public void nullLocaleTest() {
        Language.setLocale(null);
        String actual = Language.ERROR.getString();
        Assert.assertEquals(expectedEnUS, actual);
    }

    @Test
    public void confirmAllEnumsHaveADefaultValueTest() {
        for ( Language key : Language.values() ) {
            String value = key.getString();
            Assert.assertNotNull(value);
            Assert.assertFalse(value.isEmpty());
        }
    }
}
