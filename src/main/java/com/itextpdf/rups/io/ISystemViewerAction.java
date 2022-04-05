package com.itextpdf.rups.io;

import java.io.File;

public interface ISystemViewerAction {
    boolean isViewingSupported();

    void openFile(File file);
}
