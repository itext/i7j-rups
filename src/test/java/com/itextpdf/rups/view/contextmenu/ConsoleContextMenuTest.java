package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

@Category(UnitTest.class)
public class ConsoleContextMenuTest {

    @Test
    public void jMenuLengthTest() {
        JPopupMenu popupMenu = ConsoleContextMenu.getPopupMenu(null);

        MenuElement[] subElements = popupMenu.getSubElements();
        Assert.assertEquals(2, subElements.length);
    }

    @Test
    public void jMenuSubItemTypeTest() {
        JPopupMenu popupMenu = ConsoleContextMenu.getPopupMenu(null);

        MenuElement[] subElements = popupMenu.getSubElements();

        for (MenuElement menuElement : subElements) {
            Assert.assertTrue(menuElement instanceof JMenuItem);
        }
    }

    @Test
    public void assignedActionsTest() {
        JPopupMenu popupMenu = ConsoleContextMenu.getPopupMenu(null);

        MenuElement[] subElements = popupMenu.getSubElements();

        for (MenuElement menuElement : subElements) {
            Action action = ((JMenuItem) menuElement).getAction();

            Assert.assertTrue(action instanceof CopyToClipboardAction || action instanceof ClearConsoleAction );
        }
    }

}

