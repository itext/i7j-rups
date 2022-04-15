package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.itext.SyntaxHighlightedStreamPane;
import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.MenuElement;
import javax.swing.text.TextAction;

@Category(UnitTest.class)
public class StreamPanelContextMenuTest {
    @Test
    public void jMenuLengthTest() {
        JPopupMenu popupMenu =
                new StreamPanelContextMenu(new JTextPane(), new SyntaxHighlightedStreamPane(null, false), false);

        MenuElement[] subElements = popupMenu.getSubElements();
        Assert.assertEquals(4, subElements.length);
    }

    @Test
    public void jMenuSubItemTypeTest() {
        JPopupMenu popupMenu =
                new StreamPanelContextMenu(new JTextPane(), new SyntaxHighlightedStreamPane(null, false), false);

        MenuElement[] subElements = popupMenu.getSubElements();

        for (MenuElement menuElement : subElements) {
            Assert.assertTrue(menuElement instanceof JMenuItem);
        }
    }

    @Test
    public void assignedActionsTest() {
        JPopupMenu popupMenu =
                new StreamPanelContextMenu(new JTextPane(), new SyntaxHighlightedStreamPane(null, false), false);

        MenuElement[] subElements = popupMenu.getSubElements();

        for (MenuElement menuElement : subElements) {
            Action action = ((JMenuItem) menuElement).getAction();

            Assert.assertTrue(
                    action instanceof SaveToPdfStreamJTextPaneAction || action instanceof SaveToFileJTextPaneAction
                            || action instanceof CopyToClipboardAction || action instanceof TextAction );
        }
    }

    @Test
    public void saveToStreamDisabledTest() {
        StreamPanelContextMenu popupMenu =
                new StreamPanelContextMenu(new JTextPane(), new SyntaxHighlightedStreamPane(null, false), false);
        popupMenu.setSaveToStreamEnabled(false);

        MenuElement[] subElements = popupMenu.getSubElements();

        for (MenuElement menuElement : subElements) {
            JMenuItem menuItem = ((JMenuItem) menuElement);

            if (Language.SAVE_TO_STREAM.getString().equals(menuItem.getText())) {
                Assert.assertFalse(menuItem.isEnabled());
            }
        }
    }

    @Test
    public void saveToStreamReEnabledTest() {
        StreamPanelContextMenu popupMenu =
                new StreamPanelContextMenu(new JTextPane(), new SyntaxHighlightedStreamPane(null, false), false);
        popupMenu.setSaveToStreamEnabled(false);
        popupMenu.setSaveToStreamEnabled(true);

        MenuElement[] subElements = popupMenu.getSubElements();

        for (MenuElement menuElement : subElements) {
            JMenuItem menuItem = ((JMenuItem) menuElement);

            if (Language.SAVE_TO_STREAM.getString().equals(menuItem.getText())) {
                Assert.assertTrue(menuItem.isEnabled());
            }
        }
    }

}
