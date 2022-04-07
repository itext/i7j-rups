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
package com.itextpdf.rups.view.itext.contentstream;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.source.PdfTokenizer;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.kernel.pdf.PdfResources;
import com.itextpdf.kernel.pdf.canvas.parser.util.PdfCanvasParser;


final class ContentStreamHandlingUtils {
    /**
     * Factory that allows you to create RandomAccessSource files.
     */
    private static final RandomAccessSourceFactory RASF = new RandomAccessSourceFactory();

    /**
     * Symbols permitted when guessing whether a string without obvious encoding
     * is text or not.
     */
    private static final String PERMITTED_SYMBOLS = "<>()\\/?.!{} \n\t";

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private ContentStreamHandlingUtils() {

    }

    static void hexlify(byte[] bytes, StringBuilder sb) {
        for (byte b : bytes) {
            sb.append(HEX_DIGITS[(b >> 4) & 0xf]);
            sb.append(HEX_DIGITS[b & 0xf]);
        }
    }

    static int dropNonHexDigits(CharSequence str, char[] out) {
        int digitsFound = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (isHexDigit(c)) {
                out[digitsFound] = c;
                digitsFound++;
            }
        }
        return digitsFound;
    }

    static String dropNonHexDigits(CharSequence str) {
        char[] out = new char[str.length()];
        int digitsFound = dropNonHexDigits(str, out);
        return new String(out, 0, digitsFound);
    }

    static byte[] unhexlify(CharSequence str) {
        char[] digits = new char[str.length()];
        int digitsFound = dropNonHexDigits(str, digits);

        // ensure we round up
        byte[] result = new byte[(digitsFound + 1) / 2];
        for (int i = 0; i < result.length; i++) {
            int ix = 2 * i;
            int hi = hexDigitValue(digits[ix]);
            // zero pad if the length is off
            int lo = ix + 1 >= digitsFound ? 0 : hexDigitValue(digits[ix + 1]);
            result[i] = (byte) (((hi << 4) + lo) & 0xff);
        }
        return result;
    }

    static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
    }

    static byte[] ensureEscaped(byte[] stringContent) {
        // re-escape the string to make sure it decodes properly
        //  (have to do it this way since we can't access the original "raw" underlying content)
        byte[] escapedString = StreamUtil.createBufferedEscapedString(stringContent).toByteArray();
        // drop the surrounding parentheses
        byte[] innerEsc = new byte[escapedString.length - 2];
        System.arraycopy(escapedString, 1, innerEsc, 0, innerEsc.length);
        return innerEsc;
    }


    static PdfCanvasParser createCanvasParserFor(final byte[] streamContent) {
        final PdfTokenizer tok = new PdfTokenizer(new RandomAccessFileOrArray(RASF.createSource(streamContent)));
        return new PdfCanvasParser(tok, new PdfResources());
    }

    static boolean hasUtf16beBom(byte[] b) {
        return b.length >= 2 && b[0] == (byte) 0xFE && b[1] == (byte) 0xFF;
    }

    static boolean hasUtf8Bom(byte[] b) {
        return b.length >= 3 && b[0] == (byte)0xEF && b[1] == (byte)0xBB && b[2] == (byte)0xBF;
    }

    static boolean isMaybePdfDocEncodedText(byte[] b) {
        // No explicit encoding -> make an attempt with PDFDocEncoding
        String asPdfDoc = PdfEncodings.convertToString(b, PdfEncodings.PDF_DOC_ENCODING);
        // check whether the result looks like a sensible text string
        for (int i = 0; i < asPdfDoc.length(); i++) {
            char c = asPdfDoc.charAt(i);
            if(!Character.isLetterOrDigit(c) && PERMITTED_SYMBOLS.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    private static int hexDigitValue(char c) {
        return (c <= '9' ? (c - '0') : (0xa + (c - 'a')));
    }
}
