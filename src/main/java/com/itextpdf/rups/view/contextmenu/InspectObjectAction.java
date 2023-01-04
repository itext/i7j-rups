/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
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

import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.icons.FrameIconUtil;
import com.itextpdf.rups.view.itext.PdfTree;
import com.itextpdf.rups.view.itext.SyntaxHighlightedStreamPane;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Michael Demey
 */
public class InspectObjectAction extends AbstractRupsAction {

    private Component invoker;

    public InspectObjectAction(String name, Component invoker) {
        super(name);
        this.invoker = invoker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final JFrame frame = new JFrame(Language.TITLE_OBJECT_INSPECTION.getString());

        // defines the size and location
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screen.getWidth() * .70), (int) (screen.getHeight() * .70));
        frame.setLocation((int) (screen.getWidth() * .05), (int) (screen.getHeight() * .05));
        frame.setIconImages(FrameIconUtil.loadFrameIcons());
        frame.setResizable(true);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final PdfObjectTreeNode node =
                (PdfObjectTreeNode) ((PdfTree) invoker).getSelectionPath().getLastPathComponent();
        final SyntaxHighlightedStreamPane syntaxHighlightedStreamPane = new SyntaxHighlightedStreamPane(null);

        frame.add(syntaxHighlightedStreamPane);
        syntaxHighlightedStreamPane.render(node);

        final Language dialogCancel = Language.DIALOG_CANCEL;
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), dialogCancel);
        frame.getRootPane().getActionMap().put(dialogCancel, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        if (e.getSource() instanceof Component) {
            frame.setLocationRelativeTo((Component) e.getSource());
        }
    }
}
