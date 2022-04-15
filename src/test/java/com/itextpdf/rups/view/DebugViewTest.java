package com.itextpdf.rups.view;

import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class DebugViewTest {

    @Test
    public void appendMessageTest() {
        String expected = "5";
        DebugView.getInstance().getTextArea().setText("");
        new DebugView.UpdateTextPaneTask(expected).run();
        String actual = DebugView.getInstance().getTextArea().getText();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void backupMessageTest() {
        String expected = "5\n";
        DebugView.getInstance().getTextArea().setText("");
        DebugView.UpdateTextPaneTask updateTextPaneTask = new DebugView.UpdateTextPaneTask(expected);

        for ( int i = 0; i <= 8192; i++) {
            updateTextPaneTask.run();
        }

        String actual = DebugView.getInstance().getTextArea().getText();

        Assert.assertTrue(actual.contains(Language.ERROR_TOO_MANY_OUTPUT.getString()));
    }
}
