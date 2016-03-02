/*
 * $Id$
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 2007-2015 iText Group NV
 * Authors: Bruno Lowagie et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.rups.view.models;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfLiteral;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * A TableModel in case we want to show a PDF dictionary in a JTable.
 */
public class DictionaryTableModel extends AbstractTableModel {

    /**
     * A serial version UID.
     */
    private static final long serialVersionUID = -8835275996639701776L;
    /**
     * The PDF dictionary.
     */
    protected PdfDictionary dictionary;
    /**
     * An ArrayList with the dictionary keys.
     */
    protected ArrayList<PdfName> keys = new ArrayList<PdfName>();

    /**
     * Creates the TableModel.
     *
     * @param dictionary the dictionary we want to show
     */
    public DictionaryTableModel(PdfDictionary dictionary) {
        this.dictionary = dictionary;
        for (PdfName n : dictionary.keySet())
            this.keys.add(n);
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 3;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return dictionary.size() + 1;
    }


    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex < 2;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        int lastRow = keys.size();

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
                return dictionary.get(keys.get(rowIndex), false);
            default:
                return null;
        }
    }

    private String tempKey, tempValue;

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        int rowCount = getRowCount();

        if (rowIndex == rowCount - 1) {
            if (columnIndex == 0) {
                tempKey = (String) aValue;
            } else if (columnIndex == 1) {
                tempValue = (String) aValue;
            }
        } else {
            if ( columnIndex == 0) {
                String key = (String) aValue;

                if ( key.contains("/")) {
                    key = key.replace("/", "");
                }

                PdfName oldName = keys.get(rowIndex);
                PdfName newName = new PdfName(key);
                keys.set(rowIndex, newName);

                PdfObject pdfObject = dictionary.get(oldName, false);
                dictionary.remove(oldName);
                dictionary.put(newName, pdfObject);
            } else {
                // todo improve situation here
                String value = (String) aValue;
                PdfObject pdfObject = dictionary.get(keys.get(rowIndex), false);

                if ( pdfObject instanceof PdfArray) {
                    value = value.replaceAll(",", "");
                }

                dictionary.put(keys.get(rowIndex), new PdfLiteral(value));
            }
        }
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Key";
            case 1:
                return "Value";
            case 2:
                return "";
            default:
                return null;
        }
    }

    public void removeRow(int rowNumber) {
        PdfName name = keys.get(rowNumber);
        keys.remove(rowNumber);
        dictionary.remove(name);

        fireTableDataChanged();
    }

    public void addRow(String keyField, String valueField) {
        if ( keyField.startsWith("/")) {
            keyField = keyField.replace("/", "");
        }

        PdfName newEntry = new PdfName(keyField);

        if ( !dictionary.containsKey(newEntry)) {
            dictionary.put(newEntry, new PdfLiteral(valueField));
            keys.add(newEntry);
        }

        fireTableDataChanged();

        tempKey = "";
        tempValue = "";
    }
}
