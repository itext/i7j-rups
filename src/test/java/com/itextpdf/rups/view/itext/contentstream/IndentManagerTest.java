/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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
package com.itextpdf.rups.view.itext.contentstream;

import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IndentManagerTest {
    private IndentManager indentManager;
    private PdfObject indentingOperator;
    private PdfObject unindentingOperator;

    @BeforeEach
    public void setUp() {
        indentManager = new IndentManager();
        indentingOperator = new PdfString("BT");
        unindentingOperator = new PdfString("ET");
    }

    @Test
    void testConstructor() {
        assertEquals(0, indentManager.getIndentLevel());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BT", "q", "BMC", "BDC", "BX", "m", "re"})
    void testIndentingOperator(String operator) {
        indentManager.indentIfNecessary(new PdfString(operator));
        assertEquals(1, indentManager.getIndentLevel());
    }

    @Test
    void testIndentingOperatorNotIndenting() {
        indentManager.indentIfNecessary(unindentingOperator);
        assertEquals(0, indentManager.getIndentLevel());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ET", "Q", "EMC", "EX", "b", "B", "f", "f*", "F", "B*", "b*", "n", "s", "S"})
    void testUnindentingOperator(String operator) {
        indentManager.indentIfNecessary(indentingOperator);
        indentManager.unindentIfNecessary(new PdfString(operator));
        assertEquals(0, indentManager.getIndentLevel());
    }

    @Test
    void testUnindentingOperatorNotUnindenting() {
        indentManager.indentIfNecessary(indentingOperator);
        indentManager.unindentIfNecessary(indentingOperator);
        assertEquals(1, indentManager.getIndentLevel());
    }

    @Test
    void testReset() {
        indentManager.indentIfNecessary(indentingOperator);
        indentManager.indentIfNecessary(indentingOperator);
        indentManager.indentIfNecessary(indentingOperator);
        indentManager.reset();
        assertEquals(0, indentManager.getIndentLevel());
    }

    @Test
    void testUnindentingNotBelowZero() {
        indentManager.unindentIfNecessary(unindentingOperator);
        assertEquals(0, indentManager.getIndentLevel());
    }

    @Test
    void testIndentingMultiple() {
        indentManager.indentIfNecessary(indentingOperator);
        indentManager.indentIfNecessary(indentingOperator);
        indentManager.indentIfNecessary(indentingOperator);
        indentManager.unindentIfNecessary(unindentingOperator);
        indentManager.unindentIfNecessary(unindentingOperator);
        indentManager.unindentIfNecessary(unindentingOperator);
        assertEquals(0, indentManager.getIndentLevel());
    }
}
