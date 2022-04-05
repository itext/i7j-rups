package com.itextpdf.rups.view;

import com.itextpdf.rups.io.filters.PdfFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Drag and drop implementation for RUPS.
 */
public class RupsDropTarget extends DropTarget {
    private RupsTabbedPane rupsTabbedPane;

    public RupsDropTarget(RupsTabbedPane rupsTabbedPane) {
        this.rupsTabbedPane = rupsTabbedPane;
    }

    public synchronized void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        Transferable t = dtde.getTransferable();
        List<File> files = null;

        try {
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
            }
            if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) { // fix for Linux
                String urls = (String) t.getTransferData(DataFlavor.stringFlavor);
                files = new LinkedList<>();
                StringTokenizer tokens = new StringTokenizer(urls);
                while (tokens.hasMoreTokens()) {
                    String urlString = tokens.nextToken();
                    URL url = new URL(urlString);
                    files.add(new File(URLDecoder.decode(url.getFile(), "UTF-8")));
                }
            }

            PdfFilter pdfFilter = PdfFilter.INSTANCE;

            for ( File file : files ) {
                if (pdfFilter.accept(file)) {
                    this.rupsTabbedPane.openNewFile(file, false);
                }
            }
        } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
            JOptionPane.showMessageDialog(rupsTabbedPane, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        dtde.dropComplete(true);
    }
}
