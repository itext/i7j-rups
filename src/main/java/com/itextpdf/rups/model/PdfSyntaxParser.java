package com.itextpdf.rups.model;

import com.itextpdf.io.source.PdfTokenizer;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.canvas.parser.util.PdfCanvasParser;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfSyntaxParser {

    private PdfDocument document;

    public void setDocument(PdfDocument document) {
        this.document = document;
    }

    public PdfObject parseString(String s) throws IOException {
        byte[] bytesToParse = s.getBytes();
        RandomAccessSourceFactory factory = new RandomAccessSourceFactory();
        PdfTokenizer tokenizer = new PdfTokenizer(new RandomAccessFileOrArray(factory.createSource(bytesToParse)));
        UnderlineParser parser = new UnderlineParser(tokenizer, document);
        return parser.readObject();
    }

    private class UnderlineParser extends PdfCanvasParser {

        private PdfDocument document;

        /**
         * Creates a new instance of PdfContentParser
         *
         * @param tokenizer the tokenizer with the content
         */
        public UnderlineParser(PdfTokenizer tokenizer, PdfDocument document) {
            super(tokenizer);
            this.document = document;
        }

        /**
         * Reads a pdf object.
         *
         * @return the pdf object
         * @throws IOException on error
         */
        @Override
        public PdfObject readObject() throws IOException {
            PdfObject tempObject = super.readObject();
            //Additional checks
            final PdfTokenizer.TokenType type = getTokeniser().getTokenType();
            if (tempObject.getType() == PdfObject.LITERAL) {
                if (getTokeniser().getTokenType() == PdfTokenizer.TokenType.Ref) {
                    if (document == null)
                        throw new RuntimeException(LoggerMessages.EDITING_REFERENCE_NO_DOCUMENT_ERROR);
                    return document.getPdfObject(getTokeniser().getObjNr()).getIndirectReference();
                } else {
                    if (getTokeniser().tokenValueEqualsTo(PdfTokenizer.Null)) {
                        return PdfNull.PDF_NULL;
                    } else if (getTokeniser().tokenValueEqualsTo(PdfTokenizer.True)) {
                        return PdfBoolean.TRUE;
                    } else if (getTokeniser().tokenValueEqualsTo(PdfTokenizer.False)) {
                        return new PdfBoolean(false);
                    }
                    Logger logger = LoggerFactory.getLogger(PdfSyntaxParser.class);
                    logger.warn(LoggerMessages.UNEXPECTED_CHUNK_OF_SYNTAX + " : " + new String(getTokeniser().getByteContent()));
                }
            }
            return tempObject;
        }

        @Override
        public boolean nextValidToken() throws IOException {
            getTokeniser().nextValidToken();
            if (getTokeniser().getTokenType() != PdfTokenizer.TokenType.EndOfFile) {
                return true;
            }
            return false;
        }
    }

}