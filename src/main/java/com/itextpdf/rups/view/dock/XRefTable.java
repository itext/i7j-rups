/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
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
package com.itextpdf.rups.view.dock;

import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.controller.RupsInstanceController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.IndirectObjectFactory;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.models.JTableAutoModel;
import com.itextpdf.rups.view.models.JTableAutoModelInterface;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;
import java.util.Observable;
import java.util.Observer;

/**
 * A JTable that shows the indirect objects of a PDF xref table.
 */
public class XRefTable extends JTable implements JTableAutoModelInterface, Observer {

    /**
     * The factory that can produce all the indirect objects.
     */
    protected IndirectObjectFactory objects;
    /**
     * The renderer that will render an object when selected in the table.
     */
    protected PdfReaderController controller;

    /**
     * Creates a JTable visualizing xref table.
     *
     * @param controller the pdf reader controller
     * @param loader the ObjectLoader that loads the references
     */
    public XRefTable(PdfReaderController controller, ObjectLoader loader) {
        super();
        this.controller = controller;
        objects = loader.getObjects();
        setModel(new JTableAutoModel(this));
        final TableColumn col = getColumnModel().getColumn(0);
        col.setPreferredWidth(5);
        repaint();
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (observable instanceof RupsInstanceController && obj instanceof RupsEvent) {
            final RupsEvent event = (RupsEvent) obj;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                    objects = null;
                    setModel(new JTableAutoModel(this));
                    repaint();
                    return;
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    final ObjectLoader loader = (ObjectLoader) event.getContent();
                    objects = loader.getObjects();
                    setModel(new JTableAutoModel(this));
                    final TableColumn col = getColumnModel().getColumn(0);
                    col.setPreferredWidth(5);
                    break;
                case RupsEvent.POST_NEW_INDIRECT_OBJECT_EVENT:
                    setModel(new JTableAutoModel(this));
                    break;
            }
        }
    }

    /**
     * @see javax.swing.JTable#getColumnCount()
     */
    public int getColumnCount() {
        return 2;
    }

    /**
     * @see javax.swing.JTable#getRowCount()
     */
    public int getRowCount() {
        if (objects == null) return 0;
        return objects.size();
    }

    /**
     * @see javax.swing.JTable#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return getObjectReferenceByRow(rowIndex);
            case 1:
                return getObjectDescriptionByRow(rowIndex);
            default:
                return null;
        }
    }

    /**
     * Gets the reference number of an indirect object
     * based on the row index.
     *
     * @param rowIndex a row number
     * @return a reference number
     */
    protected int getObjectReferenceByRow(int rowIndex) {
        return objects.getRefByIndex(rowIndex);
    }

    /**
     * Gets the object that is shown in a row.
     *
     * @param rowIndex the row number containing the object
     * @return a PDF object
     */
    protected String getObjectDescriptionByRow(int rowIndex) {
        final PdfObject object = objects.getObjectByIndex(rowIndex);
        if (object instanceof PdfNull && !objects.isLoadedByIndex(rowIndex)) {
            return Language.INDIRECT_OBJECT.getString();
        }
        return PdfObjectTreeNode.getCaption(object);
    }

    /**
     * @see javax.swing.JTable#getColumnName(int)
     */
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Language.XREF_NUMBER.getString();
            case 1:
                return Language.XREF_OBJECT.getString();
            default:
                return null;
        }
    }

    /**
     * Gets the object that is shown in a row.
     *
     * @param rowIndex the row number containing the object
     * @return a PDF object
     */
    protected PdfObject getObjectByRow(int rowIndex) {
        return objects.loadObjectByReference(getObjectReferenceByRow(rowIndex));
    }

    /**
     * Selects a row containing information about an indirect object.
     *
     * @param ref the reference number of the indirect object
     */
    public void selectRowByReference(int ref) {
        final int row = objects.getIndexByRef(ref);
        setRowSelectionInterval(row, row);
        scrollRectToVisible(getCellRect(row, 1, true));
        valueChanged(null);
    }

    /**
     * @see javax.swing.JTable#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (evt != null) {
            super.valueChanged(evt);
        }
        if (controller != null && objects != null) {
            controller.selectNode(getObjectReferenceByRow(this.getSelectedRow()));
        }
    }
}
