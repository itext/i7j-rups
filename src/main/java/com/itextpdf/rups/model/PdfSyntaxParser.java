/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.rups.model;

import com.itextpdf.commons.exceptions.ITextException;
import com.itextpdf.io.source.PdfTokenizer;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfLiteral;
import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.canvas.parser.util.PdfCanvasParser;
import com.itextpdf.rups.view.Language;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PdfSyntaxParser {
    private int openArraysCount = 0;
    private int openDictionaryCount = 0;

    private PdfDocument document;
    private boolean isValid = true;
    private final List<PdfLiteral> unrecognizedChunks = new LinkedList<>();

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
        final byte[] bytesToParse = s.getBytes();
        final RandomAccessSourceFactory factory = new RandomAccessSourceFactory();
        final PdfTokenizer tokenizer =
                new PdfTokenizer(new RandomAccessFileOrArray(factory.createSource(bytesToParse)));
        final UnderlineParser parser = new UnderlineParser(tokenizer);
        PdfObject result;
        try {
            result = parser.readObject();
            if (parser.nextValidToken()) {
                LoggerHelper.warn(Language.ERROR_TRUNCATED_INPUT.getString(), getClass());
            }
            if (openArraysCount != 0) {
                throw new ITextException(Language.ERROR_INCORRECT_ARRAY_BRACKETS.getString());
            }
            if (openDictionaryCount != 0) {
                throw new ITextException(Language.ERROR_INCORRECT_DICTIONARY_BRACKETS.getString());
            }
            if (!isValid) {
                int input = JOptionPane.showConfirmDialog(
                        requester,
                        getUnknownValues() + Language.KEEP_CHUNKS_AS_LITERALS.getString(),
                        Language.TITLE_UNRECOGNIZED_CHUNKS.getString(),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (input != JOptionPane.YES_OPTION || failOnError) {
                    result = null;
                }
            }
        } catch (IOException | RuntimeException any) {
            LoggerHelper.warn(Language.ERROR_PARSING_PDF_OBJECT.getString(), any, getClass());
            result = null;
        }
        return result;
    }

    private String getUnknownValues() {
        final StringBuilder builder = new StringBuilder();
        for (final PdfLiteral literal : unrecognizedChunks) {
            builder.append(literal.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    private void addUnknownValue(PdfLiteral value) {
        LoggerHelper.warn(Language.ERROR_UNEXPECTED_SYNTAX.getString() + " : " + value, getClass());
        unrecognizedChunks.add(value);
        isValid = false;
    }

    private class UnderlineParser extends PdfCanvasParser {

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
            final PdfObject tempObject = super.readObject();
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
                        if (document == null) {
                            throw new ITextException(Language.ERROR_EDITING_UNSPECIFIED_DOCUMENT.getString());
                        }
                        return document.getPdfObject(getTokeniser().getObjNr()).getIndirectReference();
                    default:
                        if (getTokeniser().tokenValueEqualsTo(PdfTokenizer.Null)) {
                            return PdfNull.PDF_NULL;
                        }
                        if (getTokeniser().tokenValueEqualsTo(PdfTokenizer.True)) {
                            return PdfBoolean.TRUE;
                        }
                        if (getTokeniser().tokenValueEqualsTo(PdfTokenizer.False)) {
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
                    throw new ITextException(
                            getTokeniser().getTokenType() + Language.ERROR_ILLEGAL_CHUNK.getString());
                case EndOfFile:
                    return false;
                default:
                    break;
            }
            return true;
        }
    }
}
