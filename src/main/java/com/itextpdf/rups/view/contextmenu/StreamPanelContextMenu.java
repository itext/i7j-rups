/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.rups.view.itext.SyntaxHighlightedStreamPane;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

/**
 * Convenience class/factory to get a context menu for a text pane. This context menu contains two actions as of yet:
 * - copy
 * - select all
 *
 * @author Michael Demey
 */
public class StreamPanelContextMenu extends JPopupMenu {

    private static final String COPY = "Copy";
    private static final String SELECT_ALL = "Select All";
    private static final String SAVE_TO_FILE = "Save to File";
    private static final String SAVE_TO_STREAM = "Save to Stream";

    private JMenuItem saveToStream;

    private boolean pluginMode;

    /**
     * Creates a context menu (right click menu) with two actions:
     * - copy
     * - select all
     * <p>
     * Copy copies the selected text or when no text is selected, it copies the entire text.
     *
     * @param textPane   the text pane
     * @param controller the controller
     * @param pluginMode the plugin mode
     */
    public StreamPanelContextMenu(final JTextPane textPane, final SyntaxHighlightedStreamPane controller, boolean pluginMode) {
        super();

        this.pluginMode = pluginMode;

        final JMenuItem copyItem = new JMenuItem();
        copyItem.setAction(new CopyToClipboardAction(COPY, textPane));
        copyItem.setText(COPY);

        final JMenuItem selectAllItem = new JMenuItem(SELECT_ALL);
        selectAllItem.setAction(textPane.getActionMap().get(DefaultEditorKit.selectAllAction));
        selectAllItem.setText(SELECT_ALL);

        JMenuItem saveToFile = new JMenuItem();
        saveToFile.setText(SAVE_TO_FILE);
        saveToFile.setAction(new SaveToFileJTextPaneAction(SAVE_TO_FILE, textPane));

        saveToStream = new JMenuItem();
        saveToStream.setText(SAVE_TO_STREAM);
        saveToStream.setAction(new SaveToPdfStreamJTextPaneAction(SAVE_TO_STREAM, controller));

        add(saveToStream);
        add(new JSeparator());
        add(copyItem);
        add(saveToFile);
        add(new JSeparator());
        add(selectAllItem);
    }

    public void setSaveToStreamEnabled(boolean enabled) {
        saveToStream.setEnabled(enabled && !pluginMode);
    }
}
