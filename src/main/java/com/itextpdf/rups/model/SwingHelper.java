package com.itextpdf.rups.model;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
