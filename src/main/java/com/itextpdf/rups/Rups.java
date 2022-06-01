/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
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
package com.itextpdf.rups;

import com.itextpdf.kernel.actions.data.ITextCoreProductData;
import com.itextpdf.rups.controller.IRupsController;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.RupsDropTarget;
import com.itextpdf.rups.view.RupsMenuBar;
import com.itextpdf.rups.view.RupsTabbedPane;
import com.itextpdf.rups.view.icons.FrameIconUtil;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Rups {

    /**
     * Initializes the main components of the Rups application.
     *
     * @param f a file that should be opened on launch
     */
    public static void startNewApplication(final File f) {
        SwingUtilities.invokeLater(() -> {
            setLookandFeel();
            final IRupsController rupsController = initApplication(new JFrame());
            if (f != null) {
                loadDocumentFromFile(rupsController, f);
            }
        });
    }

    static void loadDocumentFromFile(IRupsController rupsController, File f) {
        SwingUtilities.invokeLater(() -> rupsController.openNewFile(f));
    }

    static void setLookandFeel() {
        try {
            UIManager.setLookAndFeel(RupsConfiguration.INSTANCE.getLookAndFeel());
        } catch (
                ClassNotFoundException | InstantiationException |
                        IllegalAccessException | UnsupportedLookAndFeelException e) {
            LoggerHelper.error(Language.ERROR_LOOK_AND_FEEL.getString(), e, Rups.class);
        }
    }

    static IRupsController initApplication(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screen.getWidth() * .90), (int) (screen.getHeight() * .90));
        frame.setLocation((int) (screen.getWidth() * .05), (int) (screen.getHeight() * .05));
        frame.setResizable(true);

        // title bar
        frame.setTitle(
                String.format(Language.TITLE.getString(), ITextCoreProductData.getInstance().getVersion()));
        frame.setIconImages(FrameIconUtil.loadFrameIcons());
        frame.setDefaultCloseOperation(RupsConfiguration.INSTANCE.getCloseOperation());

        final RupsTabbedPane rupsTabbedPane = new RupsTabbedPane();
        final RupsController rupsController = new RupsController(screen, rupsTabbedPane);
        final RupsMenuBar rupsMenuBar = new RupsMenuBar(rupsController);
        rupsController.addObserver(rupsMenuBar);

        frame.setDropTarget(new RupsDropTarget(rupsController));
        frame.setJMenuBar(rupsMenuBar);

        frame.getContentPane().add(rupsController.getMasterComponent(), BorderLayout.CENTER);
        frame.setVisible(true);

        return rupsController;
    }
}
