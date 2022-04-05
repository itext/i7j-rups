package com.itextpdf.rups.view;

import com.itextpdf.test.annotations.type.UnitTest;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.JTabbedPane;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class RupsTabbedPaneTest {

    private static final String EXPECTED_TAB_TITLE = "hello_world.pdf";
    private static final String EXPECTED_TAB_TITLE_2 = "hello_world_copy.pdf";
    private static final String INPUT_1 = "src/test/resources/com/itextpdf/rups/controller/hello_world.pdf";
    private static final String INPUT_2 = "src/test/resources/com/itextpdf/rups/controller/hello_world_copy.pdf";

    private RupsTabbedPane tabbedPane;
    private Dimension dimension;

    @Before
    public void before() {
        this.tabbedPane = new RupsTabbedPane();
        this.dimension = Toolkit.getDefaultToolkit().getScreenSize();
    }

    @Test
    public void initializationTest() {
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assert.assertEquals(1, jTabbedPane.getTabCount());
        Assert.assertEquals(Language.DEFAULT_TAB_TITLE.getString(), jTabbedPane.getTitleAt(0));
    }

    @Test
    public void openNullFileTest() {
        this.tabbedPane.openNewFile(null, this.dimension, false);
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assert.assertEquals(1, jTabbedPane.getTabCount());
        Assert.assertEquals(Language.DEFAULT_TAB_TITLE.getString(), jTabbedPane.getTitleAt(0));
    }

    @Test
    public void openNewFileTest() {
        File file = new File(INPUT_1);
        this.tabbedPane.openNewFile(file, this.dimension, false);
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assert.assertEquals(1, jTabbedPane.getTabCount());
        Assert.assertEquals(EXPECTED_TAB_TITLE, jTabbedPane.getTitleAt(0));
    }

    @Test
    public void openTwoFiles() {
        File file = new File(INPUT_1);
        this.tabbedPane.openNewFile(file, this.dimension, false);
        File file2 = new File(INPUT_2);
        this.tabbedPane.openNewFile(file2, this.dimension, false);
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assert.assertEquals(2, jTabbedPane.getTabCount());
        Assert.assertEquals(EXPECTED_TAB_TITLE, jTabbedPane.getTitleAt(0));
        Assert.assertEquals(EXPECTED_TAB_TITLE_2, jTabbedPane.getTitleAt(1));
    }

    @Test
    public void openTwoFilesAndCloseAllTest() {
        File file = new File(INPUT_1);
        this.tabbedPane.openNewFile(file, this.dimension, false);
        File file2 = new File(INPUT_2);
        this.tabbedPane.openNewFile(file2, this.dimension, false);
        this.tabbedPane.closeCurrentFile();
        this.tabbedPane.closeCurrentFile();
        JTabbedPane jTabbedPane = (JTabbedPane) this.tabbedPane.getJTabbedPane();
        Assert.assertEquals(1, jTabbedPane.getTabCount());
        Assert.assertEquals(Language.DEFAULT_TAB_TITLE.getString(), jTabbedPane.getTitleAt(0));
    }

}
