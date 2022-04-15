/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
package com.itextpdf.rups.view.models;

import javax.swing.table.AbstractTableModel;

/**
 * A reusable TableModel class for tables that implement the
 * JTableAutoModelInterface.
 */
public class JTableAutoModel extends AbstractTableModel {

    /**
     * The table that knows how to model itself.
     */
    protected JTableAutoModelInterface table;

    /**
     * Constructs an auto model for a JTable.
     *
     * @param table a JTable that knows information about its model.
     */
    public JTableAutoModel(JTableAutoModelInterface table) {
        this.table = table;
    }

    /**
     * @return int the number of columns
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return table.getColumnCount();
    }

    /**
     * @return int the number of rows
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return table.getRowCount();
    }

    /**
     * @param columnIndex a column number.
     * @return the name of the column
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int columnIndex) {
        return table.getColumnName(columnIndex);
    }

    /**
     * @param rowIndex    int		a row number
     * @param columnIndex int	a column number
     * @return Object    an object corresponding with a cell in the table
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return table.getValueAt(rowIndex, columnIndex);
    }
}
