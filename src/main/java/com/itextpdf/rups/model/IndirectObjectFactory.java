/*
 * $Id$
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 2007-2015 iText Group NV
 * Authors: Bruno Lowagie et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.rups.model;

import com.itextpdf.io.util.IntHashtable;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory that can produce all the indirect objects in a PDF file.
 */
public class IndirectObjectFactory {

	/** The reader object. */
	protected PdfDocument document;
	/** The current xref number. */
	protected int current;
	/** The highest xref number. */
	protected int n;
	/** A list of all the indirect objects in a PDF file. */
	protected ArrayList<PdfObject> objects = new ArrayList<PdfObject>();
	/** Mapping between the index in the objects list and the reference number in the xref table.  */
	protected IntHashtable idxToRef = new IntHashtable();
	/** Mapping between the reference number in the xref table and the index in the objects list .  */
	protected IntHashtable refToIdx = new IntHashtable();
	/** Array to indicate if object is already loaded */
	protected ArrayList<Boolean> isLoaded = new ArrayList<Boolean>();

	private static final String METHOD_NAME = "checkState";
	private static final String FIELD_NAME = "FORBID_RELEASE";
	private static Method checkStateMethod;
	private static Field forbidReleaseField;

	static {
		try {
			checkStateMethod = PdfObject.class.getDeclaredMethod(METHOD_NAME, short.class);
			checkStateMethod.setAccessible(true);
			forbidReleaseField = PdfObject.class.getDeclaredField(FIELD_NAME);
			forbidReleaseField.setAccessible(true);
		} catch (Exception ignored) {
		}
	}
	
	/**
	 * Creates a list that will contain all the indirect objects
	 * in a PDF document. 
	 * @param document	the PDF document
	 */
	public IndirectObjectFactory(PdfDocument document) {
		this.document = document;
		current = -1;
		n = document.getNumberOfPdfObjects();
	}

	/**
	 * Gets the last object that has been registered.
	 * This method only makes sense while loading the factory.
	 * with loadNextObject().
	 * @return	the number of the last object that was stored
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * Gets the highest possible object number in the XRef table.
	 * @return	an object number
	 */
	public int getXRefMaximum() {
		return n;
	}

	/**
	 * Stores the next object of the XRef table.
	 * As soon as this method returns false, it makes no longer
	 * sense calling it as all the objects have been stored.
	 * @return	false if there are no objects left to check.
	 */
	public boolean storeNextObject() {
		while (current < n) {
			current++;
			PdfObject object = document.getPdfObject(current);

			if (object != null) {
				int idx = size();
				idxToRef.put(idx, current);
				refToIdx.put(current, idx);
				store(object);
				return true;
			}
		}
		return false;

	}
	
	/**
	 * If we store all the objects, we might run out of memory;
	 * that's why we'll only store the objects that are necessary
	 * to construct other objects (for instance the page table).
	 * @param	object	an object we might want to store 
	 */
	private void store(PdfObject object) {
		if (object.isDictionary()){
			PdfDictionary dict = (PdfDictionary)object;
			if (PdfName.Page.equals(dict.get(PdfName.Type, false))) {
				objects.add(dict);
				isLoaded.add(true);
				return;
			}
		}
		if (object.isNull()) {
			isLoaded.add(true);
		} else {
			isLoaded.add(false);
		}
		if (canRelease(object)) {
			object.release();
			objects.add(PdfNull.PDF_NULL);
		} else {
			objects.add(object);
		}
	}
	
	/**
	 * Gets the total number of indirect objects in the PDF file.
	 * This isn't necessarily the same number as returned by getXRefMaximum().
	 * The PDF specification allows gaps between object numbers.
	 * @return the total number of indirect objects in the PDF.
	 */
	public int size() {
		return objects.size();
	}
	
	/**
	 * Gets the index of an object based on its number in the xref table.
	 * @param ref	a number in the xref table
	 * @return	the index in the list of indirect objects
	 */
	public int getIndexByRef(int ref) {
		return refToIdx.get(ref);
	}
	
	/**
	 * Gets the reference number in the xref table based on the index in the
	 * indirect object list.
	 * @param i		the index of an object in the indirect object list
	 * @return	the corresponding reference number in the xref table
	 */
	public int getRefByIndex(int i) {
		return idxToRef.get(i);
	}
	
	/**
	 * Gets an object based on its index in the indirect object list.
	 * @param i		an index in the indirect object list	
	 * @return	a PDF object
	 */
	public PdfObject getObjectByIndex(int i) {
		return getObjectByReference(getRefByIndex(i));
	}

	/**
	 * Gets an object based on its reference number in the xref table.
	 * @param ref	a number in the xref table
	 * @return	a PDF object
	 */
	public PdfObject getObjectByReference(int ref) {
		return objects.get(getIndexByRef(ref));
	}

	public boolean isLoadedByIndex(int i) {
		return isLoaded.get(i);
	}

	public boolean isLoadedByReference(int ref) {
		return isLoaded.get(getIndexByRef(ref));
	}


	/**
	 * Loads an object based on its reference number in the xref table.
	 * @param ref	a reference number in the xref table.
	 * @return	a PDF object
	 */
	public PdfObject loadObjectByReference(int ref) {
		PdfObject object = getObjectByReference(ref);
		int idx = getIndexByRef(ref);
		if (object instanceof PdfNull && !isLoaded.get(idx)) {
			object = document.getPdfObject(ref);
			objects.set(idx, object);
			isLoaded.set(idx, true);
		}
		return object;
	}

	private boolean canRelease(PdfObject obj) {
		try {
			return !(Boolean)checkStateMethod.invoke(obj, forbidReleaseField.get(obj));
		} catch (Exception any) {
			return true;
		}
	}

	void addNewIndirectObject(PdfObject object) {
        object.makeIndirect(document);
        ++n;
        int idx = size();
        idxToRef.put(idx, object.getIndirectReference().getObjNumber());
        refToIdx.put(object.getIndirectReference().getObjNumber(), idx);
        objects.add(object);
        LoggerHelper.info("New indirect object was successfully created. Its object number is: " + object.getIndirectReference().getObjNumber(), getClass());
    }
}
