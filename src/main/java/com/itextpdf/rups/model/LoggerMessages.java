/*
    This file is part of the iText (R) project.
    Copyright (c) 2007-2018 iText Group NV
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

public class LoggerMessages {
    //Logger messages
    public static final String CLOSING_STREAM_ERROR = "Can't close stream";
    public static final String CREATE_COMPARE_DOC_ERROR = "Can't open document for comparison";
    public static final String WAITING_FOR_LOADER_ERROR = "Can't finish loading";
    public static final String INVOKING_RUNNABLE_ERROR = "Can't invoke runnable";
    public static final String RUNNABLE_CAUSE_EXCEPTION = "Runnable invoke exception";
    public static final String FINDING_ICON_ERROR = "Can't find file: ";
    public static final String WRITING_FILE_ERROR = "Can't write to file";
    public static final String GETTING_PDF_STREAM_BYTES_ERROR = "Can't get PdfStream's bytes";
    public static final String PARENT_NODE_NULL_ERROR = "Parent node is null for ";
    public static final String XFA_LOADING_ERROR = "Can't load XFA";
    public static final String XML_DOM_PARSING_ERROR = "Can't parse xml";
    public static final String UNEXPECTED_EXCEPTION_DEFAULT = "Unexpected exception";
    public static final String COMPARING_ERROR = "Unhandled exception while comparing documents";
    public static final String REFLECTION_PDFSTREAM_ERROR = "Reflection error from PdfStream. Editing of pdfStreams will be disabled";
    public static final String PDFSTREAM_PARSING_ERROR = "Error while parsing PdfStream";
    public static final String IMAGE_PARSING_ERROR = "Error while parsing Image";
    public static final String CANNOT_PARSE_PDF_OBJECT = "Error while parsing pdf syntax";
    public static final String UNEXPECTED_CHUNK_OF_SYNTAX = "Unexpected chunk of pdf syntax";
    public static final String INVALID_CHUNK_OF_SYNTAX = "Invalid chunk of pdf syntax";
    public static final String KEY_ALREADY_EXIST = "This key already exist in dictionary. Please edit existing entry.";
    public static final String FIELD_IS_EMPTY = "Don't leave fields empty.";
    public static final String PARSED_INPUT_WAS_TRUNCATED = "The input string was truncated.";
    public static final String KEY_ISNT_PDFNAME = "Key value isn't value Name object.";
    public static final String NOT_AN_INTEGER_INDEX = "The typed index isn't integer.";
    public static final String NOT_IN_RANGE_INDEX = "The typed index is not in range.";
    public static final String REFLECTION_INVOCATION_PDFSTREAM_ERROR = "Cannot check for null inputStream from PdfStream";
    public static final String NO_OPEN_DOCUMENT = "There is no open document. Nothing to compare with";
    public static final String COMPARED_DOCUMENT_IS_NULL = "Compared document is null";
    public static final String COMPARED_DOCUMENT_IS_CLOSED = "Compared document is closed";

    //Exception messages
    public static final String CHUNK_OF_THIS_TYPE_NOT_ALLOWED_HERE = " - the chunk of this type not allowed here.";
    public static final String EDITING_REFERENCE_NO_DOCUMENT_ERROR = "Trying to edit references when no document was specified.";
    public static final String INCORRECT_SEQUENCE_OF_ARRAY_BRACKETS = "Incorrect sequence of array brackets";
    public static final String INCORRECT_SEQUENCE_OF_DICTIONARY_BRACKETS = "Incorrect sequence of dictionary brackets";
}
