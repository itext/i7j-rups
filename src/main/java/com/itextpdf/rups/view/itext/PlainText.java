package com.itextpdf.rups.view.itext;

import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;

import java.util.Observable;
import java.util.Observer;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

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
                    file = ((ObjectLoader)event.getContent()).getFile();
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
                                } catch (Exception any) {
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
