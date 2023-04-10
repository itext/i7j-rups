/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

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
package com.itextpdf.rups.view.itext.contentstream;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(Parameterized.class)
@Category(UnitTest.class)
public class PdfDocEncodingHeuristicTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> cases = new ArrayList<>();
        String[] positiveStrings = new String[] {
                "abccadslk fjds",
                "abccadslk\tfjds",
                "abccadslk\nfjds",
                "abccadslk\rfjds",
                "/+xy1209837a$^!@$#&#*!&dksjfao7210",
                "/+xy120921312½",
                "en_US", "en-US",
                "test@example.com",
                "© iText Software",
                "Bär"
        };

        byte[][] positiveBytes = new byte[][] {
                new byte[] { 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x68, 0x65, 0x6c, 0x6c, 0x6f },
                new byte[] { 0x68, 0x65, 0x6c, 0x6c, 0x6f, (byte) 0x92, 0x68, 0x65, 0x6c, 0x6c, 0x6f }
        };

        String[] negativeStrings = new String[] {
                "©z®z",
                "/+xy2½",
                "abccadslk\ffjds", // linefeed is whitespace, but undefined in PDFDocEncoding
                "Hello\007world" // non-whitespace control character
        };

        byte[][] negativeBytes = new byte[][] {
                // utf8 rendering of ä doesn't represent a letter in PDFDocEncoding
                "Bär".getBytes(StandardCharsets.UTF_8),
                // proportion of non-letter bytes too high
                new byte[] { 0x68, 0x65, 0x6c, 0x6c, 0x6f, (byte) 0x92},
                // no non-letter bytes at all
                new byte[] { 0x01, 0x02, 0x03, 0x04 },
                // contains control character that isn't whitespace
                new byte[] { 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x01, 0x68, 0x65, 0x6c, 0x6c, 0x6f }
        };

        for(String s : positiveStrings) {
            cases.add(new Object[] {
                    PdfEncodings.convertToBytes(s, PdfEncodings.PDF_DOC_ENCODING),
                    true
            });
        }

        for(byte[] b : positiveBytes) {
            cases.add(new Object[] {b, true});
        }

        for(String s : negativeStrings) {
            cases.add(new Object[] {
                    PdfEncodings.convertToBytes(s, PdfEncodings.PDF_DOC_ENCODING),
                    false
            });
        }

        for(byte[] b : negativeBytes) {
            cases.add(new Object[] {b, false});
        }

        return cases;
    }

    private final byte[] encoded;
    private final boolean textExpected;

    public PdfDocEncodingHeuristicTest(byte[] encoded, boolean textExpected) {
        this.encoded = encoded;
        this.textExpected = textExpected;
    }

    @Test
    public void testPdfDocTextHeuristic() {
        boolean result = ContentStreamHandlingUtils.isMaybePdfDocEncodedText(this.encoded);

        String asPdfDoc = PdfEncodings.convertToString(this.encoded, PdfEncodings.PDF_DOC_ENCODING);
        Assert.assertEquals(asPdfDoc, this.textExpected, result);
    }

}
