package com.itextpdf.rups.view.itext;

import com.itextpdf.io.source.PdfTokenizer;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Console;

import java.io.IOException;
import java.util.Arrays;

/**
 * Utility class to parse ObjectStreams to extract the offset of a given object id within the stream.
 */
public class ObjectStreamParser {

    /**
     * Parses an ObjectStream to find the offset to the passed parameter, compressedObjectNumber. This offset is
     * relative to the ObjectStream and not to the complete file, as described in the specification.
     *
     * If an Object ID isn't found inside the ObjectStream, this will return -1.
     *
     * @param objStm The ObjectStream to parse
     * @param compressedObjectNumber the ID of the object of which you want the offset
     * @return the offset of the object or -1 if the object is not found
     */
    public int parseObjectStream(PdfStream objStm, int compressedObjectNumber) {
        byte[] objStmBytes = objStm.getBytes(true);
        int byteOffsetOfFirst = objStm.getAsInt(PdfName.First);

        PdfTokenizer pdfTokenizer = new PdfTokenizer(
                new RandomAccessFileOrArray(
                        new RandomAccessSourceFactory()
                                .createSource(
                                        Arrays.copyOfRange(objStmBytes, 0, byteOffsetOfFirst))));

        try {
            while (pdfTokenizer.nextToken()) {
                if ( pdfTokenizer.getTokenType().equals(PdfTokenizer.TokenType.Number )) {
                    int objNumber = pdfTokenizer.getIntValue();
                    pdfTokenizer.nextToken();
                    if ( objNumber == compressedObjectNumber ) {
                        return pdfTokenizer.getIntValue();
                    }
                }
            }
        } catch (IOException e) {
            LoggerHelper.error(e.getMessage(), e, ObjectStreamParser.class);
            return -1;
        }

        return -1;
    }
}
