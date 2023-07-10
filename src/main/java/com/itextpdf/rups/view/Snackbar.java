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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Class for showing brief notifications in the app.
 * As the names suggests, this limited class is inspired by
 * <a href="https://m3.material.io/components/snackbar/overview">Snackbar</a>
 * from Android.
 */
public class Snackbar {
    private static final Color COLOR_INVERSE_ON_SURFACE = new Color(0xF4EFF4);
    private static final Color COLOR_INVERSE_SURFACE = new Color(0x313033);

    private static final int MARGIN = 16;
    private static final int X_PADDING = 16;
    private static final int PANEL_HEIGHT = 48;
    private static final int SHADOW_HEIGHT = 6;
    private static final int FONT_SIZE = 14;
    private static final int ARC_SIZE = 10;

    private final Window owner;
    private final String text;
    private final int durationInMs;

    private Snackbar(Window owner, String text, int durationInMs) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner should not be null");
        }

        this.owner = owner;
        this.text = text;
        this.durationInMs = durationInMs;
    }

    public static Snackbar make(Window owner, String text) {
        return new Snackbar(owner, text, 4000);
    }

    public static Snackbar make(Window owner, String text, int durationInMs) {
        return new Snackbar(owner, text, durationInMs);
    }

    public void show() {
        // Creating a new dialog to show
        JDialog dialog = createDialog(owner, text);
        // Make sure the dimensions are correct first
        onParentResized(dialog);
        onParentMoved(dialog);
        // Show
        dialog.setVisible(true);
        // Start timer for hiding the dialog and disposing
        Timer timer = new Timer(durationInMs, e -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private static JDialog createDialog(Window window, String text) {
        JDialog dialog = new JDialog(window, ModalityType.MODELESS);
        dialog.setUndecorated(true);
        /*
         * Dialog should be transparent. The content pane is responsible for
         * drawing the background.
         */
        dialog.setBackground(new Color(0x00000000, true));
        /*
         * At this moment this is a non-intractable dialog, so no reason for it
         * to be focusable.
         */
        dialog.setFocusableWindowState(false);
        /*
         * This handler will ensure, that our snackbar remains at the bottom of
         * the window at all times.
         */
        dialog.getParent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onParentResized(dialog);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                onParentMoved(dialog);
            }
        });

        SnackbarPanel panel = new SnackbarPanel(ARC_SIZE, ARC_SIZE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, X_PADDING, SHADOW_HEIGHT, X_PADDING));
        panel.setBackground(COLOR_INVERSE_SURFACE);

        JLabel label = new JLabel(text);
        label.setForeground(COLOR_INVERSE_ON_SURFACE);
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, FONT_SIZE));
        panel.add(label);

        dialog.setContentPane(panel);

        return dialog;
    }

    private static void onParentResized(JDialog dialog) {
        Container parent = dialog.getParent();
        dialog.setSize(
                parent.getWidth() - 2 * MARGIN,
                PANEL_HEIGHT + SHADOW_HEIGHT
        );
    }

    private static void onParentMoved(JDialog dialog) {
        Container parent = dialog.getParent();
        dialog.setLocation(
                parent.getX() + MARGIN,
                parent.getY() + parent.getHeight() - dialog.getHeight() - MARGIN
        );
    }

    /**
     * A small panel with rounded corners and a basic shadow.
     */
    private static class SnackbarPanel extends JPanel {
        private final int arcWidth;
        private final int arcHeight;

        public SnackbarPanel(int arcWidth, int arcHeight) {
            this.arcWidth = arcWidth;
            this.arcHeight = arcHeight;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D graphics = null;
            try {
                graphics = (Graphics2D) g.create();
                graphics.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );
                // Drawing the panel shadow
                // Which is pretty basic...
                GradientPaint paint = new GradientPaint(
                        0,
                        (getHeight() - SHADOW_HEIGHT),
                        new Color(0x33000000, true),
                        0,
                        getHeight(),
                        new Color(0x00000000, true)
                );
                graphics.setPaint(paint);
                graphics.fillRoundRect(
                        0,
                        SHADOW_HEIGHT,
                        getWidth() - 1,
                        getHeight() - SHADOW_HEIGHT - 1,
                        arcWidth,
                        arcHeight
                );
                // Drawing the panel itself
                graphics.setColor(getBackground());
                graphics.fillRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - SHADOW_HEIGHT - 1,
                        arcWidth,
                        arcHeight
                );
            } finally {
                if (graphics != null) {
                    graphics.dispose();
                }
            }
        }
    }
}
