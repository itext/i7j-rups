/*
    This file is part of the iText (R) project.
    Copyright (c) 2007-2018 iText Group NV
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
package com.itextpdf.rups.view.itext;

import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

public class PlainText extends JTextArea implements Observer {

    private PdfFile file;

    protected boolean loaded = false;

    private SwingWorker<String, Object> worker;

    public void update(Observable o, Object arg) {
        if (o instanceof PdfReaderController && arg instanceof RupsEvent) {
            RupsEvent event = (RupsEvent) arg;
            switch (event.getType()) {
                case RupsEvent.CLOSE_DOCUMENT_EVENT:
                    file = null;
                    setText("");
                    if (worker != null) {
                        worker.cancel(true);
                        worker = null;
                    }
                    loaded = false;
                    break;
                case RupsEvent.OPEN_DOCUMENT_POST_EVENT:
                    file = ((ObjectLoader) event.getContent()).getFile();
                    loaded = false;
                    if (worker != null) {
                        worker.cancel(true);
                        worker = null;
                    }
                    break;
                case RupsEvent.OPEN_PLAIN_TEXT_EVENT:
                    if (file == null || loaded) {
                        break;
                    }
                    loaded = true;
                    setText("Loading...");
                    worker = new SwingWorker<String, Object>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            return file.getRawContent();
                        }

                        @Override
                        protected void done() {
                            if (!isCancelled()) {
                                String text;
                                try {
                                    text = get();
                                } catch (InterruptedException | ExecutionException any) {
                                    text = "Error while loading text";
                                }
                                setText(text);
                            }
                        }
                    };
                    worker.execute();
                    break;
            }
        }
    }
}
