package com.itextpdf.rups.view;

import com.itextpdf.rups.controller.IRupsController;
import com.itextpdf.rups.io.filters.PdfFilter;
import com.itextpdf.rups.model.LoggerHelper;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Drag and drop implementation for RUPS.
 */
public class RupsDropTarget extends DropTarget {
    /**
     * The controller instance that controls the application.
     */
    private final IRupsController rupsController;

    /**
     * Creates a RupsDropTarget
     *
     * @param rupsController the controller instance that controls the application
     */
    public RupsDropTarget(IRupsController rupsController) {
        this.rupsController = rupsController;
    }

    @Override
    public final synchronized void drop(DropTargetDropEvent dtde) {
        if (dtde != null) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

            final List<File> files = extractFilesFromTransferable(dtde.getTransferable());

            for (final File file : files) {
                if (PdfFilter.INSTANCE.accept(file)) {
                    this.rupsController.openNewFile(file);
                }
            }

            dtde.dropComplete(true);
        }
    }

    /**
     * The Transferable contains the dropped file(s). Get them out!
     *
     * @param transferable The dragged object(s)
     *
     * @return a list of Files, empty on errors or if the dragged objects aren't supported
     */
    List<File> extractFilesFromTransferable(Transferable transferable) {
        List<File> files = new ArrayList<>();

        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                LoggerHelper.warn(String.format(Language.ERROR_DRAG_AND_DROP.getString(), e.getMessage()), getClass());
            }
        } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            // fix for Linux
            try {
                final String urls = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                files = new ArrayList<>();

                final StringTokenizer tokens = new StringTokenizer(urls);
                while (tokens.hasMoreTokens()) {
                    final String urlString = tokens.nextToken();
                    final URL url = new URL(urlString);
                    files.add(new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name())));
                }
            } catch (UnsupportedFlavorException | IOException e) {
                LoggerHelper.warn(String.format(Language.ERROR_DRAG_AND_DROP.getString(), e.getMessage()), getClass());
            }
        } else {
            // empty body
        }

        return files;
    }
}
