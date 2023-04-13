/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.rups.view.itext.contentstream;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfResources;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.canvas.CanvasTag;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.IXObjectDoHandler;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Utility class to extract text from and to index marked content
 * in content streams. Uses {@link PdfCanvasProcessor} internally.
 *
 * @see MarkedContentInfo
 */
public final class MarkedContentInfoGatherer {

    private final TextListener textListener;
    private final StreamTrackingPdfCanvasProcessor canvasProc;

    public MarkedContentInfoGatherer() {
        this.textListener = new TextListener();
        this.canvasProc = new StreamTrackingPdfCanvasProcessor(this.textListener);
        this.textListener.markedContentIndex = new HashMap<>();
    }

    public Map<Integer, MarkedContentInfo> getMarkedContentIndex() {
        return Collections.unmodifiableMap(this.textListener.markedContentIndex);
    }

    /**
     * Process an entire page's content stream(s).
     *
     * @param page page to process
     */
    public void processPageContent(PdfPage page) {
        // we go one content stream at a time to keep track of currentStreamRef
        final PdfResources resources = page.getResources();
        int streamCount = page.getContentStreamCount();
        for (int i = 0; i < streamCount; i++) {
            final PdfStream stm = page.getContentStream(i);
            processContentStream(stm, resources);
        }
    }

    /**
     * Process a single content stream, possibly as part of a larger whole.
     * NOTE: this explicitly relies on the state-preserving nature of {@link PdfCanvasProcessor#processContent}.
     * @param stream content stream
     */
    void processContentStream(PdfStream stream, PdfResources resources) {
        this.canvasProc.streamRefs.clear();
        this.canvasProc.streamRefs.push(stream.getIndirectReference());
        byte[] streamBytes = stream.getBytes();
        this.canvasProc.processContent(streamBytes, resources);
    }

    /**
     * Reset the state of the underlying {@link PdfCanvasProcessor}.
     * Must be called between processing of distinct pages.
     */
    public void reset() {
        this.canvasProc.reset();
        this.textListener.markedContentIndex = new HashMap<>();
        this.canvasProc.streamRefs.clear();
    }

    private final class TextListener implements IEventListener {
        private Map<Integer, MarkedContentInfo> markedContentIndex = null;

        TextListener() {
            // nothing to do
        }

        @Override
        public Set<EventType> getSupportedEvents() {
            return Collections.singleton(EventType.RENDER_TEXT);
        }

        @Override
        public void eventOccurred(IEventData data, EventType type) {
            TextRenderInfo renderInfo = (TextRenderInfo) data;
            int mcid = renderInfo.getMcid();
            if (mcid == -1) {
                return;
            }

            final MarkedContentInfo mci = markedContentIndex.computeIfAbsent(
                    mcid, k -> new MarkedContentInfo(k, canvasProc.streamRefs.peek()));
            String actualText = renderInfo.getActualText();
            mci.appendExtractedText(actualText == null ? renderInfo.getText() : actualText);
        }
    }

    private static final class StreamTrackingPdfCanvasProcessor extends PdfCanvasProcessor {
        private final Stack<PdfIndirectReference> streamRefs = new Stack<>();

        StreamTrackingPdfCanvasProcessor(TextListener eventListener) {
            super(eventListener);
        }

        @Override
        public PdfResources getResources() {
            // overridden for visibility reasons
            return super.getResources();
        }

        @Override
        protected void populateXObjectDoHandlers() {
            super.populateXObjectDoHandlers();
            registerXObjectDoHandler(PdfName.Form, new XObjTracker());
        }
    }

    private static final class XObjTracker implements IXObjectDoHandler {

        XObjTracker() {
            // nothing to do
        }

        @Override
        public void handleXObject(PdfCanvasProcessor processor,
                Stack<CanvasTag> canvasTagHierarchy, PdfStream stream, PdfName xObjectName) {
            final StreamTrackingPdfCanvasProcessor trackingProc =
                    (StreamTrackingPdfCanvasProcessor) processor;
            final PdfDictionary resourcesDic = stream.getAsDictionary(PdfName.Resources);
            final PdfResources resources = resourcesDic == null
                    ? trackingProc.getResources() : new PdfResources(resourcesDic);

            // we don't care about the graphics state here, and the push/pop logic
            // in PdfCanvasProcessor is private anyhow, so let's just ignore all that
            trackingProc.streamRefs.push(stream.getIndirectReference());
            processor.processContent(stream.getBytes(), resources);
            trackingProc.streamRefs.pop();
        }
    }
}
