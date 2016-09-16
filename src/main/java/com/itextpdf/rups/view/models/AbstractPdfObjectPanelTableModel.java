package com.itextpdf.rups.view.models;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractPdfObjectPanelTableModel extends AbstractTableModel {

    public abstract void removeRow(int rowIndex);

    public abstract void validateTempRow();

    public abstract int getButtonColumn();
}
