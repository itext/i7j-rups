/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups.view.contextmenu;

import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Language;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Action to save an image to the filesystem.
 */
public final class SaveImageAction extends AbstractRupsAction {
    static final String IMAGE_FORMAT = "png";

    private transient BufferedImage saveImg;

    /**
     * Create a save image action with a given name, invoker and target image.
     *
     * @param name    the name of the action
     * @param invoker the action's invoking component
     * @param saveImg the image to save
     */
    public SaveImageAction(String name, Component invoker, BufferedImage saveImg) {
        super(name, invoker);
        this.saveImg = saveImg;
    }

    /**
     * Create a "save image" button.
     *
     * @param saveImg the {@link BufferedImage} to save
     * @return {@link JButton} to perform the save action
     */
    public static JButton createSaveImageButton(final BufferedImage saveImg) {
        final String saveImageString = Language.SAVE_IMAGE.getString();
        final JButton saveImgButton = new JButton(saveImageString);
        saveImgButton.addActionListener(new SaveImageAction(saveImageString, saveImgButton, saveImg));
        return saveImgButton;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            final String file = "Untitled.png";
            final String suffix = ".png";

            final FileDialog fileDialog = new FileDialog(new Frame(), Language.SAVE.getString(), FileDialog.SAVE);
            fileDialog.setFilenameFilter((dir, name) -> name.endsWith(suffix));
            fileDialog.setFile(file);
            fileDialog.setVisible(true);
            ImageIO.write(saveImg, IMAGE_FORMAT, new File(fileDialog.getDirectory() + fileDialog.getFile()));
        } catch (HeadlessException | IOException e) {
            LoggerHelper.error(Language.ERROR_PARSING_IMAGE.getString(), e, getClass());
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageIO.write(saveImg, IMAGE_FORMAT, out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.saveImg = ImageIO.read(in);
    }
}
