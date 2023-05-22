package com.itextpdf.rups.shims;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
@Tag("Array")
public class RupsPdfArrayTest {

    @Test
    public void BasicNameArrayTest(){
        PdfArray initialArray = new PdfArray();

        PdfName initialValueOne = new PdfName("ValueOne");
        PdfName initialValueTwo = new PdfName("ValueTwo");
        initialArray.add(initialValueOne);
        initialArray.add(initialValueTwo);

        RupsPdfArray pdfArray = new RupsPdfArray(initialArray);

        Assertions.assertEquals("[/ValueOne/ValueTwo]", pdfArray.toString(), "PDF Array fails to Serialize correctly.");


    }
    @Test
    public void BasicNestedNameArrayTest(){
        PdfArray initialArray = new PdfArray();

        PdfArray subArrayOne = new PdfArray();
        PdfArray subArrayTwo = new PdfArray();

        PdfName initialValueOne = new PdfName("ValueOne");
        PdfName initialValueTwo = new PdfName("ValueTwo");

        subArrayOne.add(initialValueOne);
        subArrayOne.add(initialValueTwo);

        subArrayTwo.add(initialValueOne);
        subArrayTwo.add(initialValueTwo);

        initialArray.add(subArrayOne);
        initialArray.add(subArrayTwo);

        RupsPdfArray pdfArray = new RupsPdfArray(initialArray);

        Assertions.assertEquals("[[/ValueOne/ValueTwo][/ValueOne/ValueTwo]]", pdfArray.toString(), "PDF Array fails to Serialize correctly.");


    }
}
