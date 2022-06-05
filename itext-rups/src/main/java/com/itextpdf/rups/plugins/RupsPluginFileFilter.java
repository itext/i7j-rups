package com.itextpdf.rups.plugins;

import java.io.File;
import java.io.FileFilter;

public class RupsPluginFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return pathname.getName().endsWith(".jar");
    }
}
