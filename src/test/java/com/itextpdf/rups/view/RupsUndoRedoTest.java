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

import com.itextpdf.rups.Rups;
import com.itextpdf.rups.view.icons.IconButton;
import org.junit.Before;
import org.junit.Test;
import org.uispec4j.Panel;
import org.uispec4j.Table;
import org.uispec4j.Tree;
import org.uispec4j.Window;
import org.uispec4j.utils.Log;

public class RupsUndoRedoTest extends RupsWindowTest{

    private static final String INPUT_FILE = "src/test/resources/com/itextpdf/rups/controller/hello_world.pdf";

    private static Window MAIN_WINDOW;
    private static Panel objectPanel;
    private static Tree pdfTree;

    //TODO: Work out why the hell this is happening:
    // [ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7:test (default-test) on project itext-rups: Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7:test failed: java.lang.NoClassDefFoundError: org/apache/maven/surefire/api/report/ReporterFactory: org.apache.maven.surefire.api.report.ReporterFactory -> [Help 1]
    // org.apache.maven.lifecycle.LifecycleExecutionException: Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7:test (default-test) on project itext-rups: Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7:test failed: java.lang.NoClassDefFoundError: org/apache/maven/surefire/api/report/ReporterFactory

    @Before
    public void before() {
       try{
           setUp(INPUT_FILE);
       } catch (Exception e){
           fail(e.getMessage());
           e.printStackTrace(System.err);
       }
       MAIN_WINDOW = getMainWindow();

       pdfTree = MAIN_WINDOW.getTree("pdfTree");
       objectPanel = MAIN_WINDOW.getPanel("PdfObjectPanel");
    }

    @Test
    public void canUndoArrayAddition(){
        String arrayEntry = "Test";

        pdfTree.selectRoot();
        assertTrue("PDFTree contains `ID` node: ",pdfTree.contains("ID"));
        pdfTree.click("ÏD");
        Table infoArray = objectPanel.getTable();

        assertTrue("Table Header identifies the Content as an Array: ", infoArray.getHeader().contentEquals(new String[]{"Array", ""}).isTrue());
        assertEquals("Table is 3 Cells Tall: ",3,infoArray.getRowCount());

        infoArray.editCell(3,1, arrayEntry, true);
        Object cellContent = infoArray.getContentAt(3,2);

        assertTrue("The Content of the second Column is an IconButton: ", cellContent instanceof IconButton);

        ((IconButton) cellContent).doClick();

        assertEquals("Table is now 4 Cells Tall: ",4,infoArray.getRowCount());
        //TODO: Validate why this isn't failing, since the table is giving a ghost duplicate for the added row.

        cellContent = infoArray.getContentAt(3,1);

        assertTrue("The Content of the First Column is a String: ", cellContent instanceof String);

        assertEquals("Value of cell content is equal to the test value provided: ", arrayEntry, (String)cellContent);

    }

    @Test
    public void canUndoArrayUpdate(){
    }

    @Test
    public void canUndoArrayDeletion(){
    }

    @Test
    public void canUndoDictAddition(){
    }

    @Test
    public void canUndoDictUpdate(){
    }

    @Test
    public void canUndoDictDeletion(){
    }

    @Test
    public void canRedoArrayAddition(){
    }

    @Test
    public void canRedoArrayUpdate(){
    }

    @Test
    public void canRedoArrayDeletion(){
    }

    @Test
    public void canRedoDictAddition(){
    }

    @Test
    public void canRedoDictUpdate(){
    }

    @Test
    public void canRedoDictDeletion(){
    }
}
