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
package com.itextpdf.rups.view.itext;

import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.view.icons.IconFetcher;
import com.itextpdf.rups.view.models.DictionaryTableModel;
import com.itextpdf.rups.view.models.DictionaryTableModelButton;
import com.itextpdf.rups.view.models.PdfArrayTableModel;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class PdfObjectPanel extends JPanel implements Observer {

	/** Name of a panel in the CardLayout. */
	private static final String TEXT = "text";
	/** Name of a panel in the CardLayout. */
	private static final String TABLE = "table";
	
	/** The layout that will show the info about the PDF object that is being analyzed. */
	protected CardLayout layout = new CardLayout();

	/** Table with dictionary entries. */
	JTable table = new JTable();
	/** The text pane with the info about a PDF object in the bottom panel. */
	JTextArea text = new JTextArea();

    private JTableButtonMouseListener mouseListener;
	
	/** Creates a PDF object panel. */
	public PdfObjectPanel() {
		// layout
		setLayout(layout);

		// dictionary / array / stream
		JScrollPane dict_scrollpane = new JScrollPane();
		dict_scrollpane.setViewportView(table);
		add(dict_scrollpane, TABLE);
		
		// number / string / ...
		JScrollPane text_scrollpane = new JScrollPane();
		text_scrollpane.setViewportView(text);
		add(text_scrollpane, TEXT);

        mouseListener = new JTableButtonMouseListener(table);
        table.addMouseListener(mouseListener);
	}
	
	/**
	 * Clear the object panel.
	 */
	public void clear() {
		text.setText(null);
		layout.show(this, TEXT);
	}

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object obj) {
		clear();
	}
	
	/**
	 * Shows a PdfObject as text or in a table.
	 * @param object	the object that needs to be shown.
	 */
	public void render(PdfObject object) {
		if (object == null) {
			text.setText(null);
			layout.show(this, TEXT);
			this.repaint();
			text.repaint();
			return;
		}
		switch(object.getType()) {
		case PdfObject.Dictionary:
		case PdfObject.Stream:
			table.setModel(new DictionaryTableModel((PdfDictionary)object));
            table.getColumn("").setCellRenderer(new DictionaryTableModelButton(IconFetcher.getIcon("cross.png"), IconFetcher.getIcon("add.png")));
			layout.show(this, TABLE);
			this.repaint();
			break;
		case PdfObject.Array:
			table.setModel(new PdfArrayTableModel((PdfArray)object));
			layout.show(this, TABLE);
			this.repaint();
			break;
		case PdfObject.String:
			text.setText(((PdfString)object).toUnicodeString());
			layout.show(this, TEXT);
			break;
		default:
			text.setText(object.toString());
			layout.show(this, TEXT);
			break;
		}
	}
	
	/** a serial version id. */
	private static final long serialVersionUID = 1302283071087762494L;

    private class JTableButtonMouseListener extends MouseAdapter {
        private final JTable table;

        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }

        public void mouseClicked(MouseEvent e) {
            int selectedColumn = table.getSelectedColumn();

            if ( selectedColumn != 2 ) {
                return;
            }

            int selectedRow    = table.getSelectedRow();
            int rowCount = table.getRowCount();

            if ( rowCount == 1 || rowCount -1 == selectedRow ) {
                // check if two fields are empty or not
                String keyField = (String) table.getValueAt(selectedRow, 0);
                String valueField = (String) table.getValueAt(selectedRow, 1);

                if ( keyField  == null || "".equalsIgnoreCase(keyField.trim()) ) {
                    return;
                }

				if ( valueField == null || "".equalsIgnoreCase(valueField.trim()) ) {
					return;
				}

				//Todo: add type chooser dialog when stream and array modification will be implemented
                /*Map<String, Byte> choiceMap = new HashMap<String, Byte>(9);
                choiceMap.put("Boolean", PdfObject.Boolean);
                choiceMap.put("Number", PdfObject.Number);
                choiceMap.put("String", PdfObject.String);
                choiceMap.put("Name", PdfObject.Name);
                choiceMap.put("Array", PdfObject.Array);
                choiceMap.put("Dictionary", PdfObject.Dictionary);
                choiceMap.put("Stream", PdfObject.Stream);

                String[] choices = new String[choiceMap.size()];
                choiceMap.keySet().toArray(choices);

                int defaultChoice = 0; // perhaps add some processing of the input to add to the UX

                String input = (String) JOptionPane.showInputDialog(table, "What is the type of the new value?", "Value Type", JOptionPane.QUESTION_MESSAGE, null, choices, choices[defaultChoice]);

                if ( input == null ) { // user cancelled input
                    return;
                }*/

                // call addRow
                ((DictionaryTableModel) table.getModel()).addRow(keyField, valueField);

                return;
            }

            /*Checking the row or column is valid or not*/
            if (selectedRow < rowCount - 1 && selectedRow >= 0 ) {
                ((DictionaryTableModel) table.getModel()).removeRow(selectedRow);
            }
        }
    }
}
