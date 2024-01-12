/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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

import java.util.Set;

/**
 * A class that keeps track of the current indentation level in a content stream.
 */
public class IndentManager {
    private static final Set<String> INDENTING_OPERATORS = Set.of("BT", "q", "BMC", "BDC", "BX", "m", "re");
    private static final Set<String> UNINDENTING_OPERATORS = Set.of("ET", "Q", "EMC", "EX", "b", "B", "f", "f*", "F",
            "B*", "b*", "n", "s", "S");
    private static final Set<String> SUBPATH_CREATION_OPERATORS = Set.of("m", "re");
    private static final Set<String> PATH_PAINTING_OPERATORS = Set.of("b", "B", "f", "f*", "F", "B*", "b*", "n", "s",
            "S");

    private int indentLevel;
    private boolean isIndentingPath;

    /**
     * Creates a new IndentManager, with an initial indentation level of 0.
     */
    public IndentManager() {
        indentLevel = 0;
        isIndentingPath = false;
    }

    /**
     * Increase the indentation level if the operator is one that increases the indentation level.
     *
     * @param operator The operator to check
     */
    public void indentIfNecessary(PdfObject operator) {
        String operatorString = operator.toString();
        if (!INDENTING_OPERATORS.contains(operatorString)) {
            return;
        }
        if (SUBPATH_CREATION_OPERATORS.contains(operatorString)) {
            if (!isIndentingPath) {
                // Only indent subpath creation operators if we're not already indenting a path
                indentLevel++;
                isIndentingPath = true;
            }
        } else {
            // Always indent operators that don't create a new subpath
            indentLevel++;
        }
    }

    /**
     * Decrease the indentation level if the operator is one that decreases the indentation level,
     * and the current indentation level is greater than 0.
     *
     * @param operator The operator to check
     */
    public void unindentIfNecessary(PdfObject operator) {
        String operatorString = operator.toString();
        if (indentLevel > 0 && UNINDENTING_OPERATORS.contains(operatorString)) {
            indentLevel--;
            if (PATH_PAINTING_OPERATORS.contains(operatorString)) {
                isIndentingPath = false;
            }
        }
    }

    /**
     * Get the current indentation level.
     *
     * @return the current indentation level
     */
    public int getIndentLevel() {
        return indentLevel;
    }

    /**
     * Reset the indentation level to 0.
     */
    public void reset() {
        indentLevel = 0;
    }
}
