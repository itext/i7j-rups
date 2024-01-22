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
import com.itextpdf.kernel.utils.ICopyFilter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RupsPdfDictionary extends PdfDictionary {
    PdfDictionary original;
    public RupsPdfDictionary(PdfDictionary dictionary) {
        super(dictionary);
        original = dictionary;
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
    public boolean containsKey(PdfName key) {
        return original.containsKey(key);
    }

    @Override
    public boolean containsValue(PdfObject value) {
        return original.containsValue(value);
    }

    @Override
    public PdfObject get(PdfName key) {
        return original.get(key);
    }

    @Override
    public PdfArray getAsArray(PdfName key) {
        return original.getAsArray(key);
    }

    @Override
    public PdfDictionary getAsDictionary(PdfName key) {
        return original.getAsDictionary(key);
    }

    @Override
    public PdfStream getAsStream(PdfName key) {
        return original.getAsStream(key);
    }

    @Override
    public PdfNumber getAsNumber(PdfName key) {
        return original.getAsNumber(key);
    }

    @Override
    public PdfName getAsName(PdfName key) {
        return original.getAsName(key);
    }

    @Override
    public PdfString getAsString(PdfName key) {
        return original.getAsString(key);
    }

    @Override
    public PdfBoolean getAsBoolean(PdfName key) {
        return original.getAsBoolean(key);
    }

    @Override
    public Rectangle getAsRectangle(PdfName key) {
        return original.getAsRectangle(key);
    }

    @Override
    public Float getAsFloat(PdfName key) {
        return original.getAsFloat(key);
    }

    @Override
    public Integer getAsInt(PdfName key) {
        return original.getAsInt(key);
    }

    @Override
    public Boolean getAsBool(PdfName key) {
        return original.getAsBool(key);
    }

    @Override
    public PdfObject put(PdfName key, PdfObject value) {
        return original.put(key, value);
    }

    @Override
    public PdfObject remove(PdfName key) {
        return original.remove(key);
    }

    @Override
    public void putAll(PdfDictionary d) {
        original.putAll(d);
    }

    @Override
    public void clear() {
        original.clear();
    }

    @Override
    public Set<PdfName> keySet() {
        return original.keySet();
    }

    @Override
    public Collection<PdfObject> values(boolean asDirects) {
        return original.values(asDirects);
    }

    @Override
    public Collection<PdfObject> values() {
        return original.values();
    }

    @Override
    public Set<Map.Entry<PdfName, PdfObject>> entrySet() {
        return original.entrySet();
    }

    @Override
    public byte getType() {
        return original.getType();
    }

    @Override
    public String toString() {
        if (!isFlushed()) {
            StringBuilder string = new StringBuilder();
            string.append("<<");
            for (Map.Entry<PdfName, PdfObject> entry : original.entrySet()) {
                string.append(entry.getKey().toString());
                string.append(" ");
                string.append(RupsPdfArray.GetEntryString(entry.getValue()));
            }
            string.append(">>");
            return string.toString();
        }
        return original.getIndirectReference().toString();

    }

    @Override
    public PdfDictionary clone(List<PdfName> excludeKeys) {
        return original.clone(excludeKeys);
    }

    @Override
    public PdfDictionary copyTo(PdfDocument document, List<PdfName> excludeKeys, boolean allowDuplicating) {
        return original.copyTo(document, excludeKeys, allowDuplicating);
    }

    @Override
    public PdfDictionary copyTo(PdfDocument document, List<PdfName> excludeKeys, boolean allowDuplicating, ICopyFilter copyFilter) {
        return original.copyTo(document, excludeKeys, allowDuplicating, copyFilter);
    }

    @Override
    public PdfObject get(PdfName key, boolean asDirect) {
        return original.get(key, asDirect);
    }

    @Override
    public void mergeDifferent(PdfDictionary other) {
        original.mergeDifferent(other);
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

//    @Override
//    protected PdfObject setIndirectReference(PdfIndirectReference indirectReference) {
//        return original.setIndirectReference(indirectReference);
//    }

    @Override
    public boolean isLiteral() {
        return original.isLiteral();
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
