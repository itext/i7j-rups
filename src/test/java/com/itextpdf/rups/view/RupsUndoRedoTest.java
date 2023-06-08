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

import com.itextpdf.kernel.actions.data.ITextCoreProductData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.uispec4j.Key;
import org.uispec4j.Panel;
import org.uispec4j.Table;
import org.uispec4j.Tree;
import org.uispec4j.Trigger;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

@Tag("Integration")
@Tag("GUI")
public class RupsUndoRedoTest extends RupsWindowTest {

    private static final String INPUT_FILE = "src/test/resources/com/itextpdf/rups/controller/hello_world.pdf";

    private static Window MAIN_WINDOW;
    private static Panel objectPanel;
    private static Tree pdfTree;
    private static final String TEST_KEY = "/Test";
    private static final String MODIFICATION_KEY = "/Producer";
    private static final String TEST_VALUE = "(Test)";

    public RupsUndoRedoTest(){
        super("Undo/Redo interaction Test");
    }
    private void validateInitialArray(Table infoArray) {
        Assertions.assertTrue(infoArray.getHeader().contentEquals("Array", "").isTrue(), "Table Header identifies the Content as an Array: ");
        Assertions.assertTrue(infoArray.rowCountEquals(3).isTrue(), "Table is 3 Cells Tall: ");
    }

    private void validateInitialDictionary(Table infoArray) {
        Assertions.assertTrue(infoArray.getHeader().contentEquals("Key", "Value", "").isTrue(), "Table Header identifies the Content as an Dictionary: ");
        Assertions.assertTrue(infoArray.rowCountEquals(4).isTrue(), "Table is 4 Cells Tall: ");
    }

    private void clickTree(String targetNode) {
        clickTree(targetNode, 1);
    }

    private void doubleClickTree(String targetNode) {
        clickTree(targetNode, 2);
    }

    private void clickTree(String targetNode, int times) {
        pdfTree.selectRoot();
        Assertions.assertTrue(pdfTree.contains(targetNode).isTrue(), String.format("PDFTree contains `%s` node: ", targetNode));
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


    @BeforeEach
    public void before() {
        try {
            setUp(INPUT_FILE);
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
            e.printStackTrace(System.err);
        }
        MAIN_WINDOW = getMainWindow();

        String windowTitle = String.format(Language.TITLE.getString(), ITextCoreProductData.getInstance().getVersion());
        waitUntil(MAIN_WINDOW.titleEquals(windowTitle), 8000);
        String documentPanelName = INPUT_FILE.substring(1 + INPUT_FILE.lastIndexOf('/'));
        waitUntil(MAIN_WINDOW.getPanel(documentPanelName).isEnabled(), 8000);
        pdfTree = MAIN_WINDOW.getTree("pdfTree");
        objectPanel = MAIN_WINDOW.getPanel("PdfObjectPanel");
    }

    @Test
    @Order(1)
    public void phase1canUndoArrayAddition() {


        clickTree("ID");
        Assertions.assertEquals(2,pdfTree.getChildCount("ID"), "Array Tree has 2 children: ");

        Table infoArray = objectPanel.getTable();
        validateInitialArray(infoArray);

        infoArray.editCell(2, 0, TEST_VALUE, true);

        WindowInterceptor.init(infoArray.triggerClick(2, 1, Key.Modifier.NONE))
                .process(new WindowHandler() {
                    @Override
                    public Trigger process(Window dialog) throws Exception {
                        Assertions.assertEquals("Input", dialog.getTitle(), "Dialog Title is 'Input'");
                        Assertions.assertEquals("2", dialog.getInputTextBox().getText(), "Row Value defaults to: 2");
                        return dialog.getButton("OK").triggerClick();
                    }
                }).run();
        waitUntil("Table is now 4 Cells Tall: ", infoArray.rowCountEquals(4), 5000);
        Assertions.assertEquals(3,pdfTree.getChildCount("ID"), "Array Tree has 3 children: ");
        Assertions.assertEquals(TEST_VALUE, (String) infoArray.getContentAt(2, 0), "Value of cell content is equal to the test value provided: ");
        UNDO(infoArray);
        Assertions.assertTrue(infoArray.rowCountEquals(3).isTrue(), "Table is 3 Cells Tall again: ");
        Assertions.assertEquals("", infoArray.getContentAt(2, 0), "Test value is no longer in the table: ");
        Assertions.assertEquals(2, pdfTree.getChildCount("ID"), "Array Tree has 2 children: ");


    }

    @Test
    @Order(1)
    public void phase1canUndoArrayUpdate() {
        // Click Tree
        clickTree("ID");
        Assertions.assertEquals(2, pdfTree.getChildCount("ID"), "Array Tree has 2 children: ");
        Table infoArray = objectPanel.getTable();
        validateInitialArray(infoArray);
        // Store Original Value
        Assertions.assertEquals(String.class, infoArray.getContentAt(0, 0).getClass(), "Value is a String: ");
        String originalValue = (String) infoArray.getContentAt(0, 0);
        // Change Value to "(Test)"
        infoArray.editCell(0, 0, TEST_VALUE, true);
        // Press Enter Key
        infoArray.pressKey(Key.ENTER);
        // Test Updated value
        Assertions.assertEquals(TEST_VALUE, (String) infoArray.getContentAt(0, 0), "Cell Value is equal to Test Value: ");
        Assertions.assertEquals(2, pdfTree.getChildCount("ID"), "Array Tree still has 2 children: ");
        // Press Ctrl-Z
        UNDO(infoArray);
        // Test Updated Value is back to Original
        Assertions.assertEquals(originalValue, (String) infoArray.getContentAt(0,0), "Value has reset to original: ");
        validateInitialArray(infoArray);
        Assertions.assertEquals(2, pdfTree.getChildCount("ID"), "Array Tree still has 2 children: ");
    }

    @Test
    @Order(1)
    public void phase1canUndoArrayDeletion() {
        // Click Tree
        clickTree("ID");

        Assertions.assertEquals(2, pdfTree.getChildCount("ID"), "Array Tree has 2 children: ");

        Table infoArray = objectPanel.getTable();
        validateInitialArray(infoArray);
        // Store Original Value
        Assertions.assertEquals(String.class, infoArray.getContentAt(0, 0).getClass(), "Value is a String: ");
        String originalValue = (String) infoArray.getContentAt(0, 0);
        // Click Table X Button
        infoArray.click(0, 1, Key.Modifier.NONE);
        // Test Tree Size
        Assertions.assertEquals(1, pdfTree.getChildCount("ID"), "Array Tree has 1 child: ");
        // Test Table Length
        Assertions.assertEquals(2, infoArray.getRowCount(), "Table is now 2 Cells Tall: ");
        // Press Ctrl-Z
        UNDO(infoArray);
        Assertions.assertEquals(2, pdfTree.getChildCount("ID"), "Array Tree has 2 children again: ");
        // Test Table Length
        validateInitialArray(infoArray);
        // Test Updated Value is back to Original
        Assertions.assertEquals(originalValue, (String) infoArray.getContentAt(0, 0), "Value has reset to original: ");
    }

    @Test
    @Order(1)
    public void phase1canUndoDictAddition() {
        doubleClickTree("Info");
        Assertions.assertEquals(3,pdfTree.getChildCount("Info/Dictionary"), "Dict Tree has 3 children: ");
        Table infoDict = objectPanel.getTable();
        validateInitialDictionary(infoDict);
        infoDict.editCell(3,0,TEST_KEY,true);
        infoDict.editCell(3,1,TEST_VALUE,true);
        infoDict.click(3,2);
        Assertions.assertEquals(4,pdfTree.getChildCount("Info/Dictionary"), "Dictionary Tree has 4 children: ");
        Assertions.assertEquals(5, infoDict.getRowCount(), "Table is now 5 Rows tall: ");
        Assertions.assertEquals(TEST_KEY, (String) infoDict.getContentAt(3,0), "New Key is equal to Test Key: ");
        Assertions.assertEquals(TEST_VALUE, (String) infoDict.getContentAt(3,1), "New Value is equal to Test Value: ");
        UNDO(infoDict);
        Assertions.assertEquals(3,pdfTree.getChildCount("Info/Dictionary"), "Dict Tree has 3 children again: ");
        validateInitialDictionary(infoDict);
        assertFalse("Dict Tree no longer contains the Test Key: ", pdfTree.contains(String.format("Info/Dictionary%s",TEST_KEY)));
    }

    @Test
    @Order(1)
    public void phase1canUndoDictUpdate() {
        doubleClickTree("Info");
        Table infoDict = objectPanel.getTable();
        validateInitialDictionary(infoDict);
        String originalValue = (String) infoDict.getContentAt(2, 1);
        Assertions.assertEquals(MODIFICATION_KEY, (String) infoDict.getContentAt(2,0), "Key to Modify is on the expected row: ");
        infoDict.editCell(2,1,TEST_VALUE, true);
        Assertions.assertEquals(3,pdfTree.getChildCount("Info/Dictionary"), "Dictionary Tree has 3 children: ");
        Assertions.assertEquals(MODIFICATION_KEY, (String) infoDict.getContentAt(2,0), "Row Key is Unchanged");
        Assertions.assertEquals(TEST_VALUE, (String) infoDict.getContentAt(2,1), "Modified Cell is set to Test Value");
        UNDO(infoDict);
        validateInitialDictionary(infoDict);
        Assertions.assertEquals(originalValue, (String) infoDict.getContentAt(2,1), "Value has reset to original: ");
    }

    @Test
    @Order(1)
    public void phase1canUndoDictDeletion() {
        doubleClickTree("Info");
        Table infoDict = objectPanel.getTable();
        validateInitialDictionary(infoDict);
        infoDict.click(2,2);
        Assertions.assertEquals(2,pdfTree.getChildCount("Info/Dictionary"), "Dictionary Tree has 2 children: ");
        Assertions.assertEquals(3, infoDict.getRowCount(), "Table is now 3 Rows tall: ");
        UNDO(infoDict);
        Assertions.assertEquals(3,pdfTree.getChildCount("Info/Dictionary"), "Dictionary Tree has 3 children: ");
        Assertions.assertEquals(4, infoDict.getRowCount(), "Table is now 4 Rows tall: ");
    }

    @Test
    @Order(2)
    public void phase2canRedoArrayAddition() {

        // run canUndo test
        phase1canUndoArrayAddition();

        Table infoArray = objectPanel.getTable();
        // Press Ctrl-Y
        REDO(infoArray);
        Assertions.assertEquals(3, pdfTree.getChildCount("ID"), "Array Tree has 3 children again: ");
        // Test Table Length
        Assertions.assertEquals(4, infoArray.getRowCount(), "Table is now 4 Cells Tall again: ");
        // Test Table Value
        Assertions.assertEquals(TEST_VALUE, (String) infoArray.getContentAt(2, 0), "Value of cell content is equal to the test value provided: ");
    }

    @Test
    @Order(2)
    public void phase2canRedoArrayUpdate() {
        // run canUndo test
        phase1canUndoArrayUpdate();
        Table infoArray = objectPanel.getTable();
        // Press Ctrl-Y
        REDO(infoArray);
        Assertions.assertEquals(2, pdfTree.getChildCount("ID"), "Array Tree still has 2 children: ");
        // Test Table Value
        Assertions.assertEquals(TEST_VALUE, (String) infoArray.getContentAt(0, 0), "Value of cell content is equal to the test value again: ");
    }

    @Test
    @Order(2)
    public void phase2canRedoArrayDeletion() {
        // run canUndo test
        phase1canUndoArrayDeletion();
        Table infoArray = objectPanel.getTable();
        // Press Ctrl-Y
        REDO(infoArray);
        Assertions.assertEquals(1, pdfTree.getChildCount("ID"), "Array Tree has 1 child again: ");
        // Test Table Length
        Assertions.assertEquals(2, infoArray.getRowCount(), "Table is now 2 Cells Tall again: ");
    }

    @Test
    @Order(2)
    public void phase2canRedoDictAddition() {
        phase1canUndoDictAddition();
        Table infoDict = objectPanel.getTable();
        REDO(infoDict);
        Assertions.assertEquals(4,pdfTree.getChildCount("Info/Dictionary"), "Dictionary Tree has 4 children: ");
        Assertions.assertEquals(5, infoDict.getRowCount(), "Table is now 5 Rows tall: ");
        Assertions.assertEquals(TEST_KEY, (String) infoDict.getContentAt(3,0), "New Key is equal to Test Key: ");
        Assertions.assertEquals(TEST_VALUE, (String) infoDict.getContentAt(3,1), "New Value is equal to Test Value: ");
    }

    @Test
    @Order(2)
    public void phase2canRedoDictUpdate() {
        phase1canUndoDictUpdate();
        Table infoDict = objectPanel.getTable();
        REDO(infoDict);
        Assertions.assertEquals(3,pdfTree.getChildCount("Info/Dictionary"), "Dictionary Tree has 3 children: ");
        Assertions.assertEquals(4, infoDict.getRowCount(), "Table is still 4 Rows tall: ");
        Assertions.assertEquals(MODIFICATION_KEY, (String) infoDict.getContentAt(2,0), "Modification Key is unchanged: ");
        Assertions.assertEquals(TEST_VALUE, (String) infoDict.getContentAt(2,1), "New Value is equal to Test Value: ");
    }

    @Test
    @Order(2)
    public void phase2canRedoDictDeletion() {
        phase1canUndoDictDeletion();
        Table infoDict = objectPanel.getTable();
        REDO(infoDict);
        Assertions.assertEquals(2,pdfTree.getChildCount("Info/Dictionary"), "Dictionary Tree has 2 children: ");
        Assertions.assertEquals(3, infoDict.getRowCount(), "Table is now 3 Rows tall: ");
    }
}
