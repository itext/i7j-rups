package com.itextpdf.rups.mock;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class MockedTransferable implements Transferable {

    private final DataFlavor supportedDataFlavor;
    private final Object transferData;

    public MockedTransferable(DataFlavor supportedDataflavor) {
        this.supportedDataFlavor = supportedDataflavor;
        this.transferData = null;
    }

    public MockedTransferable(DataFlavor supportedDataFlavor, Object transferData) {
        this.supportedDataFlavor = supportedDataFlavor;
        this.transferData = transferData;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { this.supportedDataFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(this.supportedDataFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return this.transferData;
    }
}
