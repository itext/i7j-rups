package com.itextpdf.rups.view.diff;

import java.io.File;

public class ViewerOptions {
    public boolean decompress;
    public boolean ignoreXrefTable;
    public boolean ignoreStreams;
    public File fileA;
    public File fileB;

    @Override
    public String toString() {
        return "ViewerOptions{" +
                "decompress=" + decompress +
                ", ignoreXrefTable=" + ignoreXrefTable +
                ", ignoreStreams=" + ignoreStreams +
                ", fileA=" + fileA +
                ", fileB=" + fileB +
                '}';
    }
}
