package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.rups.view.itext.SyntaxHighlightedStreamPane;

import java.awt.event.ActionEvent;

public class SaveToPdfStreamJTextPaneAction extends AbstractRupsAction {

    /**
     * Serial version uid.
     */
    private static final long serialVersionUID = -5984892284970574660L;

    public SaveToPdfStreamJTextPaneAction(String name, SyntaxHighlightedStreamPane invoker) {
        super(name, invoker);
    }

    public void actionPerformed(ActionEvent event) {
        SyntaxHighlightedStreamPane pane = (SyntaxHighlightedStreamPane) invoker;
        pane.saveToTarget();
    }
}