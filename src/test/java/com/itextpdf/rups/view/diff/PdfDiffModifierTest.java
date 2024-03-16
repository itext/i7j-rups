package com.itextpdf.rups.view.diff;

import com.itextpdf.io.source.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DataFormatException;

public class PdfDiffModifierTest {

    @Test
    public void testSimple() throws IOException, DataFormatException {
        Path p = Path.of("src/main/resources/simpleParagraphTest.pdf");
        byte[] data = Files.readAllBytes(p);
        PdfDiffModifier tokenizer = new PdfDiffModifier(data);
        ByteArrayOutputStream baos = tokenizer.next();
        System.out.println(new String(baos.toByteArray(), StandardCharsets.US_ASCII));
    }

}