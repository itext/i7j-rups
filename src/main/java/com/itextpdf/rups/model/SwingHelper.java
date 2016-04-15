package com.itextpdf.rups.model;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

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
                    e.printStackTrace();
                }
            } catch (InvocationTargetException e) {
                if (!isSilent) {
                    e.printStackTrace();
                }
            }
        }
    }
}
