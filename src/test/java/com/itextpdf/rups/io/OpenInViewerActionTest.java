package com.itextpdf.rups.io;

import com.itextpdf.rups.mock.MockedPdfFile;
import com.itextpdf.rups.mock.MockedRupsController;
import com.itextpdf.rups.mock.MockedSystemViewer;
import com.itextpdf.rups.model.PdfFile;
import com.itextpdf.test.annotations.type.UnitTest;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class OpenInViewerActionTest {

    @Test
    public void normalTest() throws IOException {
        PdfFile pdfFile = new MockedPdfFile(new byte[] {}, false);

        MockedRupsController mockedRupsController = new MockedRupsController(pdfFile);
        MockedSystemViewer systemViewerAction = new MockedSystemViewer(true);
        OpenInViewerAction openInViewerAction = new OpenInViewerAction(mockedRupsController, systemViewerAction);
        openInViewerAction.actionPerformed(null);
        Assert.assertTrue(systemViewerAction.isFileOpened());
    }

    @Test
    public void viewingNotSupportedTest() throws IOException {
        PdfFile pdfFile = new MockedPdfFile(new byte[] {}, false);

        MockedRupsController mockedRupsController = new MockedRupsController(pdfFile);
        MockedSystemViewer systemViewerAction = new MockedSystemViewer(false);
        OpenInViewerAction openInViewerAction = new OpenInViewerAction(mockedRupsController, systemViewerAction);
        openInViewerAction.actionPerformed(null);
        Assert.assertFalse(systemViewerAction.isFileOpened());
    }

    @Test
    public void viewingNullFileTest() throws IOException {
        MockedRupsController mockedRupsController = new MockedRupsController(null);
        MockedSystemViewer systemViewerAction = new MockedSystemViewer(true);
        OpenInViewerAction openInViewerAction = new OpenInViewerAction(mockedRupsController, systemViewerAction);
        openInViewerAction.actionPerformed(null);
        Assert.assertFalse(systemViewerAction.isFileOpened());
    }
}
