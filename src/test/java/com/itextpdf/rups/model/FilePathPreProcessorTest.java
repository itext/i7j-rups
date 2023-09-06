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
package com.itextpdf.rups.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class FilePathPreProcessorTest {

    @Test
    public void process() {
        String path = "file://";
        String expected = "";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }

    @Test
    public void processLinuxRootFolder() {
        String path = "file:///";
        String expected = "/";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }
    @Test
    public void processWindowsRootFolder() {
        String path = "file://C:/";
        String expected = "C:/";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }

    @Test
    public void processNormalFile() {
        String path = "file:///home/user/file.txt";
        String expected = "/home/user/file.txt";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }
    @Test
    public void processNormalFileWindows() {
        String path = "file://C:/Users/user/file.txt";
        String expected = "C:/Users/user/file.txt";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }

    @Test
    public void processNull() {
        String path = null;
        String expected = null;
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }

    @Test
    public void processEmpty() {
        String path = "";
        String expected = "";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }

    @Test
    public void jpanelBehaviour() {
        String path = "/home/user/file:/home/user/file.txt";
        String expected = "/home/user/file.txt";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }

    @Test
    public void jpanelBehaviourWindows() {
        String path = "C:/Users/user/file:C:/Users/user/file.txt";
        String expected = "C:/Users/user/file.txt";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }

    @Test
    public void pathContainsSpaceUrlEncodingLinux() {
        String path = "file:///home/user/file%20with%20space.txt";
        String expected = "/home/user/file with space.txt";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }

    @Test
    public void pathContainsSpaceUrlEncodingWindows() {
        String path = "file://C:/Users/user/file%20with%20space.txt";
        String expected = "C:/Users/user/file with space.txt";
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }
}