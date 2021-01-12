/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
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
package com.itextpdf.rups.model;

import com.itextpdf.kernel.pdf.EncryptionConstants;

/**
 * This class can tell you more about the permissions that are allowed
 * on the PDF file.
 */
public class Permissions {

    /**
     * Was the file encrypted?
     */
    protected boolean encrypted = true;
    /**
     * Which owner password was provided to open the file?
     */
    protected byte[] ownerPassword = null;
    /**
     * What is the user password?
     */
    protected byte[] userPassword = null;
    /**
     * What are the document permissions?
     */
    protected int permissions = 0;
    /**
     * How was the document encrypted?
     */
    protected int cryptoMode = 0;

    /**
     * Tells you if the document was encrypted.
     *
     * @return true is the document was encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Setter for the encrypted variable.
     *
     * @param encrypted set this to true if the document was encrypted
     */
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * Returns the owner password of the PDF file (if any).
     *
     * @return the owner password that was provided upon opening the document
     */
    public byte[] getOwnerPassword() {
        return ownerPassword;
    }

    /**
     * Setter for the owner password.
     *
     * @param ownerPassword the owner password
     */
    public void setOwnerPassword(byte[] ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    /**
     * Returns the user password (if any).
     *
     * @return the user password
     */
    public byte[] getUserPassword() {
        return userPassword;
    }

    /**
     * Setter for the user password.
     *
     * @param userPassword the user password of a PDF file
     */
    public void setUserPassword(byte[] userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Returns the permissions in the form of an int (each bit is a specific permission)
     *
     * @return the value for the permissions
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * Setter for the permissions.
     *
     * @param permissions the permissions in the form of an int
     */
    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    /**
     * Returns the crypto mode.
     *
     * @return the crypto mode
     */
    public int getCryptoMode() {
        return cryptoMode;
    }

    /**
     * Setter for the crypto mode
     *
     * @param cryptoMode the crypto mode
     */
    public void setCryptoMode(int cryptoMode) {
        this.cryptoMode = cryptoMode;
    }

    /**
     * Tells you if printing is allowed.
     *
     * @return true if printing is allowed
     */
    public boolean isAllowPrinting() {
        if (!encrypted) return true;
        return (EncryptionConstants.ALLOW_PRINTING & permissions) == EncryptionConstants.ALLOW_PRINTING;
    }

    /**
     * Tells you if modifying the contents is allowed.
     *
     * @param decrypted decrypted
     * @return true if modifying contents is allowed
     */
    public boolean isAllowModifyContents(boolean decrypted) {
        if (!encrypted) return true;
        return (EncryptionConstants.ALLOW_MODIFY_CONTENTS & permissions) == EncryptionConstants.ALLOW_MODIFY_CONTENTS;
    }

    /**
     * Tells you if copying is allowed.
     *
     * @param decrypted decrypted
     * @return true if copying is allowed
     */
    public boolean isAllowCopy(boolean decrypted) {
        if (!encrypted) return true;
        return (EncryptionConstants.ALLOW_COPY & permissions) == EncryptionConstants.ALLOW_COPY;
    }

    /**
     * Tells you if modifying annotations is allowed
     *
     * @return true if modifying annotations is allowed
     */
    public boolean isAllowModifyAnnotations() {
        if (!encrypted) return true;
        return (EncryptionConstants.ALLOW_MODIFY_ANNOTATIONS & permissions) == EncryptionConstants.ALLOW_MODIFY_ANNOTATIONS;
    }

    /**
     * Tells you if filling in forms is allowed.
     *
     * @return true if filling in forms is allowed
     */
    public boolean isAllowFillIn() {
        if (!encrypted) return true;
        return (EncryptionConstants.ALLOW_FILL_IN & permissions) == EncryptionConstants.ALLOW_FILL_IN;
    }

    /**
     * Tells you if modifying the layout for screenreaders is allowed.
     *
     * @return true if modifying the layout for screenreaders is allowed
     */
    public boolean isAllowScreenReaders() {
        if (!encrypted) return true;
        return (EncryptionConstants.ALLOW_SCREENREADERS & permissions) == EncryptionConstants.ALLOW_SCREENREADERS;
    }

    /**
     * Tells you if document assembly is allowed.
     *
     * @return true if document assembly is allowed
     */
    public boolean isAllowAssembly() {
        if (!encrypted) return true;
        return (EncryptionConstants.ALLOW_ASSEMBLY & permissions) == EncryptionConstants.ALLOW_ASSEMBLY;
    }

    /**
     * Tells you if degraded printing is allowed.
     *
     * @return true if degraded printing is allowed
     */
    public boolean isAllowDegradedPrinting() {
        if (!encrypted) return true;
        return (EncryptionConstants.ALLOW_DEGRADED_PRINTING & permissions) == EncryptionConstants.ALLOW_DEGRADED_PRINTING;
    }
}
