/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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
package com.itextpdf.rups.view;

import com.itextpdf.kernel.actions.data.ITextCoreProductData;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.io.FileCloseAction;
import com.itextpdf.rups.io.FileCompareAction;
import com.itextpdf.rups.io.FileOpenAction;
import com.itextpdf.rups.io.FileSaveAction;
import com.itextpdf.rups.io.OpenInViewerAction;
import com.itextpdf.rups.io.filters.PdfFilter;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class RupsMenuBar extends JMenuBar {
    /**
     * The action needed to open a file.
     */
    protected FileOpenAction fileOpenAction;
    /**
     * The action needed to close a file/tab.
     */
    protected FileCloseAction fileCloseAction;
    /**
     * The action needed to open a file in the system viewer.
     */
    protected OpenInViewerAction openInViewerAction;
    /**
     * The action needed to save a file.
     */
    protected FileSaveAction fileSaverAction;
    /**
     * The action needed to compare files.
     */
    protected FileCompareAction fileCompareAction;
    /**
     * The HashMap with all the actions.
     */
    protected HashMap<String, JMenuItem> items;
    /**
     * The Preferences Window
     */
    private final PreferencesWindow preferencesWindow;

    /**
     * Creates a JMenuBar.
     */
    public RupsMenuBar(RupsController controller) {
        items = new HashMap<>();

        preferencesWindow = new PreferencesWindow();

        fileOpenAction = new FileOpenAction(PdfFilter.INSTANCE, controller.getMasterComponent());
        fileOpenAction.addPropertyChangeListener(new FileOpenChangeListener(controller));

        fileCloseAction = new FileCloseAction();
        fileCloseAction.addPropertyChangeListener(new FileCloseChangeListener(controller));

        fileSaverAction = new FileSaveAction(PdfFilter.INSTANCE, controller.getMasterComponent());
        fileSaverAction.addPropertyChangeListener(new FileSaveChangeListener(controller));

        openInViewerAction = new OpenInViewerAction(controller);

        fileCompareAction =
                new FileCompareAction(PdfFilter.INSTANCE, controller.getMasterComponent());
        fileCompareAction.addPropertyChangeListener(new FileCompareChangeListener());

        final JMenu file = new JMenu(Language.MENU_BAR_FILE.getString());
        addItem(file, Language.MENU_BAR_OPEN.getString(), fileOpenAction,
                KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
        FileCloseAction action = new FileCloseAction();
        action.addPropertyChangeListener(new FileCloseChangeListener(controller));
        addItem(file, Language.MENU_BAR_CLOSE.getString(), action,
                KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK));
        addItem(file, Language.MENU_BAR_SAVE_AS.getString(), fileSaverAction,
                KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
        addItem(file, Language.MENU_BAR_COMPARE_WITH.getString(), fileCompareAction,
                KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK));
        file.addSeparator();
        addItem(file, Language.MENU_BAR_OPEN_IN_PDF_VIEWER.getString(), openInViewerAction, KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK));
        addItem(file, Language.MENU_BAR_NEW_INDIRECT.getString(),
                new NewIndirectPdfObjectDialog.AddNewIndirectAction(controller),
                KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK));
        add(file);

        final JMenu edit = new JMenu(Language.MENU_BAR_EDIT.getString());
        addItem(edit, Language.PREFERENCES.getString(), e -> {
                    preferencesWindow.show(controller.getMasterComponent());
                }
        );
        add(edit);

        add(Box.createGlue());

        final JMenu help = new JMenu(Language.MENU_BAR_HELP.getString());
        addItem(help, Language.MENU_BAR_ABOUT.getString(), new MessageAction(Language.MESSAGE_ABOUT.getString()));
        addItem(help, Language.MENU_BAR_VERSION.getString(),
                new MessageAction(ITextCoreProductData.getInstance().getVersion()));
        add(help);
        enableItems(false);
    }

    /**
     * Create an item with a certain caption and a certain action,
     * then add the item to a menu.
     *
     * @param menu    the menu to which the item has to be added
     * @param caption the caption of the item
     * @param action  the action corresponding with the caption
     */
    protected final void addItem(JMenu menu, String caption, ActionListener action) {
        addItem(menu, caption, action, null);
    }

    protected final void addItem(JMenu menu, String caption, ActionListener action, KeyStroke keyStroke) {
        JMenuItem item = new JMenuItem(caption);
        item.addActionListener(action);
        if (keyStroke != null) {
            item.setAccelerator(keyStroke);
        }
        menu.add(item);
        items.put(caption, item);
    }

    /**
     * Enables/Disables a series of menu items.
     *
     * @param enabled true for enabling; false for disabling
     */
    public void enableItems(boolean enabled) {
        enableItem(Language.MENU_BAR_CLOSE.getString(), enabled);
        enableItem(Language.MENU_BAR_SAVE_AS.getString(), enabled);
        enableItem(Language.MENU_BAR_OPEN_IN_PDF_VIEWER.getString(), enabled);
        enableItem(Language.MENU_BAR_COMPARE_WITH.getString(), enabled);
        enableItem(Language.MENU_BAR_NEW_INDIRECT.getString(), enabled);
    }

    /**
     * Enables/disables a specific menu item
     *
     * @param caption the caption of the item that needs to be enabled/disabled
     * @param enabled true for enabling; false for disabling
     */
    protected void enableItem(String caption, boolean enabled) {
        items.get(caption).setEnabled(enabled);
    }
}
