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
package com.itextpdf.rups.view.itext;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.NodeAddArrayChildEvent;
import com.itextpdf.rups.event.NodeAddDictChildEvent;
import com.itextpdf.rups.event.NodeDeleteArrayChildEvent;
import com.itextpdf.rups.event.NodeDeleteDictChildEvent;
import com.itextpdf.rups.event.NodeUpdateArrayChildEvent;
import com.itextpdf.rups.event.NodeUpdateDictChildEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.io.PdfObjectTreeEdit;
import com.itextpdf.rups.model.PdfSyntaxParser;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.icons.IconFetcher;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.models.AbstractPdfObjectPanelTableModel;
import com.itextpdf.rups.view.models.DictionaryTableModel;
import com.itextpdf.rups.view.models.DictionaryTableModelButton;
import com.itextpdf.rups.view.models.PdfArrayTableModel;

import java.awt.CardLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class PdfObjectPanel extends Observable implements Observer {

    private static final String ADD_ICON = "add.png";
    private static final String CROSS_ICON = "cross.png";

    /**
     * Name of a panel in the CardLayout.
     */
    private static final String TABLE = Language.TABLE.getString();

    /**
     * Name of a panel in the CardLayout.
     */
    private static final String TEXT = Language.TEXT.getString();

    private final PdfReaderController controller;

    /**
     * The layout that will show the info about the PDF object that is being analyzed.
     */
    protected CardLayout layout = new CardLayout();

    protected UndoManager undoManager;

    /**
     * Table with dictionary entries.
     */
    JTable table = new JTable();
    /**
     * The text pane with the info about a PDF object in the bottom panel.
     */
    JTextArea text = new JTextArea();

    private final JPanel panel = new JPanel();

    private PdfObjectTreeNode target;


    /**
     * Creates a PDF object panel.
     */
    public PdfObjectPanel(PdfReaderController controller) {

        this.controller = controller;

        // panel name
        panel.setName("PdfObjectPanel");

        table.setName("PdfObjectTable");

        // layout
        panel.setLayout(layout);

        // dictionary / array / stream
        final JScrollPane dictScrollPane = new JScrollPane();
        dictScrollPane.setViewportView(table);
        dictScrollPane.setName("dictScrollPane");
        panel.add(dictScrollPane, TABLE);

        // number / string / ...
        final JScrollPane textScrollPane = new JScrollPane();
        textScrollPane.setViewportView(text);
        textScrollPane.setName("textScrollPane");
        text.setEditable(false);
        panel.add(textScrollPane, TEXT);

        table.addMouseListener(new JTableButtonMouseListener());

        this.undoManager = new UndoManager();
        this.undoManager.setLimit(8192);

        //TODO: Check if WHEN_IN_FOCUSED_WINDOW bleeds across open docs
        panel.registerKeyboardAction(new UndoAction(undoManager),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        panel.registerKeyboardAction(new RedoAction(undoManager),
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Clear the object panel.
     */
    public void clear() {
        target = null;
        text.setText(null);
        layout.show(panel, TEXT);
    }

    public JPanel getPanel() {
        return panel;
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (observable instanceof PdfReaderController && obj instanceof RupsEvent) {
            clear();
        }
    }

    /**
     * Shows a PdfObject as text or in a table.
     *
     * @param node   the node's content that needs to be shown.
     * @param parser the pdf syntax parser
     */
    public void render(PdfObjectTreeNode node, PdfSyntaxParser parser) {
        target = node;
        final PdfObject pdfObjectClone = node.getPdfObject().clone();
        if (pdfObjectClone == null) {
            text.setText(null);
            layout.show(panel, TEXT);
            panel.repaint();
            text.repaint();
            return;
        }

        DictionaryTableModelButton rowButtons = new DictionaryTableModelButton(IconFetcher.getIcon(CROSS_ICON), IconFetcher.getIcon(ADD_ICON));
        rowButtons.setName("rowButton");
        switch (pdfObjectClone.getType()) {
            case PdfObject.DICTIONARY:
            case PdfObject.STREAM:
                final DictionaryTableModel model =
                        new DictionaryTableModel((PdfDictionary) pdfObjectClone, parser, panel);
                model.addTableModelListener(new DictionaryModelListener());
                table.setModel(model);
                table.getColumn("").setCellRenderer(rowButtons);
                layout.show(panel, TABLE);
                panel.repaint();
                table.revalidate();
                break;
            case PdfObject.ARRAY:
                final PdfArrayTableModel arrayModel =
                        new PdfArrayTableModel((PdfArray) pdfObjectClone, parser, panel);
                arrayModel.addTableModelListener(new ArrayModelListener());
                table.setModel(arrayModel);
                table.getColumn("").setCellRenderer(rowButtons);
                layout.show(panel, TABLE);
                panel.repaint();
                table.revalidate();
                break;
            case PdfObject.STRING:
                text.setText(((PdfString) pdfObjectClone).toUnicodeString());
                layout.show(panel, TEXT);
                break;
            default:
                text.setText(pdfObjectClone.toString());
                layout.show(panel, TEXT);
                break;
        }
    }

    private class JTableButtonMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            final int selectedColumn = table.getSelectedColumn();

            if (selectedColumn != ((AbstractPdfObjectPanelTableModel) table.getModel()).getButtonColumn()) {
                return;
            }

            final int selectedRow = table.getSelectedRow();
            final int rowCount = table.getRowCount();

            if (rowCount == 1 || rowCount - 1 == selectedRow) {
                ((AbstractPdfObjectPanelTableModel) table.getModel()).validateTempRow();
                return;
            }

            /*Checking the row or column is valid or not*/
            if (selectedRow < rowCount - 1 && selectedRow >= 0 && target != null) {
                ((AbstractPdfObjectPanelTableModel) table.getModel()).removeRow(selectedRow);
            }
        }
    }

    /**
     * Notify PdfReader Controller about changes in DictionaryModel
     */
    private class DictionaryModelListener implements TableModelListener {
        @Override
        public void tableChanged(TableModelEvent e) {
            final int row = e.getFirstRow();
            if (row != e.getLastRow()) {
                return;
            }
            final PdfName key = (PdfName) table.getValueAt(row, 0);
            final PdfObject value;
            RupsEvent notification = null;
            switch (e.getType()) {
                case TableModelEvent.DELETE:
                    notification = new NodeDeleteDictChildEvent(key, target);
                    break;
                case TableModelEvent.UPDATE:
                    value = ((PdfDictionary) ((DictionaryTableModel) table.getModel()).getPdfObject()).get(key, false);
                    notification = new NodeUpdateDictChildEvent(key, value, target, row);
                    break;
                case TableModelEvent.INSERT:
                    value = ((PdfDictionary) ((DictionaryTableModel) table.getModel()).getPdfObject()).get(key, false);
                    notification = new NodeAddDictChildEvent(key, value, target, row);
                    break;
            }
            if (notification == null) {
                return;
            }
            undoManager.addEdit(new PdfObjectTreeEdit(controller, notification));
            PdfObjectPanel.this.setChanged();
            PdfObjectPanel.this.notifyObservers(notification);
        }
    }

    /**
     * Notify PdfReader Controller about changes in ArrayModel
     */
    private class ArrayModelListener implements TableModelListener {
        @Override
        public void tableChanged(TableModelEvent e) {
            final int row = e.getFirstRow();
            if (row != e.getLastRow()) {
                return;
            }
            // TODO: Maybe abstract the PDF object used for backing the UI elements from those backing the document tree.
            RupsEvent notification = null;
            final PdfObject value;
            switch (e.getType()) {
                case TableModelEvent.DELETE:
                    notification = new NodeDeleteArrayChildEvent(row, target);
                    break;
                case TableModelEvent.UPDATE:
                    value = ((PdfArray) ((PdfArrayTableModel) table.getModel()).getPdfObject()).get(row, false);
                    notification = new NodeUpdateArrayChildEvent(value, target, row);
                    break;
                case TableModelEvent.INSERT:
                    value = ((PdfArray) ((PdfArrayTableModel) table.getModel()).getPdfObject()).get(row, false);
                    notification = new NodeAddArrayChildEvent(value, target, row);
                    break;
            }
            if (notification == null) {
                return;
            }
            undoManager.addEdit(new PdfObjectTreeEdit(controller, notification));
            PdfObjectPanel.this.setChanged();
            PdfObjectPanel.this.notifyObservers(notification);
        }
    }
}
