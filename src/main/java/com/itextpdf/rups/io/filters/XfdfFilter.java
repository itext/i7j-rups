package com.itextpdf.rups.io.filters;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class XfdfFilter extends FileFilter {

    /**
     * A public instance of the PdfFilter.
     */
    public static final XfdfFilter INSTANCE = new XfdfFilter();

    /**
     * @param f File
     * @return boolean
     * @see FileFilter#accept(File)
     */
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".xfdf");
    }

    /**
     * @return String
     * @see FileFilter#getDescription()
     */
    public String getDescription() {
        return "*.xfdf XFDF files";
    }
}