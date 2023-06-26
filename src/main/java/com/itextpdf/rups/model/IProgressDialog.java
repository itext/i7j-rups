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

/**
 * Interface for an informational dialog window showing the progress of a certain action.
 */
public interface IProgressDialog {
    /**
     * Changes the message describing what's in progress.
     *
     * @param msg the message describing what's in progress
     */
    void setMessage(String msg);

    /**
     * Changes the value of the progress bar.
     *
     * @param value the current value
     */
    void setValue(int value);

    /**
     * Sets the maximum value for the progress bar.
     * If 0 or less, sets the progress bar to indeterminate mode.
     *
     * @param n the maximum value for the progress bar
     */
    void setTotal(int n);

    /**
     * Displays an error dialog for the given exception.
     *
     * @param ex exception to display information about
     */
    void showErrorDialog(Exception ex);

    /**
     * Control the visibility of the progress dialog.
     *
     * @param visible visibility status of the dialog
     */
    void setVisible(boolean visible);
}