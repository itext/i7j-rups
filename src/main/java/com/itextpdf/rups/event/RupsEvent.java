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
package com.itextpdf.rups.event;

/**
 * Abstract Event Class that other events should extend from.
 */
public abstract class RupsEvent {

    public static final byte OPEN_FILE_EVENT = 0;
    public static final byte OPEN_DOCUMENT_POST_EVENT = 1;
    public static final byte CLOSE_DOCUMENT_EVENT = 2;
    public static final byte SAVE_TO_FILE_EVENT = 3;
    public static final byte COMPARE_WITH_FILE_EVENT = 4;
    public static final byte COMPARE_POST_EVENT = 5;
    public static final byte ROOT_NODE_CLICKED_EVENT = 6;
    public static final byte TREE_NODE_CLICKED_EVENT = 7;
    public static final byte OPEN_STRUCTURE_EVENT = 8;
    public static final byte OPEN_PLAIN_TEXT_EVENT = 9;
    public static final byte CONSOLE_WRITE_EVENT = 10;
    public static final byte NODE_ADD_DICT_CHILD_EVENT = 11;
    public static final byte NODE_DELETE_DICT_CHILD_EVENT = 12;
    public static final byte NODE_ADD_ARRAY_CHILD_EVENT = 13;
    public static final byte NODE_DELETE_ARRAY_CHILD_EVENT = 14;
    public static final byte NEW_INDIRECT_OBJECT_EVENT = 15;
    public static final byte POST_NEW_INDIRECT_OBJECT_EVENT = 16;

    public abstract int getType();

    public abstract Object getContent();
}
