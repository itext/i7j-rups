package com.itextpdf.rups.model;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class SwingHelper {

    /**
     * If the current thread is an AWT dispatch thread then runnable will be invoked immediately.
     * Otherwise it will be executed asynchronously.
     *
     * @param runnable Runnable instance whose run() method is to be invoked
     */
    public static void invoke(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
}
