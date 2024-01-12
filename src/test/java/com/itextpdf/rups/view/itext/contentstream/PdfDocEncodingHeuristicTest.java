/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

@Tag("UnitTest")
public class PdfDocEncodingHeuristicTest {

    public static Stream<Arguments> data() {

        return Stream.of(
                // Positive Strings
                Arguments.of(PdfEncodings.convertToBytes("abccadslk fjds", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("abccadslk\tfjds", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("abccadslk\nfjds", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("abccadslk\rfjds", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("/+xy1209837a$^!@$#&#*!&dksjfao7210", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("/+xy120921312½", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("en_US", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("en-US", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("test@example.com", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("© iText Software", PdfEncodings.PDF_DOC_ENCODING), true),
                Arguments.of(PdfEncodings.convertToBytes("Bär", PdfEncodings.PDF_DOC_ENCODING), true),

                // Positive Byte Arrays
                Arguments.of(new byte[]{0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x68, 0x65, 0x6c, 0x6c, 0x6f}, true),
                Arguments.of(new byte[]{0x68, 0x65, 0x6c, 0x6c, 0x6f, (byte) 0x92, 0x68, 0x65, 0x6c, 0x6c, 0x6f}, true),

                // Negative Strings
                Arguments.of(PdfEncodings.convertToBytes("©z®z", PdfEncodings.PDF_DOC_ENCODING), false),
                Arguments.of(PdfEncodings.convertToBytes("/+xy2½", PdfEncodings.PDF_DOC_ENCODING), false),
                // linefeed is whitespace, but undefined in PDFDocEncoding
                Arguments.of(PdfEncodings.convertToBytes("abccadslk\ffjds", PdfEncodings.PDF_DOC_ENCODING), false),
                // non-whitespace control character
                Arguments.of(PdfEncodings.convertToBytes("Hello\007world", PdfEncodings.PDF_DOC_ENCODING), false),

                // Negative Byte Arrays
                // utf8 rendering of ä doesn't represent a letter in PDFDocEncoding
                Arguments.of("Bär".getBytes(StandardCharsets.UTF_8), false),
                // proportion of non-letter bytes too high
                Arguments.of(new byte[]{0x68, 0x65, 0x6c, 0x6c, 0x6f, (byte) 0x92}, false),
                // no non-letter bytes at all
                Arguments.of(new byte[]{0x01, 0x02, 0x03, 0x04}, false),
                // contains control character that isn't whitespace
                Arguments.of(new byte[]{0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x01, 0x68, 0x65, 0x6c, 0x6c, 0x6f}, false)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testPdfDocTextHeuristic(byte[] encoded, boolean textExpected) {
        boolean result = ContentStreamHandlingUtils.isMaybePdfDocEncodedText(encoded);

        String asPdfDoc = PdfEncodings.convertToString(encoded, PdfEncodings.PDF_DOC_ENCODING);
        Assertions.assertEquals(textExpected, result, asPdfDoc);
    }
}
