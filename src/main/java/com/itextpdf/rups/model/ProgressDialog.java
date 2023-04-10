/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

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
package com.itextpdf.rups.model;

import com.itextpdf.rups.view.Language;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowEvent;

/**
 * An informational dialog window showing the progress of a certain action.
 */
public final class ProgressDialog extends JDialog implements IProgressDialog {
    /**
     * label showing the message describing what's in progress.
     */
    private final JLabel message;

    /**
     * the progress bar
     */
    private final JProgressBar progress;

    /**
     * the icon used for this dialog box.
     */
    public static final JLabel INFO = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));

    /**
     * Creates a Progress frame displaying a certain message
     * and a progress bar in indeterminate mode.
     *
     * @param parent     the parent frame of this dialog (used to position the dialog)
     * @param msg        the message that will be displayed.
     * @param frame      the frame
     */
    public ProgressDialog(Component parent, String msg, Frame frame) {
        super(frame);
        this.setTitle(Language.DIALOG_PROGRESS.getString());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(300, 100);
        this.setLocationRelativeTo(parent);

        setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        getContentPane().add(INFO, constraints);
        constraints.gridheight = 1;
        constraints.gridx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        message = new JLabel(msg);
        getContentPane().add(message, constraints);
        constraints.gridy = 1;
        progress = new JProgressBar();
        progress.setIndeterminate(true);
        getContentPane().add(progress, constraints);
    }

    @Override
    public void dispose() {
        final Window frame = getOwner();
        super.dispose();
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Changes the message describing what's in progress
     *
     * @param msg the message describing what's in progress
     */
    @Override
    public void setMessage(String msg) {
        message.setText(msg);
    }

    /**
     * Changes the value of the progress bar.
     *
     * @param value the current value
     */
    @Override
    public void setValue(int value) {
        progress.setValue(value);
    }

    /**
     * Sets the maximum value for the progress bar.
     * If 0 or less, sets the progress bar to indeterminate mode.
     *
     * @param n the maximum value for the progress bar
     */
    @Override
    public void setTotal(int n) {
        if (n > 0) {
            progress.setMaximum(n);
            progress.setIndeterminate(false);
            progress.setStringPainted(true);
        } else {
            progress.setIndeterminate(true);
            progress.setStringPainted(false);
        }
    }

    /**
     * Displays an error dialog for the given exception.
     *
     * @param ex exception to display information about
     */
    @Override
    public void showErrorDialog(Exception ex) {
        ErrorDialogPane.showErrorDialog(this, ex);
    }
}
