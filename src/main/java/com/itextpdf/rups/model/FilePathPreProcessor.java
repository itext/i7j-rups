package com.itextpdf.rups.model;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Pre-processes file path to make it suitable for further processing.
 */
public final class FilePathPreProcessor {

    private FilePathPreProcessor() {
        // Empty constructor
    }

    public static String process(String path) {
        if (path == null) {
            return null;
        }
        String processedPath = path;
        final String fileProtocol = "file://";
        if (processedPath.startsWith(fileProtocol)) {
            processedPath = processedPath.substring(fileProtocol.length());
        }
        final String file = "file:";
        if (processedPath.contains(file)) {
            processedPath = processedPath.substring(processedPath.indexOf(file) + file.length());
        }
        // Check if the path is encoded
        if (isUriEncoded(processedPath)) {
            processedPath = URLDecoder.decode(processedPath, StandardCharsets.UTF_8);
        }
        return processedPath;
    }

    private static boolean isUriEncoded(String path) {
        return !path.equals(URLDecoder.decode(path, StandardCharsets.UTF_8));
    }
}


