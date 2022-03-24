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

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.LoggerMessages;
import com.itextpdf.rups.model.PdfSyntaxParser;
import com.itextpdf.rups.model.PdfSyntaxUtils;

import javax.swing.*;
import java.awt.*;

/**
 * A TableModel in case we want to show a PDF array in a JTable.
 */
public class PdfArrayTableModel extends AbstractPdfObjectPanelTableModel {

    /**
     * The PDF array.
     */
    protected PdfArray array;

    private boolean pluginMode;
    private PdfSyntaxParser parser;
    /**
     * The owner component on witch will be displayed all messages
     */
    private Component parent;

    private String tempValue = "";

    /**
     * Creates the TableModel.
     *
     * @param array      a PDF array
     * @param pluginMode the plugin mode
     * @param parser     the pdf syntax parser
     * @param parent     the parent
     */
    public PdfArrayTableModel(PdfArray array, boolean pluginMode, PdfSyntaxParser parser, Component parent) {
        this.array = array;
        this.pluginMode = pluginMode;
        this.parser = parser;
        this.parent = parent;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !pluginMode && columnIndex < 1;
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return pluginMode ? 1 : 2;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return array.size() + 1;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if (rowIndex == array.size()) return tempValue;
            return PdfSyntaxUtils.getSyntaxString(array.get(rowIndex, false));
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        int rowCount = getRowCount();

        if (rowIndex == rowCount - 1) {
            if (columnIndex == 0) {
                tempValue = (String) aValue;
            }
        } else {
            if (!(aValue instanceof String) || "".equalsIgnoreCase(((String) aValue).trim())) {
                LoggerHelper.warn(LoggerMessages.FIELD_IS_EMPTY, getClass());
                return;
            }
            if (columnIndex == 0) {
                String value = (String) aValue;
                PdfObject newValue = parser.parseString(value, parent);
                if (newValue != null) {
                    removeRow(rowIndex);
                    addRow(rowIndex, newValue);
                }
            }
        }
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Array";
            case 1:
                return "";
            default:
                return null;
        }
    }

    @Override
    public void removeRow(int rowIndex) {
        fireTableRowsDeleted(rowIndex, rowIndex);
        array.remove(rowIndex);
        fireTableDataChanged();
    }

    @Override
    public void validateTempRow() {
        if ("".equalsIgnoreCase(tempValue.trim())) {
            LoggerHelper.warn(LoggerMessages.FIELD_IS_EMPTY, getClass());
            return;
        }

        PdfObject value = parser.parseString(tempValue, parent);

        if (value != null) {
            int index;
            while (true) {
                String result = JOptionPane.showInputDialog(parent, "Choose array index", array.size());
                if (result == null) {
                    //canceled input
                    return;
                }
                try {
                    index = Integer.valueOf(result);
                } catch (NumberFormatException any) {
                    LoggerHelper.warn(LoggerMessages.NOT_AN_INTEGER_INDEX, any, getClass());
                    continue;
                }
                if (0 <= index && index <= array.size()) {
                    //correct input
                    break;
                } else {
                    LoggerHelper.warn(LoggerMessages.NOT_IN_RANGE_INDEX, getClass());
                }
            }
            addRow(index, value);
            tempValue = "";
            fireTableDataChanged();
        }
    }

    @Override
    public int getButtonColumn() {
        return 1;
    }

    private void addRow(int index, PdfObject value) {
        array.add(index, value);
        fireTableRowsInserted(index, index);
    }

}

