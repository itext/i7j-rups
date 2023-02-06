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

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.controller.RupsInstanceController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.TreeNodeFactory;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.PageSelectionListener;
import com.itextpdf.rups.view.itext.PageEnumerator;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfPageTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfTrailerTreeNode;
import com.itextpdf.rups.view.models.JTableAutoModel;
import com.itextpdf.rups.view.models.JTableAutoModelInterface;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

/**
 * A JTable listing all the pages in a PDF file: the object number of each
 * page dictionary and the page numbers (with label information if present).
 */
public class PagesTable extends JTable implements JTableAutoModelInterface, Observer {

    private final ObjectLoader loader;
    /**
     * A list with page nodes.
     */
    private ArrayList<PdfPageTreeNode> list = new ArrayList<>();

    /**
     * Nodes in the FormTree correspond with nodes in the main PdfTree.
     */
    protected PdfReaderController controller;
    protected PageSelectionListener listener;

    /**
     * Constructs a PagesTable.
     *
     * @param controller the pdf reader controller
     * @param listener   the page navigation listener
     * @param loader
     */
    public PagesTable(PdfReaderController controller, PageSelectionListener listener, ObjectLoader loader) {
        this.controller = controller;
        this.listener = listener;
        this.loader = loader;
        setModel(new JTableAutoModel(this));
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object obj) {
        if (observable instanceof PdfReaderController && obj instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) obj;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                    list = new ArrayList<>();
                    break;
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    ObjectLoader loader = (ObjectLoader) event.getContent();
                    String[] pageLabels = loader.getFile().getPdfDocument().getPageLabels();
                    int i = 0;
                    TreeNodeFactory factory = loader.getNodes();
                    PdfTrailerTreeNode trailer = controller.getPdfTree().getRoot();
                    PdfObjectTreeNode catalog = factory.getChildNode(trailer, PdfName.Root);
                    final Enumeration<PdfPageTreeNode> p =
                            new PageEnumerator((PdfDictionary) catalog.getPdfObject(), factory);
                    PdfPageTreeNode child;
                    final StringBuilder stringBuilder = new StringBuilder();
                    while (p.hasMoreElements()) {
                        child = p.nextElement();
                        stringBuilder.setLength(0);
                        i++;
                        stringBuilder.append(String.format(Language.PAGE_NUMBER.getString(), i));
                        if (pageLabels != null) {
                            stringBuilder.append(" ( ");
                            stringBuilder.append(pageLabels[i - 1]);
                            stringBuilder.append(" )");
                        }
                        child.setUserObject(stringBuilder.toString());
                        list.add(child);
                    }
                    break;
            }
            setModel(new JTableAutoModel(this));
            repaint();
        }
    }

    /**
     * @see javax.swing.JTable#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /**
     * @see javax.swing.JTable#getRowCount()
     */
    @Override
    public int getRowCount() {
        return list.size();
    }

    /**
     * @see javax.swing.JTable#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (getRowCount() == 0) return null;
        switch (columnIndex) {
            case 0:
                return String.format(Language.PAGES_TABLE_OBJECT.getString(), list.get(rowIndex).getNumber());
            case 1:
                return list.get(rowIndex);
            default:
                return null;
        }
    }

    /**
     * @see javax.swing.JTable#getColumnName(int)
     */
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Language.OBJECT.getString();
            case 1:
                return Language.PAGE.getString();
            default:
                return null;
        }
    }

    /**
     * @see javax.swing.JTable#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (evt != null) {
            super.valueChanged(evt);
        }
        if (controller == null) {
            return;
        }
        if (getRowCount() > 0) {
            int selectedRow = getSelectedRow();
            if (selectedRow >= 0) {
                controller.selectNode(list.get(selectedRow));
                if (listener != null) {
                    listener.gotoPage(getSelectedRow() + 1);
                }
            }
        }
    }
}