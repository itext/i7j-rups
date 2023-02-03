package com.itextpdf.rups.model;

public final class FilePathPreProcessor {

    public static String process(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("file://")) {
            path = path.substring(7);
        }
        if (path.contains("file:")) {
            path = path.substring(path.indexOf("file:") + 5);
        }
        // its possible spaces are encoded as %20
        if (path.contains("%20")) {
            path = path.replace("%20", " ");
        }
        return path;
    }


}


