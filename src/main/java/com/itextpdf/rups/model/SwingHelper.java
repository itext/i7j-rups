package com.itextpdf.rups.model;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class SwingHelper {

    public static void invokeSync(Runnable runnable) {
        invokeSync(runnable, false);
    }

    public static void invokeSync(Runnable runnable, boolean isSilent) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException e) {
                if (!isSilent) {
                    LoggerHelper.warn(LoggerMessages.INVOKING_RUNNABLE_ERROR, e, SwingHelper.class);
                }
            } catch (InvocationTargetException e) {
                if (!isSilent) {
                    LoggerHelper.warn(LoggerMessages.RUNNABLE_CAUSE_EXCEPTION, e, SwingHelper.class);
                }
            }
        }
    }
}
