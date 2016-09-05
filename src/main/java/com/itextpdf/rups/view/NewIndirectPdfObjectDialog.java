package com.itextpdf.rups.view;

import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.event.NewIndirectObjectEvent;
import com.itextpdf.rups.model.PdfSyntaxParser;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class NewIndirectPdfObjectDialog extends JDialog implements PropertyChangeListener {

    private PdfObject result;
    private JComboBox type;
    private JTextArea textArea;
    private JOptionPane optionPane;

    private PdfSyntaxParser parser;

    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";

    /** Creates the reusable dialog. */
    public NewIndirectPdfObjectDialog(Frame parent, String title, PdfSyntaxParser parser) {
        super(parent, true);

        setTitle(title);

        this.parser = parser;
        textArea = new JTextArea();
        textArea.setMinimumSize(new Dimension(100, 200));
        type = new JComboBox<String>(new String [] {"Stream", "Other"});

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setMinimumSize(new Dimension(100, 200));
        //Create an array of the text and components to be displayed.
        Object[] array = {/*"Type", type,*/ "Value", scrollPane};

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
                optionPane.setValue(new Integer(
                        JOptionPane.CLOSED_OPTION));
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

    /** This method reacts to state changes in the option pane. */
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

    /** This method clears the dialog and hides it. */
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
