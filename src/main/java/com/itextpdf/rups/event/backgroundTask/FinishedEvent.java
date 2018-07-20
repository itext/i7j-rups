package com.itextpdf.rups.event.backgroundTask;

public class FinishedEvent extends BackgroundTaskEvent {
    @Override
    public int getType() {
        return FINISHED;
    }
}
