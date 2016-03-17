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
package com.itextpdf.rups;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.kernel.Version;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * iText RUPS is a tool that allows you to inspect the internal structure
 * of a PDF file.
 */
public class Rups {

    /**
     * Allows people to plug in RUPS into their projects without RUPS shutting down the JVM.
      */
    public static int CLOSE_OPERATION = WindowConstants.EXIT_ON_CLOSE;

    static RupsController cont;
	// main method
	/**
	 * Main method. Starts the RUPS application.
	 * @param	args	no arguments needed
	 */
	public static void main(String[] args) {
		final File f;
		if (args.length > 0) {
            String pathToFile = args[0];
            f = new File(pathToFile);
        } else {
            f = null;
        }
		SwingUtilities.invokeLater(
		        new Runnable(){
		            public void run() {
		                cont = startApplication(f, CLOSE_OPERATION);
		            }
		        }
		        );
        /*SwingUtilities.invokeLater(
                new Runnable(){
                    public void run() {
                        cont.loadFile(f);
                    }
                }
        );
        /*new Thread() {
            @Override
            public void start() {
                startApplication(f, CLOSE_OPERATION);
            }
        }.start();
        /*new Runnable(){
            public void run() {
                startApplication(f, CLOSE_OPERATION);
            }
        };*/
	}

	// methods

    /**
     * Initializes the main components of the Rups application.
     * @param f a file that should be opened on launch
     */
    public static RupsController startApplication(File f, final int onCloseOperation) {
        JFrame frame = new JFrame();
        initFrameDim(frame);
        RupsController controller = new RupsController(frame.getSize());
        initApplication(frame, controller, onCloseOperation);
		if (null != f && f.canRead()) {
			controller.loadFile(f);
		}
        return controller;
    }

    protected static void initApplication(JFrame frame, RupsController controller, final int onCloseOperation) {
        // title bar
        frame.setTitle("iText RUPS " + Version.getInstance().getVersion());
        frame.setDefaultCloseOperation(onCloseOperation);
        // the content
        frame.setJMenuBar(controller.getMenuBar());
        frame.getContentPane().add(controller.getMasterComponent(), java.awt.BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // defines the size and location
    protected static void initFrameDim(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screen.getWidth() * .90), (int)(screen.getHeight() * .90));
        frame.setLocation((int)(screen.getWidth() * .05), (int)(screen.getHeight() * .05));
        frame.setResizable(true);
    }

    public static RupsController startAsPlugin(JComponent comp) {
        RupsController controller = new RupsController(comp.getSize());
        comp.add(controller.getTreePanel());
        return controller;
    }
}