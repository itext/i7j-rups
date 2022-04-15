package com.itextpdf.rups.model;

import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class BackgroundTaskTest {

    @Test
    public void normalScenario() throws InterruptedException {
        MockedBackgroundTask backgroundTask = new MockedBackgroundTask();
        backgroundTask.start();
        backgroundTask.join();
        Assert.assertTrue(backgroundTask.hasTaskExecuted());
    }
}
