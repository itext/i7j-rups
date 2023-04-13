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
package com.itextpdf.rups.view.icons;

import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.view.Language;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.HashMap;

/**
 * Class that fetches the icons in com.itextpdf.rups.view.icons.
 */
public class IconFetcher {

    /**
     * Cache with icons.
     */
    private static final HashMap<String, Icon> cache = new HashMap<>();

    /**
     * Gets an Icon with a specific name.
     *
     * @param filename the filename of the Icon.
     * @return an Icon
     */
    public static Icon getIcon(String filename) {
        if (filename == null) {
            return null;
        }
        Icon icon = cache.get(filename);
        if (icon == null) {
            try {
                icon = new ImageIcon(IconFetcher.class.getResource(filename));
                cache.put(filename, icon);
            } catch (Exception e) {
                LoggerHelper.error(
                        String.format(Language.ERROR_CANNOT_FIND_FILE.getString(), filename), e, IconFetcher.class
                );
                return null;
            }
        }
        return icon;
    }
}
