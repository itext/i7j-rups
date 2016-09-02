package com.itextpdf.rups.model;

import com.itextpdf.io.source.PdfTokenizer;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfLiteral;
import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.canvas.parser.util.PdfCanvasParser;

import java.awt.Component;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdfSyntaxParser {
    private int openArraysCount = 0;
    private int openDictionaryCount = 0;

    private PdfDocument document;
    private boolean isValid = true;
    private List<PdfLiteral> unrecognizedChunks = new LinkedList<>();

    public void setDocument(PdfDocument document) {
        this.document = document;
    }

    public PdfObject parseString(String s, Component requester) {
        return parseString(s, requester, false);
    }

    public PdfObject parseString(String s) {
        return parseString(s, null, true);
    }

    private PdfObject parseString(String s, Component requester, boolean failOnError) {
        isValid = true;
        unrecognizedChunks.clear();
        openArraysCount = 0;
        openDictionaryCount = 0;
        byte[] bytesToParse = s.getBytes();
        RandomAccessSourceFactory factory = new RandomAccessSourceFactory();
        PdfTokenizer tokenizer = new PdfTokenizer(new RandomAccessFileOrArray(factory.createSource(bytesToParse)));
        UnderlineParser parser = new UnderlineParser(tokenizer);
        PdfObject result;
        try {
            result = parser.readObject();
            if (parser.nextValidToken()) {
                Logger logger = LoggerFactory.getLogger(PdfSyntaxParser.class);
                logger.warn(LoggerMessages.PARSED_INPUT_WAS_TRUNCATED);
            }
            if (openArraysCount != 0) {
                throw new RuntimeException(LoggerMessages.INCORRECT_SEQUENCE_OF_ARRAY_BRACKETS);
            }
            if (openDictionaryCount != 0) {
                throw new RuntimeException(LoggerMessages.INCORRECT_SEQUENCE_OF_DICTIONARY_BRACKETS);
            }
            if (!isValid) {
                int input = JOptionPane.showConfirmDialog(
                        requester,
                        getUnknownValues() + "Do you want to keep those chunks as Literals?",
                        "Unrecognized chunks in the input!",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (input != JOptionPane.YES_OPTION || failOnError) {
                    result = null;
                }
            }
        } catch (Exception any) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.warn(LoggerMessages.CANNOT_PARSE_PDF_OBJECT);
            logger.debug(LoggerMessages.CANNOT_PARSE_PDF_OBJECT, any);
            result = null;
        }
        return result;
    }

    private String getUnknownValues() {
        StringBuilder builder = new StringBuilder();
        for (PdfLiteral literal : unrecognizedChunks) {
            builder.append(literal.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    private void addUnknownValue(PdfLiteral value) {
        Logger logger = LoggerFactory.getLogger(PdfSyntaxParser.class);
        logger.warn(LoggerMessages.UNEXPECTED_CHUNK_OF_SYNTAX + " : " + value);
        unrecognizedChunks.add(value);
        isValid = false;
    }

    private class UnderlineParser extends PdfCanvasParser {

        private PdfObject tempObject;

        /**
         * Creates a new instance of PdfContentParser
         *
         * @param tokenizer the tokenizer with the content
         */
        public UnderlineParser(PdfTokenizer tokenizer) {
            super(tokenizer);
        }

        /**
         * Reads a pdf object.
         *
         * @return the pdf object
         * @throws IOException on error
         */
        @Override
        public PdfObject readObject() throws IOException {
            tempObject = super.readObject();
            //Additional checks
            if (tempObject.getType() == PdfObject.LITERAL) {
                final PdfTokenizer.TokenType type = getTokeniser().getTokenType();
                switch (type) {
                    case StartArray:
                    case EndArray:
                    case StartDic:
                    case EndDic:
                        break;
                    case Ref:
                        if (document == null)
                            throw new RuntimeException(LoggerMessages.EDITING_REFERENCE_NO_DOCUMENT_ERROR);
                        return document.getPdfObject(getTokeniser().getObjNr()).getIndirectReference();
                    default:
                        if (getTokeniser().tokenValueEqualsTo(PdfTokenizer.Null)) {
                            return PdfNull.PDF_NULL;
                        } else if (getTokeniser().tokenValueEqualsTo(PdfTokenizer.True)) {
                            return PdfBoolean.TRUE;
                        } else if (getTokeniser().tokenValueEqualsTo(PdfTokenizer.False)) {
                            return new PdfBoolean(false);
                        }
                        addUnknownValue((PdfLiteral) tempObject);
                        break;
                }
            }
            return tempObject;
        }

        @Override
        public boolean nextValidToken() throws IOException {
            getTokeniser().nextValidToken();
            switch (getTokeniser().getTokenType()) {
                case StartArray:
                    ++openArraysCount;
                    break;
                case EndArray:
                    --openArraysCount;
                    break;
                case StartDic:
                    ++openDictionaryCount;
                    break;
                case EndDic:
                    --openDictionaryCount;
                    break;
                case Obj:
                case EndObj:
                    throw new RuntimeException(getTokeniser().getTokenType() + LoggerMessages.CHUNK_OF_THIS_TYPE_NOT_ALLOWED_HERE);
                case EndOfFile:
                    return false;
                default:
                    break;
            }
            return true;
        }
    }
}