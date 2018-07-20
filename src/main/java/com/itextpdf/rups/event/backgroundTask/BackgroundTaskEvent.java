package com.itextpdf.rups.event.backgroundTask;

public abstract class BackgroundTaskEvent {
    public static final byte FINISHED = 0;

    public abstract int getType();
}
