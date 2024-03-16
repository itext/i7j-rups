package com.itextpdf.rups.view.diff;

import be.ysebie.diff.lib.DiffStrategy;
import com.itextpdf.io.source.ByteArrayOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;

public class DiffGenerationStrategy implements DiffStrategy<ByteBuffer> {

    private final ViewerOptions options;

    private ByteBuffer[] tokenizedDataA;
    private ByteBuffer[] tokenizedDataB;

    DiffGenerationStrategy(ViewerOptions options) throws IOException, DataFormatException {
        this.options = options;
        setupDiffData();
    }

    private void setupDiffData() throws IOException, DataFormatException {
        if (options.fileA == null || options.fileB == null) {
            throw new IOException("Files not set");
        }
        tokenizedDataA = generateData(parsePdfDocument(Files.readAllBytes(options.fileA.toPath())));
        tokenizedDataB = generateData(parsePdfDocument(Files.readAllBytes(options.fileB.toPath())));
    }

    @Override
    public ByteBuffer[] getDiffDataA() {
        return tokenizedDataA;
    }

    @Override
    public ByteBuffer[] getDiffDataB() {
        return tokenizedDataB;
    }

    private ByteBuffer[] generateData(byte[] dataA) {
        // if the byte is a new line, split the array
        int start = 0;
        int end = 0;
        List<ByteBuffer> result = new ArrayList<>();
        for (int i = 0; i < dataA.length; i++) {
            if (dataA[i] == '\n') {
                end = i;
                byte[] tmp = Arrays.copyOfRange(dataA, start, end);
                start = end + 1;
                result.add(ByteBuffer.wrap(tmp));
            }
        }
        if (start < dataA.length) {
            result.add(ByteBuffer.wrap(Arrays.copyOfRange(dataA, start, dataA.length)));
        }
        return result.toArray(new ByteBuffer[0]);
    }

    private byte[] parsePdfDocument(byte[] data) throws IOException, DataFormatException {
        if (!options.decompress) {
            return data;
        }
        PdfDiffModifier diffViewerTokenizer = new PdfDiffModifier(data);
        ByteArrayOutputStream baos = diffViewerTokenizer.next();
        return baos.toByteArray();
    }
}

