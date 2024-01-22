/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.rups.shims;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfTextArray;
import com.itextpdf.kernel.utils.ICopyFilter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class RupsPdfTextArray extends PdfTextArray {
    PdfTextArray original;
    public RupsPdfTextArray(PdfTextArray pdfTextArray) {
        super();
        original = pdfTextArray;
    }

    @Override
    public void add(PdfObject pdfObject) {
        original.add(pdfObject);
    }

    @Override
    public void addAll(PdfArray a) {
        original.addAll(a);
    }

    @Override
    public void addAll(Collection<PdfObject> c) {
        original.addAll(c);
    }

    @Override
    public boolean add(float number) {
        return original.add(number);
    }

    @Override
    public boolean add(String text, PdfFont font) {
        return original.add(text, font);
    }

    @Override
    public boolean add(byte[] text) {
        return original.add(text);
    }

    @Override
    protected boolean add(String text) {
        return add(text.getBytes());
    }

    @Override
    public int size() {
        return original.size();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public boolean contains(PdfObject o) {
        return original.contains(o);
    }

    @Override
    public Iterator<PdfObject> iterator() {
        return original.iterator();
    }

    @Override
    public void add(int index, PdfObject element) {
        original.add(index, element);
    }

    @Override
    public PdfObject set(int index, PdfObject element) {
        return original.set(index, element);
    }

    @Override
    public PdfObject get(int index) {
        return original.get(index);
    }

    @Override
    public void remove(int index) {
        original.remove(index);
    }

    @Override
    public void remove(PdfObject o) {
        original.remove(o);
    }

    @Override
    public void clear() {
        original.clear();
    }

    @Override
    public int indexOf(PdfObject o) {
        return original.indexOf(o);
    }

    @Override
    public List<PdfObject> subList(int fromIndex, int toIndex) {
        return original.subList(fromIndex, toIndex);
    }

    @Override
    public byte getType() {
        return original.getType();
    }

    @Override
    public String toString() {
        return RupsPdfArray.getShimmedString(original);
    }

    @Override
    public PdfObject get(int index, boolean asDirect) {
        return original.get(index, asDirect);
    }

    @Override
    public PdfArray getAsArray(int index) {
        return original.getAsArray(index);
    }

    @Override
    public PdfDictionary getAsDictionary(int index) {
        return original.getAsDictionary(index);
    }

    @Override
    public PdfStream getAsStream(int index) {
        return original.getAsStream(index);
    }

    @Override
    public PdfNumber getAsNumber(int index) {
        return original.getAsNumber(index);
    }

    @Override
    public PdfName getAsName(int index) {
        return original.getAsName(index);
    }

    @Override
    public PdfString getAsString(int index) {
        return original.getAsString(index);
    }

    @Override
    public PdfBoolean getAsBoolean(int index) {
        return original.getAsBoolean(index);
    }

    @Override
    public Rectangle toRectangle() {
        return original.toRectangle();
    }

    @Override
    public float[] toFloatArray() {
        return original.toFloatArray();
    }

    @Override
    public double[] toDoubleArray() {
        return original.toDoubleArray();
    }

    @Override
    public long[] toLongArray() {
        return original.toLongArray();
    }

    @Override
    public int[] toIntArray() {
        return original.toIntArray();
    }

    @Override
    public boolean[] toBooleanArray() {
        return original.toBooleanArray();
    }

    @Override
    public PdfIndirectReference getIndirectReference() {
        return original.getIndirectReference();
    }

    @Override
    public boolean isIndirect() {
        return original.isIndirect();
    }

    @Override
    public PdfObject makeIndirect(PdfDocument document, PdfIndirectReference reference) {
        return original.makeIndirect(document, reference);
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
        original.release();
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

    @Override
    public void forEach(Consumer<? super PdfObject> action) {
        original.forEach(action);
    }

    @Override
    public Spliterator<PdfObject> spliterator() {
        return original.spliterator();
    }

    @Override
    public int hashCode() {
        return original.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return original.equals(obj);
    }
}
