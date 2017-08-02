package com.itextpdf.rups.view;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;


public class StyleAppender extends OutputStreamAppender<ILoggingEvent> {

    private static final String DEFAULT_STYLE_TYPE = Console.ConsoleStyleContext.BACKUP;

    String styleType = DEFAULT_STYLE_TYPE;

    @Override
    public void start() {
        setOutputStream(new Console.ConsoleOutputStream(styleType));
        super.start();
    }

    public String getStyleType() {
        return styleType;
    }

    public void setStyleType(String styleType) {
        if (!styleType.equals(Console.ConsoleStyleContext.INFO) &&
                !styleType.equals(Console.ConsoleStyleContext.ERROR) &&
                !styleType.equals(Console.ConsoleStyleContext.BACKUP)) {
            addError("Wrong style is set for appender named [" + name + "]. Using default style");
            styleType = DEFAULT_STYLE_TYPE;
        }
        this.styleType = styleType;
    }
}
