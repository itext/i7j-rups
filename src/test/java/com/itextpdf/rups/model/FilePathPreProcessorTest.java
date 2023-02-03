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