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

import com.itextpdf.kernel.pdf.PdfDocument;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * An interface for holding opened {@link PdfDocument} objects.
 */
public interface IPdfFile {
    /**
     * Return the original PDF document location as a {@link File} object.
     *
     * @return the original PDF document location as a {@link File} object
     */
    File getOriginalFile();

    /**
     * Returns the iText {@link PdfDocument} object, contained within this
     * wrapper.
     *
     * @return a {@link PdfDocument} object
     */
    PdfDocument getPdfDocument();

    /**
     * Returns {@code true}, if the PDF document was opened as an "Owner" (i.e.
     * with full permissions on document manipulation). Returns {@code false}
     * otherwise.
     *
     * @return {@code true}, if the PDF document was opened as an "Owner";
     *         {@code false} otherwise
     */
    default boolean isOpenedAsOwner() {
        return getByteArrayOutputStream() != null;
    }

    /**
     * Returns a byte array, which contains the original raw data of the opened
     * PDF document.
     *
     * @return a byte array, which contains the original raw data of the opened
     *         PDF document
     */
    byte[] getOriginalContent();

    /**
     * Returns the output byte stream, which contains the modified PDF document.
     * Can be {@code null}, if the document is not opened as "Owner".
     *
     * @return the output byte stream, which contains the modified PDF document,
     *         or {@code null} if the document is not opened as "Owner"
     *
     * @see #isOpenedAsOwner()
     */
    ByteArrayOutputStream getByteArrayOutputStream();
}
