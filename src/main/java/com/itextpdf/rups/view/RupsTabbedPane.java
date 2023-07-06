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

import com.itextpdf.rups.controller.RupsInstanceController;
import com.itextpdf.rups.model.IPdfFile;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * The class holding the JTabbedPane that holds the Rups tabs. This class is responsible for loading, closing, and
 * saving PDF files handled by the {@link com.itextpdf.rups.controller.RupsInstanceController rupsInstanceController}
 * object that is tied to a {@link com.itextpdf.rups.view.RupsPanel RupsPanel}, which is the main content pane for the
 * tabs in the JTabbedPane.
 */
public class RupsTabbedPane {

    private final JPanel defaultTab;
    private final JTabbedPane jTabbedPane;

    public RupsTabbedPane() {
        this.jTabbedPane = new JTabbedPane();
        this.defaultTab = new JPanel();
        this.defaultTab.add(new JLabel(Language.DEFAULT_TAB_TEXT.getString()));
        this.jTabbedPane.addTab(Language.DEFAULT_TAB_TITLE.getString(), defaultTab);
    }

    public void openNewFile(File file, Dimension dimension) {
        if (file != null) {
            if (this.defaultTab.equals(this.jTabbedPane.getSelectedComponent())) {
                this.jTabbedPane.removeTabAt(this.jTabbedPane.getSelectedIndex());
            }

            RupsPanel rupsPanel = new RupsPanel();
            RupsInstanceController rupsInstanceController = new RupsInstanceController(dimension, rupsPanel);
            rupsPanel.setRupsInstanceController(rupsInstanceController);
            rupsInstanceController.loadFile(file);
            this.jTabbedPane.addTab(file.getName(), null, rupsPanel);
            this.jTabbedPane.setSelectedComponent(rupsPanel);
        }
    }

    public boolean closeCurrentFile() {
        boolean isLastTab = this.jTabbedPane.getTabCount() == 1;

        this.jTabbedPane.removeTabAt(this.jTabbedPane.getSelectedIndex());

        if (this.jTabbedPane.getTabCount() == 0) {
            this.jTabbedPane.addTab(Language.DEFAULT_TAB_TITLE.getString(), this.defaultTab);
        }

        return isLastTab;
    }

    public IPdfFile getCurrentFile() {
        Component currentComponent = this.jTabbedPane.getSelectedComponent();
        RupsPanel currentRupsPanel = (RupsPanel) currentComponent;
        return currentRupsPanel.getPdfFile();
    }

    public void saveCurrentFile(File file) {
        RupsPanel currentRupsPanel = (RupsPanel) this.jTabbedPane.getSelectedComponent();
        currentRupsPanel.getRupsInstanceController().saveFile(file);
    }

    public Component getJTabbedPane() {
        return this.jTabbedPane;
    }

    /**
     * Checks to see whether the provided file has already been opened in RUPS.
     *
     * @param file potentially duplicate file
     *
     * @return true if already opened, false if not
     */
    public boolean isFileAlreadyOpen(File file) {
        for (int tabIndex = 0; tabIndex < this.jTabbedPane.getTabCount(); tabIndex++) {
            Component component = this.jTabbedPane.getComponentAt(tabIndex);

            if ( component instanceof RupsPanel ) {
                RupsPanel rupsPanel = (RupsPanel) component;
                IPdfFile pdfFile = rupsPanel.getPdfFile();
                if (pdfFile != null && pdfFile.getOriginalFile().equals(file)) {
                    return true;
                }
            }
        }

        return false;
    }
}
