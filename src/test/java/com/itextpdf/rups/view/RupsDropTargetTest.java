package com.itextpdf.rups.view;

import com.itextpdf.rups.mock.MockedDropTargetDropEvent;
import com.itextpdf.rups.mock.MockedRupsController;
import com.itextpdf.rups.mock.MockedTransferable;
import com.itextpdf.test.ITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class RupsDropTargetTest extends ITextTest {

    private RupsDropTarget dropTarget;
    private List<File> fileList;
    private MockedRupsController rupsController;

    @Before
    public void beforeTest() {
        this.rupsController = new MockedRupsController();
        this.dropTarget = new RupsDropTarget(rupsController);
        this.fileList = Arrays.asList(new File("a.pdf"), new File("b.pdf"));
    }

    @Test
    public void nullTest() {
        this.dropTarget.drop(null);
        Assert.assertEquals(0, this.rupsController.getOpenedCount());
    }

    @Test
    public void noDataFlavorTest() {
        Transferable transferable = new MockedTransferable(null);
        List<File> files = this.dropTarget.extractFilesFromTransferable(transferable);
        Assert.assertNotNull(files);
        Assert.assertTrue(files.isEmpty());
    }

    @Test
    public void noSupportedDataFlavorTest() {
        Transferable transferable = new MockedTransferable(DataFlavor.imageFlavor);
        List<File> files = this.dropTarget.extractFilesFromTransferable(transferable);
        Assert.assertNotNull(files);
        Assert.assertTrue(files.isEmpty());
    }

    @Test
    public void javaFileListFlavorTest() {
        Transferable transferable = new MockedTransferable(DataFlavor.javaFileListFlavor, fileList);
        List<File> files = this.dropTarget.extractFilesFromTransferable(transferable);
        Assert.assertNotNull(files);
        Assert.assertEquals(this.fileList.size(), files.size());
    }

    @Test
    public void stringFlavorTest() throws MalformedURLException {
        StringBuilder builder = new StringBuilder();

        for ( File file : this.fileList ) {
            builder.append(file.toURL());
            builder.append(" ");
        }

        Transferable transferable = new MockedTransferable(DataFlavor.stringFlavor, builder.toString());
        List<File> files = this.dropTarget.extractFilesFromTransferable(transferable);
        Assert.assertNotNull(files);
        Assert.assertEquals(this.fileList.size(), files.size());
    }

    @Test
    public void test() {
        MockedTransferable mockedTransferable = new MockedTransferable(DataFlavor.javaFileListFlavor, fileList);
        DropTarget dropTarget = new DropTarget();
        this.dropTarget.drop(new MockedDropTargetDropEvent(mockedTransferable, dropTarget));
        Assert.assertEquals(fileList.size(), this.rupsController.getOpenedCount());
    }
}
