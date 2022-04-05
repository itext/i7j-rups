package com.itextpdf.rups.controller;

import com.itextpdf.rups.event.CloseDocumentEvent;
import com.itextpdf.rups.event.OpenFileEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.event.SaveToFileEvent;
import com.itextpdf.rups.view.RupsTabbedPane;
import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.Dim;

import java.awt.*;
import java.io.File;

public class RupsControllerTest {

    @Test
    public void closeTest() {
        MockRupsTabbedPane rupsTabbedPane = new MockRupsTabbedPane(new Dimension());
        RupsController rupsController = new RupsController(rupsTabbedPane);

        rupsController.update(null, new CloseDocumentEvent());
        Assert.assertTrue(rupsTabbedPane.closed);
        Assert.assertFalse(rupsTabbedPane.opened);
        Assert.assertFalse(rupsTabbedPane.saved);
    }

    @Test
    public void saveTest() {
        MockRupsTabbedPane rupsTabbedPane = new MockRupsTabbedPane(new Dimension());
        RupsController rupsController = new RupsController(rupsTabbedPane);

        rupsController.update(null, new SaveToFileEvent(new File("")));
        Assert.assertTrue(rupsTabbedPane.saved);
        Assert.assertFalse(rupsTabbedPane.opened);
        Assert.assertFalse(rupsTabbedPane.closed);
    }

    @Test
    public void openTest() {
        MockRupsTabbedPane rupsTabbedPane = new MockRupsTabbedPane(new Dimension());
        RupsController rupsController = new RupsController(rupsTabbedPane);

        rupsController.update(null, new OpenFileEvent(new File("")));
        Assert.assertTrue(rupsTabbedPane.opened);
        Assert.assertFalse(rupsTabbedPane.saved);
        Assert.assertFalse(rupsTabbedPane.closed);
    }


    class MockRupsTabbedPane extends RupsTabbedPane {
        private boolean closed, saved, opened;
        public MockRupsTabbedPane(Dimension dimension) {
            super(dimension);
        }

        @Override
        public void closeCurrentFile() {
            closed = true;
        }

        @Override
        public void saveCurrentFile(File file) {
            saved = true;
        }

        @Override
        public void openNewFile(File file, boolean readonly) {
            opened = true;
        }
    }
}
