package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.rups.view.itext.PdfTree;
import com.itextpdf.rups.view.itext.SyntaxHighlightedStreamPane;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.kernel.pdf.PdfObject;

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
        final JFrame frame = new JFrame("Rups Object Inspection Window");

        // defines the size and location
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screen.getWidth() * .70), (int)(screen.getHeight() * .70));
        frame.setLocation((int)(screen.getWidth() * .05), (int)(screen.getHeight() * .05));
        frame.setResizable(true);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final PdfObject pdfObject = ( (PdfObjectTreeNode) ( (PdfTree) invoker ).getSelectionPath().getLastPathComponent() ).getPdfObject();
        final SyntaxHighlightedStreamPane syntaxHighlightedStreamPane = new SyntaxHighlightedStreamPane();

        frame.add(syntaxHighlightedStreamPane);
        syntaxHighlightedStreamPane.render(pdfObject);

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
        frame.getRootPane().getActionMap().put("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        if ( e.getSource() instanceof Component ) {
            frame.setLocationRelativeTo((Component) e.getSource());
        }
    }
}