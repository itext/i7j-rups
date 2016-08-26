package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.rups.model.LoggerMessages;
import com.itextpdf.rups.view.itext.SyntaxHighlightedStreamPane;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveToPdfStreamJTextPaneAction extends AbstractRupsAction {

    /** Serial version uid. */
	private static final long serialVersionUID = -5984892284970574660L;

    public SaveToPdfStreamJTextPaneAction(String name, SyntaxHighlightedStreamPane invoker) {
        super(name, invoker);
    }

    public void actionPerformed(ActionEvent event) {
        SyntaxHighlightedStreamPane pane = (SyntaxHighlightedStreamPane)invoker;
        pane.saveToTarget();
    }
}