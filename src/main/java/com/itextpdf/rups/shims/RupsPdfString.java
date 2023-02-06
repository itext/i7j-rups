package com.itextpdf.rups.shims;

import com.itextpdf.kernel.pdf.PdfString;

public class RupsPdfString extends PdfString {
    public RupsPdfString(String value, String encoding) {
        super(value, encoding);
    }

    public RupsPdfString(String value) {
        super(value);
    }

    public RupsPdfString(byte[] content) {
        super(content);
    }

    public RupsPdfString(PdfString unpatchedPdfString){
        this(unpatchedPdfString.getValueBytes());
        this.setHexWriting(unpatchedPdfString.isHexWriting());
        this.encoding = unpatchedPdfString.getEncoding();
        this.directOnly = !unpatchedPdfString.isIndirect();
        this.indirectReference = unpatchedPdfString.getIndirectReference();
    }

    protected RupsPdfString(byte[] content, boolean hexWriting) {
        super(content, hexWriting);
    }

    @Override
    public String toString() {
        String wrapper;
        if(isHexWriting())
            wrapper = "<%s>";
        else
            wrapper = "(%s)";
        return String.format(wrapper, new String(encodeBytes(getValueBytes())));
    }
}
