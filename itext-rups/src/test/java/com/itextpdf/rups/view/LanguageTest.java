/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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
package com.itextpdf.rups.view;

import com.itextpdf.rups.RupsConfiguration;
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
        RupsConfiguration.INSTANCE.setUserLocale(Locale.US);
        String actual = key.getString();
        Assert.assertEquals(expectedEnUS, actual);
    }

    @Test
    public void defaultLocaleTest() {
        RupsConfiguration.INSTANCE.setUserLocale(Locale.getDefault());
        String actual = key.getString();
        Assert.assertEquals(expectedEnUS, actual);
    }

    @Test
    public void changeLocale() {
        Locale userLocale = RupsConfiguration.INSTANCE.getUserLocale();
        String enActual = key.getString();
        Assert.assertNotNull(enActual);
        RupsConfiguration.INSTANCE.setUserLocale(dutchLocale);
        RupsConfiguration.INSTANCE.saveConfiguration();
        String nlActual = key.getString();
        RupsConfiguration.INSTANCE.setUserLocale(userLocale);
        RupsConfiguration.INSTANCE.saveConfiguration();
        Assert.assertEquals(expectedNlNL, nlActual);
    }

    @Test
    public void notExistingLocaleTest() {
        RupsConfiguration.INSTANCE.setUserLocale(new Locale("gibberish"));
        String actual = Language.ERROR.getString();
        Assert.assertEquals(expectedEnUS, actual);
    }

    @Test
    public void stringFormatOnLocaleTest() {
        RupsConfiguration.INSTANCE.setUserLocale(Locale.getDefault());
        String actual = String.format(Language.PAGE_NUMBER.getString(), 1);
        Assert.assertEquals("Page 1", actual);
    }

    @Test
    public void nullLocaleTest() {
        RupsConfiguration.INSTANCE.setUserLocale(null);
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
