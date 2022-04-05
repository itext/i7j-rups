package com.itextpdf.rups.mock;

import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.rups.model.PdfFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MockedPdfFile extends PdfFile {
    /**
     * Constructs a PdfFile object.
     *
     * @param file     the byte[] to read
     * @param readOnly read only
     *
     * @throws IOException  an I/O exception
     * @throws PdfException a PDF exception
     */
    public MockedPdfFile(byte[] file, boolean readOnly) throws IOException, PdfException {
        super(file, readOnly);
    }

    @Override
    protected void readFile(InputStream fis, boolean checkPass, boolean readOnly) throws IOException, PdfException {
        // empty on purpose
    }

    @Override
    public String getFilename() {
        return "mock.pdf";
    }

    @Override
    public File getDirectory() {
        return new File("");
    }
}

