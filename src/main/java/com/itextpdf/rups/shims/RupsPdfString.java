package com.itextpdf.rups.shims;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.utils.ICopyFilter;

public class RupsPdfString extends PdfString {

    PdfString original;
    public RupsPdfString(String value, String encoding) {
        super(value, encoding);
    }

    public RupsPdfString(PdfString original){
        super(original.getValueBytes());
        this.original = original;
    }


    @Override
    public byte[] getValueBytes(){
        return original.getValueBytes();
    }
    @Override
            public boolean isHexWriting(){
        return original.isHexWriting();
    }
    @Override
    public String getEncoding(){
        return original.getEncoding();
    }
    @Override
            public boolean isIndirect(){
        return original.isIndirect();
    }

    @Override
    public byte getType() {
        return original.getType();
    }

    @Override
    public PdfString setHexWriting(boolean hexWriting) {
        return original.setHexWriting(hexWriting);
    }

    @Override
    public String getValue() {
        return original.getValue();
    }

    @Override
    public String toUnicodeString() {
        return original.toUnicodeString();
    }

    @Override
    public int hashCode() {
        return original.hashCode();
    }

    @Override
    public void markAsUnencryptedObject() {
        original.markAsUnencryptedObject();
    }

//    @Override
//    protected void generateValue() {
//        super.generateValue();
//    }

//    @Override
//    protected void generateContent() {
//        super.generateContent();
//    }

//    @Override
//    protected boolean encrypt(PdfEncryption encrypt) {
//        return original.encrypt(encrypt);
//    }

//    @Override
//    protected byte[] decodeContent() {
//        return original.decodeContent();
//    }

//    @Override
//    protected byte[] encodeBytes(byte[] bytes) {
//        return original.encodeBytes(bytes);
//    }

//    @Override
//    protected PdfObject newInstance() {
//        return original.clone();
//    }

//    @Override
//    protected void copyContent(PdfObject from, PdfDocument document, ICopyFilter copyFilter) {
//        super.copyContent(from, document, copyFilter);
//    }

//    @Override
//    protected boolean hasContent() {
//        return original.hasContent();
//    }

    @Override
    public PdfObject makeIndirect(PdfDocument document, PdfIndirectReference reference) {
        return original.makeIndirect(document, reference);
    }

    @Override
    public PdfObject setIndirectReference(PdfIndirectReference indirectReference) {
        return original.setIndirectReference(indirectReference);
    }

//    @Override
//    protected int compareContent(PdfPrimitiveObject o) {
//        return original.compareContent(o);
//    }

    @Override
    public PdfIndirectReference getIndirectReference() {
        return original.getIndirectReference();
    }

    @Override
    public PdfObject makeIndirect(PdfDocument document) {
        return original.makeIndirect(document);
    }

    @Override
    public boolean isFlushed() {
        return original.isFlushed();
    }

    @Override
    public boolean isModified() {
        return original.isModified();
    }

    @Override
    public PdfObject clone() {
        return original.clone();
    }

    @Override
    public PdfObject clone(ICopyFilter filter) {
        return original.clone(filter);
    }

    @Override
    public PdfObject copyTo(PdfDocument document) {
        return original.copyTo(document);
    }

    @Override
    public PdfObject copyTo(PdfDocument document, boolean allowDuplicating) {
        return original.copyTo(document, allowDuplicating);
    }

    @Override
    public PdfObject copyTo(PdfDocument document, ICopyFilter copyFilter) {
        return original.copyTo(document, copyFilter);
    }

    @Override
    public PdfObject copyTo(PdfDocument document, boolean allowDuplicating, ICopyFilter copyFilter) {
        return original.copyTo(document, allowDuplicating, copyFilter);
    }

    @Override
    public PdfObject setModified() {
        return original.setModified();
    }

    @Override
    public boolean isReleaseForbidden() {
        return original.isReleaseForbidden();
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    public boolean isNull() {
        return original.isNull();
    }

    @Override
    public boolean isBoolean() {
        return original.isBoolean();
    }

    @Override
    public boolean isNumber() {
        return original.isNumber();
    }

    @Override
    public boolean isString() {
        return original.isString();
    }

    @Override
    public boolean isName() {
        return original.isName();
    }

    @Override
    public boolean isArray() {
        return original.isArray();
    }

    @Override
    public boolean isDictionary() {
        return original.isDictionary();
    }

    @Override
    public boolean isStream() {
        return original.isStream();
    }

    @Override
    public boolean isIndirectReference() {
        return original.isIndirectReference();
    }

    @Override
    public boolean isLiteral() {
        return original.isLiteral();
    }

//    @Override
//    protected boolean checkState(short state) {
//        return original.checkState(state);
//    }

//    @Override
//    protected PdfObject setState(short state) {
//        return original.setState(state);
//    }

//    @Override
//    protected PdfObject clearState(short state) {
//        return original.clearState(state);
//    }

    @Override
    protected void copyContent(PdfObject from, PdfDocument document) {
        super.copyContent(from, document);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof PdfString) {
            return original.equals(o);
        } else {
            return false;
        }
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
