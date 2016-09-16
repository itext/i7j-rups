package com.itextpdf.rups.view;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;

public class DebugAppender extends OutputStreamAppender<ILoggingEvent> {

    @Override
    public void start() {
        setOutputStream(new DebugView.DebugOutputStream());
        super.start();
    }
}
