/*
 * $Id$
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 2007-2015 iText Group NV
 * Authors: Bruno Lowagie et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.rups.model;

import javax.swing.*;

/**
 * Allows you to perform long lasting tasks in background.
 * If we ever move to Java 6, we should use the SwingWorker class
 * (included in the JDK) instead of this custom Event Dispatching
 * code.
 */
public abstract class BackgroundTask {

    /**
     * Inner class that holds the reference to the thread.
     */
    private static class ThreadWrapper {
        private Thread thread;
        ThreadWrapper(Thread t) { thread = t; }
        synchronized Thread get() { return thread; }
        synchronized void clear() { thread = null; }
    }

    /** A wrapper for the tread that executes a time-consuming task. */
    private ThreadWrapper thread;

    /**
     * Starts a thread.
     * Executes the time-consuming task in the construct method;
     * finally calls the finish().
     */
    public BackgroundTask() {
        final Runnable doFinished = new Runnable() {
            public void run() {
                finished();
            }
        };

        Runnable doConstruct = new Runnable() {
            public void run() {
                try {
                    doTask();
                } finally {
                    thread.clear();
                }
                SwingUtilities.invokeLater(doFinished);
            }
        };
        Thread t = new Thread(doConstruct);
        thread = new ThreadWrapper(t);
    }

    /**
     * Implement this class; the time-consuming task will go here.
     */
    public abstract void doTask();

    /**
     * Starts the thread.
     */
    public void start() {
        Thread t = thread.get();
        if (t != null) {
            t.start();
        }
    }

    /**
     * Wait for thread to finish what it's doing
     *
     * @throws InterruptedException an exception
     */
    public void join() throws InterruptedException {
        Thread t = thread.get();
        if (t != null) {
            t.join();
        }
    }

    /**
     * Forces the thread to stop what it's doing.
     */
    public void interrupt() {
        Thread t = thread.get();
        if (t != null) {
            t.interrupt();
        }
        thread.clear();
    }

    /**
     * Called on the event dispatching thread once the
     * construct method has finished its task.
     */
    public void finished() {
    }
}
