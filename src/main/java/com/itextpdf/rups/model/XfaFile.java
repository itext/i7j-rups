/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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
package com.itextpdf.rups.model;

import com.itextpdf.rups.io.OutputStreamResource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * Class that deals with the XFA file that can be inside a PDF file.
 */
public class XfaFile implements OutputStreamResource {

    /**
     * The X4J Document object (XML).
     */
    protected Document xfaDocument;

    /**
     * Constructs an XFA file from an OutputStreamResource.
     * This resource can be an XML file or a node in a RUPS application.
     *
     * @throws IOException       an I/O exception
     * @throws DocumentException a document exception
     * @param    resource    the XFA resource
     */
    public XfaFile(OutputStreamResource resource) throws IOException, DocumentException {
        // Is there a way to avoid loading everything in memory?
        // Can we somehow get the XML from the PDF as an InputSource, Reader or InputStream?
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resource.writeTo(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        // TODO DEVSIX-5299 refactor logic to use XML processing from com.itextpdf.kernel.utils.XmlUtils
        SAXReader reader = new SAXReader();
        reader.setEntityResolver(new SafeEmptyEntityResolver());
        xfaDocument = reader.read(bais);
    }

    /**
     * Getter for the XFA Document object.
     *
     * @return a Document object (X4J)
     */
    public Document getXfaDocument() {
        return xfaDocument;
    }

    /**
     * Writes a formatted XML file to the OutputStream.
     *
     * @see com.itextpdf.rups.io.OutputStreamResource#writeTo(java.io.OutputStream)
     */
    public void writeTo(OutputStream os) throws IOException {
        if (xfaDocument == null)
            return;
        OutputFormat format = new OutputFormat("   ", true);
        XMLWriter writer = new XMLWriter(os, format);
        writer.write(xfaDocument);
    }

    // Prevents XXE attacks
    private static class SafeEmptyEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new StringReader(""));
        }
    }

}
