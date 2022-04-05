package com.itextpdf.rups.io;

import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Language;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class DefaultSystemViewerAction implements ISystemViewerAction {
    @Override
    public final boolean isViewingSupported() {
        return Desktop.isDesktopSupported();
    }

    @Override
    public final void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            LoggerHelper.warn(Language.ERROR_FILE_COULD_NOT_BE_VIEWED.getString(), this.getClass());
        }
    }
}
