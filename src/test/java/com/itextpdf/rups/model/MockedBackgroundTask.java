package com.itextpdf.rups.model;

public class MockedBackgroundTask extends BackgroundTask {

    private boolean taskExecuted;

    public MockedBackgroundTask() {
        this.taskExecuted = false;
    }

    @Override public void doTask() {
        taskExecuted = true;
    }

    @Override public void finished() {
    }

    public boolean hasTaskExecuted() {
        return taskExecuted;
    }
}
