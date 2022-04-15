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

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.PdfSyntaxParser;
import com.itextpdf.rups.model.PdfSyntaxUtils;
import com.itextpdf.rups.view.Language;

import java.awt.Component;
import java.util.ArrayList;

/**
 * A TableModel in case we want to show a PDF dictionary in a JTable.
 */
public class DictionaryTableModel extends AbstractPdfObjectPanelTableModel {

    private final boolean pluginMode;
    private final PdfSyntaxParser parser;
    /**
     * The owner component on witch will be displayed all messages
     */
    private final Component parent;
    /**
     * The PDF dictionary.
     */
    protected PdfDictionary dictionary;
    /**
     * An ArrayList with the dictionary keys.
     */
    protected ArrayList<PdfName> keys = new ArrayList<>();

    /**
     * Creates the TableModel.
     *
     * @param dictionary the dictionary we want to show
     * @param pluginMode the plugin mode
     * @param parser     the pdf syntax parser
     * @param owner      the owner
     */
    public DictionaryTableModel(PdfDictionary dictionary, boolean pluginMode, PdfSyntaxParser parser, Component owner) {
        this.pluginMode = pluginMode;
        this.dictionary = dictionary;
        this.parser = parser;
        this.parent = owner;
        this.keys.addAll(dictionary.keySet());
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return pluginMode ? 2 : 3;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return pluginMode ? dictionary.size() : dictionary.size() + 1;
    }


    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !pluginMode && columnIndex < 2;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        final int lastRow = keys.size();

        if (rowIndex == lastRow) {
            if (columnIndex == 0) {
                return tempKey;
            }
            if (columnIndex == 1) {
                return tempValue;
            }
        }

        switch (columnIndex) {
            case 0:
                return keys.get(rowIndex);
            case 1:
                return PdfSyntaxUtils.getSyntaxString(dictionary.get(keys.get(rowIndex), false));
            default:
                return null;
        }
    }

    private String tempKey = "/", tempValue = "";

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        final int rowCount = getRowCount();

        if (rowIndex == rowCount - 1) {
            if (columnIndex == 0) {
                tempKey = (String) aValue;
                if (!tempKey.startsWith("/")) {
                    tempKey = "/" + tempKey;
                }
            } else if (columnIndex == 1) {
                tempValue = (String) aValue;
            }
        } else {
            if (!(aValue instanceof String) || "".equalsIgnoreCase(((String) aValue).trim())) {
                LoggerHelper.warn(Language.ERROR_EMPTY_FIELD.getString(), getClass());
                return;
            }
            if (columnIndex == 0) {
                final String key = (String) aValue;

                final PdfName oldName = keys.get(rowIndex);
                final PdfName newName = getCorrectKey(key);
                if (newName == null) {
                    return;
                }

                final PdfObject pdfObject = dictionary.get(oldName, false);
                removeRow(rowIndex);
                addRow(newName, pdfObject);
            } else {
                final String value = (String) aValue;
                final PdfObject newValue = parser.parseString(value, parent);
                if (newValue != null) {
                    final PdfName oldName = keys.get(rowIndex);
                    removeRow(rowIndex);
                    addRow(oldName, newValue);
                }
            }
        }

        fireTableDataChanged();
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Language.DICTIONARY_KEY.getString();
            case 1:
                return Language.DICTIONARY_VALUE.getString();
            case 2:
                return "";
            default:
                return null;
        }
    }

    @Override
    public int getButtonColumn() {
        return 2;
    }

    @Override
    public void removeRow(int rowIndex) {
        fireTableRowsDeleted(rowIndex, rowIndex);
        dictionary.remove(keys.get(rowIndex));
        keys.remove(rowIndex);
        fireTableDataChanged();
    }

    @Override
    public void validateTempRow() {

        if ("".equalsIgnoreCase(tempKey.trim()) || "".equalsIgnoreCase(tempValue.trim())) {
            LoggerHelper.warn(Language.ERROR_EMPTY_FIELD.getString(), getClass());
            return;
        }

        PdfName key = getCorrectKey(tempKey);
        if (key == null) {
            return;
        }

        final PdfObject value = parser.parseString(tempValue, parent);

        if (value != null) {
            if (dictionary.containsKey(key)) {
                LoggerHelper.warn(Language.ERROR_DUPLICATE_KEY.getString(), getClass());
            } else {
                addRow(key, value);

                tempKey = "/";
                tempValue = "";
            }

            fireTableDataChanged();
        }
    }

    private void addRow(PdfName key, PdfObject value) {
        dictionary.put(key, value);
        int index = -1;
        for (PdfName name : dictionary.keySet()) {
            ++index;
            if (name.equals(key)) {
                break;
            }
        }
        keys.add(index, key);
        fireTableRowsInserted(index, index);
    }

    private PdfName getCorrectKey(String value) {
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        final PdfObject result = parser.parseString(value);

        if (result instanceof PdfName) {
            return (PdfName) result;
        }

        LoggerHelper.error(Language.ERROR_KEY_IS_NOT_NAME.getString(), getClass());
        return null;
    }
}
