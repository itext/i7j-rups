/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups.view;

import com.github.caciocavallosilano.cacio.ctc.junit.CacioTestRunner;
import com.itextpdf.kernel.actions.data.ITextCoreProductData;
import org.assertj.swing.annotation.GUITest;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.OrderWith;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Orderable;
import org.junit.runner.manipulation.Ordering;
import org.junit.runners.MethodSorters;
import org.uispec4j.Key;
import org.uispec4j.Panel;
import org.uispec4j.Table;
import org.uispec4j.Tree;
import org.uispec4j.Trigger;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.utils.Log;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;
import com.itextpdf.test.annotations.type.IntegrationTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
@RunWith(CacioTestRunner.class)
public class RupsUndoRedoTest extends RupsWindowTest {

    private static final String INPUT_FILE = "src/test/resources/com/itextpdf/rups/controller/hello_world.pdf";

    private static Window MAIN_WINDOW;
    private static Panel objectPanel;
    private static Tree pdfTree;
    private static final String TEST_KEY = "/Test";
    private static final String MODIFICATION_KEY = "/Producer";
    private static final String TEST_VALUE = "(Test)";

    //TODO: Work out why the hell this is happening:
    // [ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7:test (default-test) on project itext-rups: Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7:test failed: java.lang.NoClassDefFoundError: org/apache/maven/surefire/api/report/ReporterFactory: org.apache.maven.surefire.api.report.ReporterFactory -> [Help 1]
    // org.apache.maven.lifecycle.LifecycleExecutionException: Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7:test (default-test) on project itext-rups: Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7:test failed: java.lang.NoClassDefFoundError: org/apache/maven/surefire/api/report/ReporterFactory

    private void validateInitialArray(Table infoArray) {
        assertTrue("Table Header identifies the Content as an Array: ", infoArray.getHeader().contentEquals(new String[]{"Array", ""}).isTrue());
        assertThat("Table is 3 Cells Tall: ", infoArray.rowCountEquals(3));
    }

    private void validateInitialDictionary(Table infoArray) {
        assertTrue("Table Header identifies the Content as an Dictionary: ", infoArray.getHeader().contentEquals(new String[]{"Key", "Value", ""}).isTrue());
        assertThat("Table is 4 Cells Tall: ", infoArray.rowCountEquals(4));
    }

    private void clickTree(String targetNode) {
        clickTree(targetNode, 1);
    }

    private void doubleClickTree(String targetNode) {
        clickTree(targetNode, 2);
    }

    private void clickTree(String targetNode, int times) {
        pdfTree.selectRoot();
        assertTrue(String.format("PDFTree contains `%s` node: ", targetNode), pdfTree.contains(targetNode));
        if (times == 2) {
            pdfTree.doubleClick(targetNode);
        } else {
            while (times > 0) {
                pdfTree.click(targetNode);
                times--;
            }
        }
    }

    private static void UNDO(UIComponent component) {
        component.pressKey(Key.control(Key.Z));
    }

    private static void REDO(UIComponent component) {
        component.pressKey(Key.control(Key.Y));
    }


    @Before
    public void before() {
        try {
            setUp(INPUT_FILE);
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace(System.err);
        }
        MAIN_WINDOW = getMainWindow();

        // TODO: Change this to match the Maven defined Snapshot name.
        waitUntil(MAIN_WINDOW.titleEquals(String.format(Language.TITLE.getString(), ITextCoreProductData.getInstance().getVersion())), 8000);
        // TODO: Change this to test the panel name.
        waitUntil(MAIN_WINDOW.getPanel(INPUT_FILE.substring(1 + INPUT_FILE.lastIndexOf('/'))).isEnabled(), 8000);
        pdfTree = MAIN_WINDOW.getTree("pdfTree");
        objectPanel = MAIN_WINDOW.getPanel("PdfObjectPanel");
    }

    @Test
    public void phase1canUndoArrayAddition() {


        clickTree("ID");
        assertEquals("Array Tree has 2 children: ",2,pdfTree.getChildCount("ID"));

        Table infoArray = objectPanel.getTable();
        validateInitialArray(infoArray);

        infoArray.editCell(2, 1, TEST_VALUE, true);

        WindowInterceptor.init(infoArray.triggerClick(2, 1, Key.Modifier.NONE))
                .process(new WindowHandler() {
                    @Override
                    public Trigger process(Window dialog) throws Exception {
                        assertEquals("Dialog Title is 'Input'", "Input", dialog.getTitle());
                        assertEquals("Row Value defaults to: 2", "2", dialog.getInputTextBox().getText());
                        return dialog.getButton("OK").triggerClick();
                    }
                }).run();
        waitUntil("Table is now 4 Cells Tall: ", infoArray.rowCountEquals(4), 5000);
        assertEquals("Array Tree has 3 children: ",3,pdfTree.getChildCount("ID"));
        assertEquals("Value of cell content is equal to the test value provided: ", TEST_VALUE, (String) infoArray.getContentAt(2, 0));
        UNDO(infoArray);
        assertThat("Table is 3 Cells Tall again: ", infoArray.rowCountEquals(3));
        assertEquals("Test value is no longer in the table: ", "", infoArray.getContentAt(2, 0));
        assertEquals("Array Tree has 2 children: ",2,pdfTree.getChildCount("ID"));


    }

    @Test
    public void phase1canUndoArrayUpdate() {
        // Click Tree
        clickTree("ID");
        assertEquals("Array Tree has 2 children: ", 2, pdfTree.getChildCount("ID"));
        Table infoArray = objectPanel.getTable();
        validateInitialArray(infoArray);
        // Store Original Value
        assertEquals("Value is a String: ", String.class, infoArray.getContentAt(0, 0).getClass());
        String originalValue = (String) infoArray.getContentAt(0, 0);
        // Double-Click Table
        //        infoArray.doubleClick(0, 0);
        //        infoArray.selectCell(0, 0);
        // Change Value to "(Test)"
        infoArray.editCell(0, 0, TEST_VALUE, true);
        // Press Enter Key
        infoArray.pressKey(Key.ENTER);
        // Test Updated value
        assertEquals("Cell Value is equal to Test Value: ", TEST_VALUE, (String) infoArray.getContentAt(0, 0));
        assertEquals("Array Tree still has 2 children: ", 2, pdfTree.getChildCount("ID"));
        // Press Ctrl-Z
        UNDO(infoArray);
        // Test Updated Value is back to Original
        assertEquals("Value has reset to original: ", originalValue, (String) infoArray.getContentAt(0,0));
        validateInitialArray(infoArray);
        assertEquals("Array Tree still has 2 children: ", 2, pdfTree.getChildCount("ID"));
    }

    @Test
    public void phase1canUndoArrayDeletion() {
        // Click Tree
        clickTree("ID");

        assertEquals("Array Tree has 2 children: ", 2, pdfTree.getChildCount("ID"));

        Table infoArray = objectPanel.getTable();
        validateInitialArray(infoArray);
        // Store Original Value
        assertEquals("Value is a String: ", String.class, infoArray.getContentAt(0, 0).getClass());
        String originalValue = (String) infoArray.getContentAt(0, 0);
        // Click Table X Button
        infoArray.click(0, 1, Key.Modifier.NONE);
        // Test Tree Size
        assertEquals("Array Tree has 1 child: ", 1, pdfTree.getChildCount("ID"));
        // Test Table Length
        assertEquals("Table is now 2 Cells Tall: ", 2, infoArray.getRowCount());
        // Press Ctrl-Z
        UNDO(infoArray);
        assertEquals("Array Tree has 2 children again: ", 2, pdfTree.getChildCount("ID"));
        // Test Table Length
        validateInitialArray(infoArray);
        // Test Updated Value is back to Original
        assertEquals("Value has reset to original: ", originalValue, (String) infoArray.getContentAt(0, 0));
    }

    @Test
    public void phase1canUndoDictAddition() {
        doubleClickTree("Info");
        assertEquals("Dict Tree has 3 children: ",3,pdfTree.getChildCount("Info/Dictionary"));
        Table infoDict = objectPanel.getTable();
        validateInitialDictionary(infoDict);
        infoDict.editCell(3,0,TEST_KEY,true);
        infoDict.editCell(3,1,TEST_VALUE,true);
        infoDict.click(3,2);
        assertEquals("Dictionary Tree has 4 children: ",4,pdfTree.getChildCount("Info/Dictionary"));
        assertEquals("Table is now 5 Rows tall: ", 5, infoDict.getRowCount());
        assertEquals("New Key is equal to Test Key: ", TEST_KEY, (String) infoDict.getContentAt(3,0));
        assertEquals("New Value is equal to Test Value: ", TEST_VALUE, (String) infoDict.getContentAt(3,1));
        UNDO(infoDict);
        assertEquals("Dict Tree has 3 children again: ",3,pdfTree.getChildCount("Info/Dictionary"));
        validateInitialDictionary(infoDict);
        assertFalse("Dict Tree no longer contains the Test Key: ", pdfTree.contains(String.format("Info/Dictionary%s",TEST_KEY)));
    }

    @Test
    public void phase1canUndoDictUpdate() {
        doubleClickTree("Info");
        Table infoDict = objectPanel.getTable();
        validateInitialDictionary(infoDict);
        String originalValue = (String) infoDict.getContentAt(2, 1);
        assertEquals("Key to Modify is on the expected row: ", MODIFICATION_KEY, (String) infoDict.getContentAt(2,0));
        infoDict.editCell(2,1,TEST_VALUE, true);
        assertEquals("Dictionary Tree has 3 children: ",3,pdfTree.getChildCount("Info/Dictionary"));
        assertEquals("Row Key is Unchanged", MODIFICATION_KEY, (String) infoDict.getContentAt(2,0));
        assertEquals("Modified Cell is set to Test Value", TEST_VALUE, (String) infoDict.getContentAt(2,1));
        UNDO(infoDict);
        validateInitialDictionary(infoDict);
        assertEquals("Value has reset to original: ", originalValue, (String) infoDict.getContentAt(2,1));
    }

    @Test
    public void phase1canUndoDictDeletion() {
        doubleClickTree("Info");
        Table infoDict = objectPanel.getTable();
        validateInitialDictionary(infoDict);
        infoDict.click(2,2);
        assertEquals("Dictionary Tree has 2 children: ",2,pdfTree.getChildCount("Info/Dictionary"));
        assertEquals("Table is now 3 Rows tall: ", 3, infoDict.getRowCount());
        UNDO(infoDict);
        assertEquals("Dictionary Tree has 3 children: ",3,pdfTree.getChildCount("Info/Dictionary"));
        assertEquals("Table is now 4 Rows tall: ", 4, infoDict.getRowCount());
//        fail("Test Not Implemented");
    }

    @Test
    public void phase2canRedoArrayAddition() {

        // run canUndo test
        phase1canUndoArrayAddition();

        Table infoArray = objectPanel.getTable();
        // Press Ctrl-Y
        REDO(infoArray);
        assertEquals("Array Tree has 3 children again: ", 3, pdfTree.getChildCount("ID"));
        // Test Table Length
        assertEquals("Table is now 4 Cells Tall again: ", 4, infoArray.getRowCount());
        // Test Table Value
        assertEquals("Value of cell content is equal to the test value provided: ", TEST_VALUE, (String) infoArray.getContentAt(2, 0));
    }

    @Test
    public void phase2canRedoArrayUpdate() {
        // run canUndo test
        phase1canUndoArrayUpdate();
        Table infoArray = objectPanel.getTable();
        // Press Ctrl-Y
        REDO(infoArray);
        assertEquals("Array Tree still has 2 children: ", 2, pdfTree.getChildCount("ID"));
        // Test Table Value
        assertEquals("Value of cell content is equal to the test value again: ", TEST_VALUE, (String) infoArray.getContentAt(0, 0));
    }

    @Test
    public void phase2canRedoArrayDeletion() {
        // run canUndo test
        phase1canUndoArrayDeletion();
        Table infoArray = objectPanel.getTable();
        // Press Ctrl-Y
        REDO(infoArray);
        assertEquals("Array Tree has 1 child again: ", 1, pdfTree.getChildCount("ID"));
        // Test Table Length
        assertEquals("Table is now 2 Cells Tall again: ", 2, infoArray.getRowCount());
    }

    @Test
    public void phase2canRedoDictAddition() {
        phase1canUndoDictAddition();
        Table infoDict = objectPanel.getTable();
        REDO(infoDict);
        assertEquals("Dictionary Tree has 4 children: ",4,pdfTree.getChildCount("Info/Dictionary"));
        assertEquals("Table is now 5 Rows tall: ", 5, infoDict.getRowCount());
        assertEquals("New Key is equal to Test Key: ", TEST_KEY, (String) infoDict.getContentAt(3,0));
        assertEquals("New Value is equal to Test Value: ", TEST_VALUE, (String) infoDict.getContentAt(3,1));
    }

    @Test
    public void phase2canRedoDictUpdate() {
        phase1canUndoDictUpdate();
        Table infoDict = objectPanel.getTable();
        REDO(infoDict);
        assertEquals("Dictionary Tree has 3 children: ",3,pdfTree.getChildCount("Info/Dictionary"));
        assertEquals("Table is still 4 Rows tall: ", 4, infoDict.getRowCount());
        assertEquals("Modification Key is unchanged: ", MODIFICATION_KEY, (String) infoDict.getContentAt(2,0));
        assertEquals("New Value is equal to Test Value: ", TEST_VALUE, (String) infoDict.getContentAt(2,1));
//        fail("Test Not Implemented");
    }

    @Test
    public void phase2canRedoDictDeletion() {
        phase1canUndoDictDeletion();
        Table infoDict = objectPanel.getTable();
        REDO(infoDict);
        assertEquals("Dictionary Tree has 2 children: ",2,pdfTree.getChildCount("Info/Dictionary"));
        assertEquals("Table is now 3 Rows tall: ", 3, infoDict.getRowCount());
//        fail("Test Not Implemented");
    }
}
