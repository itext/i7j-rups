package com.itextpdf.rups.controller;

import com.itextpdf.rups.event.CloseDocumentEvent;
import com.itextpdf.rups.event.OpenFileEvent;
import com.itextpdf.rups.event.SaveToFileEvent;
import com.itextpdf.rups.view.RupsTabbedPane;

import java.awt.Dimension;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class RupsControllerTest {

    @Test
    public void closeTest() {
        MockRupsTabbedPane rupsTabbedPane = new MockRupsTabbedPane();
        RupsController rupsController = new RupsController(null, rupsTabbedPane);

        rupsController.update(null, new CloseDocumentEvent());
        Assert.assertTrue(rupsTabbedPane.closed);
        Assert.assertFalse(rupsTabbedPane.opened);
        Assert.assertFalse(rupsTabbedPane.saved);
    }

    @Test
    public void saveTest() {
        MockRupsTabbedPane rupsTabbedPane = new MockRupsTabbedPane();
        RupsController rupsController = new RupsController(null, rupsTabbedPane);

        rupsController.update(null, new SaveToFileEvent(new File("")));
        Assert.assertTrue(rupsTabbedPane.saved);
        Assert.assertFalse(rupsTabbedPane.opened);
        Assert.assertFalse(rupsTabbedPane.closed);
    }

    @Test
    public void openTest() {
        MockRupsTabbedPane rupsTabbedPane = new MockRupsTabbedPane();
        RupsController rupsController = new RupsController(null, rupsTabbedPane);

        rupsController.update(null, new OpenFileEvent(new File("test.pdf")));
        Assert.assertTrue(rupsTabbedPane.opened);
        Assert.assertFalse(rupsTabbedPane.saved);
        Assert.assertFalse(rupsTabbedPane.closed);
    }


    class MockRupsTabbedPane extends RupsTabbedPane {
        private boolean closed, saved, opened;
        public MockRupsTabbedPane() {
            super();
        }

        @Override
        public boolean closeCurrentFile() {
            closed = true;
            return closed;
        }

        @Override
        public void saveCurrentFile(File file) {
            saved = true;
        }

        @Override
        public void openNewFile(File file, Dimension dimension, boolean readonly) {
            opened = true;
        }
    }
}
