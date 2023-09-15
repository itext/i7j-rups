package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import java.util.Arrays;

@Category(UnitTest.class)
public class PdfTreeContextMenuTest {

    @Test
    public void jMenuLengthTest() {
        JPopupMenu popupMenu = PdfTreeContextMenu.getPopupMenu(null);

        MenuElement[] subElements = popupMenu.getSubElements();
        Assert.assertEquals(4, subElements.length);
    }

    @Test
    public void jMenuSubItemTypeTest() {
        JPopupMenu popupMenu = PdfTreeContextMenu.getPopupMenu(null);

        MenuElement[] subElements = popupMenu.getSubElements();

        Assert.assertTrue(Arrays.stream(subElements).allMatch(menuElement -> menuElement instanceof JMenuItem));
    }

    @Test
    public void assignedActionsTest() {
        JPopupMenu popupMenu = PdfTreeContextMenu.getPopupMenu(null);

        MenuElement[] subElements = popupMenu.getSubElements();

        Assert.assertTrue(Arrays.stream(subElements).anyMatch(element -> ((JMenuItem) element).getAction() instanceof InspectObjectAction));

        Assert.assertTrue(Arrays.stream(subElements).anyMatch(element -> ((JMenuItem) element).getAction() instanceof SaveToFilePdfTreeAction));

        Assert.assertTrue(Arrays.stream(subElements).anyMatch(element -> ((JMenuItem) element).getAction() instanceof CopyToClipboardAction));

        Assert.assertTrue(Arrays.stream(subElements).anyMatch(element -> ((JMenuItem) element).getAction() instanceof SaveToFilePdfTreeAction));
    }
}
