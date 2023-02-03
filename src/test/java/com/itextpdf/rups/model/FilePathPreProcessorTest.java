package com.itextpdf.rups.model;

import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class FilePathPreProcessorTest extends ExtendedITextTest {

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


    @ParameterizedTest
    @MethodSource("provideUriArguments")
    public void testMultipleUriEncodedPaths(String path, String expected) {
        String actual = FilePathPreProcessor.process(path);
        assertEquals(expected, actual);
    }


    private static Stream<Arguments> provideUriArguments() {
        return Stream.of(
                Arguments.of("file:///home/user/file%20with%20space.txt", "/home/user/file with space.txt"),
                Arguments.of("file://C:/Users/user/file%20with%20space.txt", "C:/Users/user/file with space.txt"),
                // Latin-1 encoding
                Arguments.of("file:///home/user/file%20with%20%C3%A4%C3%B6%C3%BC%C3%9F.txt",
                        "/home/user/file with äöüß.txt"),
                // Latin variations of e
                Arguments.of("file:///home/user/file%20with%20%C3%A9%C3%AA%C3%AB%C3%A8%C3%89%C3%8A%C3%8B%C3%88.txt",
                        "/home/user/file with éêëèÉÊËÈ.txt")
        );
    }
}