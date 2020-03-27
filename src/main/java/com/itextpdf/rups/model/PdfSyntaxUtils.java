/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
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
package com.itextpdf.rups.model;

import com.itextpdf.kernel.pdf.*;

public class PdfSyntaxUtils {

    public static synchronized String getSyntaxString(PdfObject object) {
        StringBuilder stringBuilder = new StringBuilder();
        safeAppendSyntaxString(object, stringBuilder);
        return stringBuilder.toString();
    }

    private static void appendSyntaxString(PdfString string, StringBuilder stringBuilder) {
        stringBuilder
                .append("(")
                .append(string.toUnicodeString())
                .append(")");
    }

    private static void appendSyntaxString(PdfIndirectReference reference, StringBuilder stringBuilder) {
        stringBuilder
                .append(reference.getObjNumber())
                .append(" ")
                .append(reference.getGenNumber())
                .append(" R");
    }


    private static void appendSyntaxString(PdfArray array, StringBuilder stringBuilder) {
        stringBuilder.append("[ ");
        for (int i = 0; i < array.size(); ++i) {
            safeAppendSyntaxString(array.get(i, false), stringBuilder);
            stringBuilder.append(" ");
        }
        stringBuilder.append("]");
    }

    private static void appendSyntaxString(PdfDictionary dictionary, StringBuilder stringBuilder) {
        stringBuilder.append("<< ");
        for (PdfName key : dictionary.keySet()) {
            safeAppendSyntaxString(key, stringBuilder);
            stringBuilder.append(" ");
            safeAppendSyntaxString(dictionary.get(key, false), stringBuilder);
            stringBuilder.append(" ");
        }
        stringBuilder.append(">>");
    }

    private static void appendSyntaxString(PdfObject object, StringBuilder stringBuilder) {
        stringBuilder.append(object.toString());
    }

    private static void safeAppendSyntaxString(PdfObject object, StringBuilder stringBuilder) {
        if (object != null) {
            switch (object.getType()) {
                case PdfObject.STREAM:
                case PdfObject.DICTIONARY:
                    appendSyntaxString((PdfDictionary) object, stringBuilder);
                    break;
                case PdfObject.ARRAY:
                    appendSyntaxString((PdfArray) object, stringBuilder);
                    break;
                case PdfObject.STRING:
                    appendSyntaxString((PdfString) object, stringBuilder);
                    break;
                case PdfObject.INDIRECT_REFERENCE:
                    appendSyntaxString((PdfIndirectReference) object, stringBuilder);
                    break;
                default:
                    appendSyntaxString(object, stringBuilder);
                    break;
            }
        }
    }
}
