package com.itextpdf.rups.model;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfString;

public class PdfSyntaxUtils {

    private static volatile StringBuilder stringBuilder;

    public static synchronized String getSyntaxString(PdfObject object) {
        stringBuilder = new StringBuilder();
        safeAppendSyntaxString(object);
        return stringBuilder.toString();
    }

    private static void appendSyntaxString(PdfString string) {
        stringBuilder
                .append("(")
                .append(string.toUnicodeString())
                .append(")");
    }

    private static void appendSyntaxString(PdfIndirectReference reference) {
        stringBuilder
                .append(reference.getObjNumber())
                .append(" ")
                .append(reference.getGenNumber())
                .append(" R");
    }


    private static void appendSyntaxString(PdfArray array) {
        stringBuilder.append("[ ");
        for (int i = 0; i < array.size(); ++i) {
            safeAppendSyntaxString(array.get(i, false));
            stringBuilder.append(" ");
        }
        stringBuilder.append("]");
    }

    private static void appendSyntaxString(PdfDictionary dictionary) {
        stringBuilder.append("<< ");
        for (PdfName key : dictionary.keySet()) {
            safeAppendSyntaxString(key);
            stringBuilder.append(" ");
            safeAppendSyntaxString(dictionary.get(key, false));
            stringBuilder.append(" ");
        }
        stringBuilder.append(">>");
    }

    private static void appendSyntaxString(PdfObject object) {
        stringBuilder.append(object.toString());
    }

    private static void safeAppendSyntaxString(PdfObject object) {
        if (object != null) {
            switch (object.getType()) {
                case PdfObject.STREAM:
                case PdfObject.DICTIONARY:
                    appendSyntaxString((PdfDictionary) object);
                    break;
                case PdfObject.ARRAY:
                    appendSyntaxString((PdfArray) object);
                    break;
                case PdfObject.STRING:
                    appendSyntaxString((PdfString) object);
                    break;
                case PdfObject.INDIRECT_REFERENCE:
                    appendSyntaxString((PdfIndirectReference) object);
                    break;
                default:
                    appendSyntaxString(object);
                    break;
            }
        }
    }
}
