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

import com.github.caciocavallosilano.cacio.ctc.junit.CacioAssertJRunner;
import com.itextpdf.rups.RupsLauncher;
import com.itextpdf.test.annotations.type.IntegrationTest;
import junit.framework.TestCase;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.toolkit.UISpecDisplay;

@Category(IntegrationTest.class)
@RunWith(GUITestRunner.class)
//@RunWith(CacioAssertJRunner.class)
public abstract class RupsWindowTest extends TestCase {
    //TODO: Work out what issues are that the compiler is having with the CacioTestRunner class. - Done
    //SEE: https://github.com/UISpec4J/UISpec4J/blob/fdc0b420cabb36134dd99830434c8fd2d05d508d/uispec4j/src/main/java/org/uispec4j/UISpecTestCase.java
    //SEE: https://github.com/CaciocavalloSilano/caciocavallo/blob/master/cacio-tta/src/test/java/com/github/caciocavallosilano/cacio/ctc/MouseInfoTest.java
    static final String ADAPTER_CLASS_PROPERTY = "uispec4j.adapter";
    static final String PROPERTY_NOT_DEFINED;

    private UISpecAdapter adapter;

    static {
        PROPERTY_NOT_DEFINED =
                "Adapter class not defined - the '" + ADAPTER_CLASS_PROPERTY +
                        "' property must refer to a class implementing the UISpecAdapter interface";
        UISpec4J.init();
    }

    protected RupsWindowTest() {
    }
    protected RupsWindowTest(String testName) {
        super(testName);
    }

    public void setAdapter(UISpecAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    protected void setUp() throws Exception {
        setUp("");
    }

    /**
     * Initializes the resources needed by the test case.<br>
     * NB: If you provide your own implementation, do not forget to call this one first.
     */


    protected void setUp(String filePath) throws Exception {
        super.setUp();
        String fileName = filePath.substring(1 + filePath.lastIndexOf('/'));
        UISpecDisplay.instance().reset();
        setAdapter(new MainClassAdapter(RupsLauncher.class, new String[]{filePath}));
        waitUntil(getMainWindow().containsSwingComponent(RupsPanel.class), 8000);
    }

    /**
     * Checks whether an unexpected exception had occurred, and releases the test resources.
     */
    protected void tearDown() throws Exception {
        adapter = null;
        UISpecDisplay.instance().rethrowIfNeeded();
        UISpecDisplay.instance().reset();
        super.tearDown();
    }

    private void retrieveAdapter() throws AdapterNotFoundException {
        String adapterClassName = System.getProperty(ADAPTER_CLASS_PROPERTY);
        if (adapterClassName == null) {
            throw new AdapterNotFoundException();
        }
        try {
            adapter = (UISpecAdapter)Class.forName(adapterClassName).getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
            throw new AdapterNotFoundException(adapterClassName, e);
        }
    }

    /**
     * Returns the Window created by the adapter.
     *
     * @throws AdapterNotFoundException if the <code>uispec4j.adapter</code> property does not refer
     *                                  to a valid adapter
     */
    public Window getMainWindow() throws AdapterNotFoundException {
        // TODO: Find a way to intercept and interact with Modal Dialogs
        return getAdapter().getMainWindow();
    }

    /**
     * Checks the given assertion.
     * This method is equivalent to {@link #assertThat(Assertion)}.
     *
     * @see UISpecAssert#assertTrue(Assertion)
     */
    public void assertTrue(Assertion assertion) {
        UISpecAssert.assertTrue(assertion);
    }

    /**
     * Checks the given assertion.
     * If it fails an AssertionError is thrown with the given message.
     * This method is equivalent to {@link #assertThat(String,Assertion)}.
     *
     * @see UISpecAssert#assertTrue(String,Assertion)
     */
    public void assertTrue(String message, Assertion assertion) {
        UISpecAssert.assertTrue(message, assertion);
    }

    /**
     * Checks the given assertion.
     * This method is equivalent to {@link #assertTrue(Assertion)}.
     *
     * @see UISpecAssert#assertThat(Assertion)
     */
    public void assertThat(Assertion assertion) {
        UISpecAssert.assertThat(assertion);
    }

    /**
     * Checks the given assertion.
     * If it fails an AssertionError is thrown with the given message.
     * This method is equivalent to {@link #assertTrue(String,Assertion)}.
     *
     * @see UISpecAssert#assertTrue(String,Assertion)
     */
    public void assertThat(String message, Assertion assertion) {
        UISpecAssert.assertThat(message, assertion);
    }

    /**
     * Waits for at most 'waitTimeLimit' ms until the assertion is true.
     *
     * @see UISpecAssert#waitUntil(Assertion, long)
     */
    public void waitUntil(Assertion assertion, long waitTimeLimit) {
        UISpecAssert.waitUntil(assertion, waitTimeLimit);
    }

    /**
     * Checks that the given assertion fails.
     *
     * @see UISpecAssert#assertFalse(Assertion)
     */
    public void assertFalse(Assertion assertion) {
        UISpecAssert.assertFalse(assertion);
    }

    /**
     * Waits for at most 'waitTimeLimit' ms until the assertion is true.
     * If it fails an AssertionError is thrown with the given message.
     *
     * @see UISpecAssert#waitUntil(String,Assertion,long)
     */
    public void waitUntil(String message, Assertion assertion, long waitTimeLimit) {
        UISpecAssert.waitUntil(message, assertion, waitTimeLimit);
    }

    /**
     * Checks that the given assertion fails.
     * If it succeeds an AssertionError is thrown with the given message.
     *
     * @see UISpecAssert#assertFalse(String,Assertion)
     */
    public void assertFalse(String message, Assertion assertion) {
        UISpecAssert.assertFalse(message, assertion);
    }

    /**
     * Returns a negation of the given assertion.
     *
     * @see UISpecAssert#not(Assertion)
     */
    public Assertion not(Assertion assertion) {
        return UISpecAssert.not(assertion);
    }

    /**
     * Returns the intersection of two assertions.
     *
     * @see UISpecAssert#and(Assertion[])
     */
    public Assertion and(Assertion... assertions) {
        return UISpecAssert.and(assertions);
    }

    /**
     * Returns the union of two assertions.
     *
     * @see UISpecAssert#or(Assertion[])
     */
    public Assertion or(Assertion... assertions) {
        return UISpecAssert.or(assertions);
    }

    /**
     * Checks that the given assertion equals the expected parameter.
     *
     * @see UISpecAssert#assertEquals(boolean,Assertion)
     */
    public void assertEquals(boolean expected, Assertion assertion) {
        UISpecAssert.assertEquals(expected, assertion);
    }

    /**
     * Checks that the given assertion equals the expected parameter.
     * If it fails an AssertionError is thrown with the given message.
     *
     * @see UISpecAssert#assertEquals(String,boolean,Assertion)
     */
    public void assertEquals(String message, boolean expected, Assertion assertion) {
        UISpecAssert.assertEquals(message, expected, assertion);
    }

    private UISpecAdapter getAdapter() throws AdapterNotFoundException {
        if (adapter == null) {
            retrieveAdapter();
        }
        return adapter;
    }

    static class AdapterNotFoundException extends RuntimeException {
        public AdapterNotFoundException() {
            super(PROPERTY_NOT_DEFINED);
        }

        public AdapterNotFoundException(String adapterClassName, Exception e) {
            super("Adapter class '" + adapterClassName + "' not found", e);
        }
    }
}