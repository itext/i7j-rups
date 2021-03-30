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
package com.itextpdf.rups.view;

import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.event.NewIndirectObjectEvent;
import com.itextpdf.rups.model.PdfSyntaxParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NewIndirectPdfObjectDialog extends JDialog implements PropertyChangeListener {

    private PdfObject result;
    private JTextArea textArea;
    private JOptionPane optionPane;

    private PdfSyntaxParser parser;

    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";

    /**
     * Creates the reusable dialog.
     *
     * @param parent parent Frame
     * @param title  dialog title
     * @param parser PdfSyntaxParser
     */
    public NewIndirectPdfObjectDialog(Frame parent, String title, PdfSyntaxParser parser) {
        super(parent, true);

        setTitle(title);

        this.parser = parser;
        textArea = new JTextArea();
        textArea.setMinimumSize(new Dimension(100, 200));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setMinimumSize(new Dimension(100, 200));
        //Create an array of the text and components to be displayed.
        Object[] array = {"Value", scrollPane};

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {btnString1, btnString2};

        //Create the JOptionPane.
        optionPane = new JOptionPane(array,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]);

        //Make this dialog display it.
        setContentPane(optionPane);

        pack();
        setSize(300, 450);
        Point parentLocation = parent.getLocation();
        setLocation(parentLocation.x + 20, parentLocation.y + 40);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
            }
        });

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                textArea.requestFocusInWindow();
            }
        });

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
    }

    /**
     * This method reacts to state changes in the option pane.
     */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
                && (e.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
                JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(
                    JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
                result = parser.parseString(textArea.getText(), getContentPane());
                clearAndHide();
            } else { //user closed dialog or clicked cancel
                result = null;
                clearAndHide();
            }
        }
    }

    /**
     * This method clears the dialog and hides it.
     */
    public void clearAndHide() {
        textArea.setText(null);
        setVisible(false);
    }

    public PdfObject getResult() {
        return result;
    }

    public static class AddNewIndirectAction extends AbstractAction {

        private RupsController controller;

        public AddNewIndirectAction(RupsController controller) {
            super();
            this.controller = controller;
        }

        public AddNewIndirectAction(RupsController controller, String name) {
            super(name);
            this.controller = controller;
        }

        public AddNewIndirectAction(RupsController controller, String name, Icon icon) {
            super(name, icon);
            this.controller = controller;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.update(null, new NewIndirectObjectEvent());
        }
    }
}
