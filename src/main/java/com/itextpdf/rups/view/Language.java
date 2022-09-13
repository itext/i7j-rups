/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
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
package com.itextpdf.rups.view;

import com.itextpdf.rups.RupsConfiguration;

import java.util.ResourceBundle;

/**
 * This enum will load the String appropriate for the currently set Locale, either through the System default or
 * by using the user-set Locale.
 *
 * <p>
 * Expanding on this class is as easy as making a new enum constant and adding a String variable with the same name
 * in the resource bundles.
 */
public enum Language {
    ARRAY,
    ARRAY_CHOOSE_INDEX,

    BOOKMARKS,

    CLEAR,
    COMPARE_EQUAL,
    COMPARE_WITH,
    CONSOLE,
    CONSOLE_BACKUP,
    CONSOLE_ERROR,
    CONSOLE_INFO,
    CONSOLE_TOOL_TIP,
    COPY,
    COPY_TO_CLIPBOARD,

    DEBUG_INFO,
    DEBUG_INFO_DESCRIPTION,
    DEFAULT_TAB_TEXT,
    DEFAULT_TAB_TITLE,
    DIALOG,
    DIALOG_CANCEL,
    DIALOG_ENTER,
    DIALOG_PROGRESS,
    DIALOG_VALUE,
    DICTIONARY,
    DICTIONARY_KEY,
    DICTIONARY_OF_TYPE,
    DICTIONARY_VALUE,
    DUPLICATE_FILES_OFF,

    EDITOR_CONSOLE,
    EDITOR_CONSOLE_TOOLTIP,
    ENTER_PASSWORD,

    ERROR,
    ERROR_BUILDING_CONTENT_STREAM,
    ERROR_CANNOT_CHECK_NULL_FOR_INPUT_STREAM,
    ERROR_CANNOT_FIND_FILE,
    ERROR_CLOSING_STREAM,
    ERROR_COMPARE_DOCUMENT_CREATION,
    ERROR_COMPARED_DOCUMENT_CLOSED,
    ERROR_COMPARED_DOCUMENT_NULL,
    ERROR_DRAG_AND_DROP,
    ERROR_DUPLICATE_KEY,
    ERROR_EMPTY_FIELD,
    ERROR_EDITING_UNSPECIFIED_DOCUMENT,
    ERROR_FILE_COULD_NOT_BE_VIEWED,
    ERROR_ILLEGAL_CHUNK,
    ERROR_INCORRECT_ARRAY_BRACKETS,
    ERROR_INCORRECT_DICTIONARY_BRACKETS,
    ERROR_INDEX_NOT_INTEGER,
    ERROR_INDEX_NOT_IN_RANGE,
    ERROR_INITIALIZING_SETTINGS,
    ERROR_KEY_IS_NOT_NAME,
    ERROR_LOADING_DEFAULT_SETTINGS,
    ERROR_LOADING_IMAGE,
    ERROR_LOADING_XFA,
    ERROR_LOOK_AND_FEEL,
    ERROR_NO_OPEN_DOCUMENT_COMPARE,
    ERROR_ONLY_OPEN_ONE_FILE,
    ERROR_OPENING_FILE,
    ERROR_PARENT_NULL,
    ERROR_PARSING_IMAGE,
    ERROR_PARSING_PDF_OBJECT,
    ERROR_PARSING_PDF_STREAM,
    ERROR_PARSING_XML,
    ERROR_PASSWORD,
    ERROR_PROCESSING_IMAGE,
    ERROR_QUERY_CONTENT_STREAM,
    ERROR_READING_OBJECT_NUMBER,
    ERROR_REFLECTION_PDF_STREAM,
    ERROR_TOO_MANY_OUTPUT,
    ERROR_TRUNCATED_INPUT,
    ERROR_UNEXPECTED_EXCEPTION,
    ERROR_UNEXPECTED_SYNTAX,
    ERROR_WHILE_LOADING_TEXT,
    ERROR_WRITING_FILE,
    ERROR_WRONG_ENCODING,
    ERROR_WRONG_PASSWORD,

    FILE_FILTER_DESCRIPTION,
    FORM,
    FORM_FIELDS,
    FORM_INTERACTIVE,
    FORM_UNNAMED_FIELD,
    FORM_XDP,
    FORM_XFA,
    FORM_XFA_DESCRIPTION,
    FORM_XFA_DOCUMENT,
    FORM_XFA_LONG_FORM,

    GUI_UPDATING,

    IGNORED_TEXT,
    INDIRECT_OBJECT,
    INDIRECT_OBJECT_CREATION_SUCCESS,
    INLINE_IMAGE_ALT,
    INSPECT_OBJECT,

    KEEP_CHUNKS_AS_LITERALS,

    LOADING,
    LOCALE,
    LOG_TREE_NODE_CREATED,

    MENU_BAR_ABOUT,
    MENU_BAR_CLOSE,
    MENU_BAR_COMPARE_WITH,
    MENU_BAR_EDIT,
    MENU_BAR_FILE,
    MENU_BAR_HELP,
    MENU_BAR_NEW_INDIRECT,
    MENU_BAR_OPEN,
    MENU_BAR_OPEN_IN_PDF_VIEWER,
    MENU_BAR_SAVE_AS,
    MENU_BAR_VERSION,
    MESSAGE_ABOUT,

    NO_SELECTED_FILE,
    NULL_AS_TEXT,

    OBJECT,
    OPEN,
    OPEN_PDF,
    OUTLINES,
    OUTLINES_BOOKMARKS,

    PAGE,
    PAGE_NUMBER,
    PAGES,
    PAGES_TABLE_OBJECT,
    PDF_READING,
    PDF_OBJECT_STREAMS_TREE_NODE,
    PDF_OBJECT_TREE,
    PLAINTEXT,
    PLAINTEXT_DESCRIPTION,
    PREFERENCES,
    PREFERENCES_ALLOW_DUPLICATE_FILES,
    PREFERENCES_NEED_RESTART,
    PREFERENCES_OPEN_FOLDER,
    PREFERENCES_RESET_TO_DEFAULTS,
    PREFERENCES_RESET_TO_DEFAULTS_CONFIRM,
    PREFERENCES_RUPS_SETTINGS,
    PREFERENCES_SELECT_NEW_DEFAULT_FOLDER,
    PREFERENCES_VISUAL_SETTINGS,

    RAW_BYTES,

    SAVE,
    SAVE_IMAGE,
    SAVE_OVERWRITE,
    SAVE_RAW_BYTES_TO_FILE,
    SAVE_SUCCESS,
    SAVE_TO_FILE,
    SAVE_TO_STREAM,
    SAVE_UNSAVED_CHANGES,
    SELECT_ALL,
    STREAM,
    STREAM_OF_TYPE,
    STRUCTURE,
    STRUCTURE_TREE,

    TABLE,
    TEXT,
    TITLE,
    TITLE_OBJECT_INSPECTION,
    TITLE_UNRECOGNIZED_CHUNKS,
    TOOLTIP_ENCODING,
    TOOLTIP_HEX,

    WARNING,

    XREF,
    XREF_BYTE_OFFSET,
    XREF_BYTE_OFFSET_OBJECT_STREAM,
    XREF_DESCRIPTION,
    XREF_NA,
    XREF_NUMBER,
    XREF_OBJECT,
    XREF_READING
    ;

    /**
     * The location of the resource bundles.
     */
    private static final String BUNDLE_LOCATION = "bundles/rups-lang";

    /**
     * Retrieves the String appropriate for the selected Locale.
     *
     * @return the correct String value
     */
    public String getString() {
        return ResourceBundle.getBundle(BUNDLE_LOCATION, RupsConfiguration.INSTANCE.getUserLocale()).getString(name());
    }

}
