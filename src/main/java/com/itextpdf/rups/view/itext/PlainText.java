package com.itextpdf.rups.view.itext;

import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfFile;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;

public class PlainText extends JTextArea implements Observer {

    private PdfFile file;

    protected boolean loaded = false;

    private SwingWorker<String, Object> worker;

    public void update(Observable o, Object arg) {
        if (arg == null) {
            file = null;
            setText("");
            if (worker != null) {
                worker.cancel(true);
                worker = null;
            }
            loaded = false;
        } else if (arg instanceof ObjectLoader) {
            file = ((ObjectLoader)arg).getFile();
            loaded = false;
            if (worker != null) {
                worker.cancel(true);
                worker = null;
            }
        } else if (arg instanceof ChangeEvent && file != null && !loaded) {
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
        }
    }
}
