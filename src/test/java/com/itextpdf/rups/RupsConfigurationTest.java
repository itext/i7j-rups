package com.itextpdf.rups;

import com.itextpdf.test.annotations.type.UnitTest;

import java.io.File;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import javax.swing.WindowConstants;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class RupsConfigurationTest {

    private static Properties copy;

    @BeforeClass
    public static void beforeClass() throws BackingStoreException {
        copy = RupsConfiguration.INSTANCE.getCurrentState();
        RupsConfiguration.INSTANCE.resetToDefaultProperties();
    }

    @Test
    public void setHomeFolderTest() {
        String path = "src/test";
        RupsConfiguration.INSTANCE.setHomeFolder(path);
        RupsConfiguration.INSTANCE.saveConfiguration();
        File homeFolder = RupsConfiguration.INSTANCE.getHomeFolder();
        File expected = new File(path);
        Assert.assertEquals(expected, homeFolder);
    }

    @Test
    public void setHomeFolderToNullTest() {
        RupsConfiguration.INSTANCE.setHomeFolder(null);
        RupsConfiguration.INSTANCE.saveConfiguration();
        File homeFolder = RupsConfiguration.INSTANCE.getHomeFolder();
        File expected = new File(System.getProperty("user.home"));
        Assert.assertEquals(expected, homeFolder);
    }

    @Test
    public void setHomeFolderToFileTest() {
        String path = "src/test/resources/com/itextpdf/rups/controller/hello_world.pdf";
        RupsConfiguration.INSTANCE.setHomeFolder(path);
        RupsConfiguration.INSTANCE.saveConfiguration();
        File homeFolder = RupsConfiguration.INSTANCE.getHomeFolder();
        File expected = new File(System.getProperty("user.home"));
        Assert.assertEquals(expected, homeFolder);
    }

    @Test
    public void setHomeFolderToNonExistentDirectoryTest() {
        String path = "does/not/exist/";
        RupsConfiguration.INSTANCE.setHomeFolder(path);
        RupsConfiguration.INSTANCE.saveConfiguration();
        File homeFolder = RupsConfiguration.INSTANCE.getHomeFolder();
        File expected = new File(System.getProperty("user.home"));
        Assert.assertEquals(expected, homeFolder);
    }

    @Test
    public void setDuplicateFilesTrueTest() {
        RupsConfiguration.INSTANCE.setOpenDuplicateFiles(true);
        RupsConfiguration.INSTANCE.saveConfiguration();
        Assert.assertTrue(RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
    }

    @Test
    public void setDuplicateFilesFalseTest() {
        RupsConfiguration.INSTANCE.setOpenDuplicateFiles(false);
        RupsConfiguration.INSTANCE.saveConfiguration();
        Assert.assertFalse(RupsConfiguration.INSTANCE.canOpenDuplicateFiles());
    }

    @Test
    public void setLookAndFeel() {
        String laf = "crossplatform";
        RupsConfiguration.INSTANCE.setLookAndFeel(laf);
        RupsConfiguration.INSTANCE.saveConfiguration();
        String lookAndFeel = RupsConfiguration.INSTANCE.getLookAndFeel();
        Assert.assertTrue(lookAndFeel.contains("javax.swing.plaf"));
    }

    @Test
    public void setSystemLookAndFeel() {
        String laf = "system";
        RupsConfiguration.INSTANCE.setLookAndFeel(laf);
        RupsConfiguration.INSTANCE.saveConfiguration();
        String lookAndFeel = RupsConfiguration.INSTANCE.getLookAndFeel();
        Assert.assertTrue(lookAndFeel.contains("javax.swing.plaf") || lookAndFeel.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"));
    }

    @Test
    public void clearUnsavedChangesTest() {
        RupsConfiguration.INSTANCE.setLookAndFeel("system");
        Assert.assertTrue(RupsConfiguration.INSTANCE.hasUnsavedChanges());
        RupsConfiguration.INSTANCE.cancelTemporaryChanges();
        Assert.assertFalse(RupsConfiguration.INSTANCE.hasUnsavedChanges());
    }

    @Test
    public void closingOperationsPossibleValuesTest() {
        int closeOperation = RupsConfiguration.INSTANCE.getCloseOperation();
        Assert.assertTrue(
                closeOperation == WindowConstants.DO_NOTHING_ON_CLOSE ||
                        closeOperation == WindowConstants.HIDE_ON_CLOSE ||
                        closeOperation == WindowConstants.EXIT_ON_CLOSE ||
                        closeOperation == WindowConstants.DISPOSE_ON_CLOSE
                );
    }

    @AfterClass
    public static void afterClass() {
        RupsConfiguration.INSTANCE.restore(copy);
    }

}
