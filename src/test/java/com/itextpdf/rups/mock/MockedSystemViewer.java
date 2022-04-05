package com.itextpdf.rups.mock;

import com.itextpdf.rups.io.ISystemViewerAction;

import java.io.File;

public class MockedSystemViewer implements ISystemViewerAction {

    private final boolean supported;
    private boolean fileOpened;

    public MockedSystemViewer(boolean supported) {
        this.supported = supported;
        this.fileOpened = false;
    }

    @Override
    public boolean isViewingSupported() {
        return this.supported;
    }

    @Override
    public void openFile(File file) {
        this.fileOpened = true;
    }

    public boolean isFileOpened() {
        return fileOpened;
    }
}
