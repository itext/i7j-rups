package com.itextpdf.rups.view;

import java.util.Locale;
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
    CONSOLE_BACKUP,
    CONSOLE_ERROR,
    CONSOLE_INFO,
    COPY,
    COPY_TO_CLIPBOARD,

    DEBUG_INFO,
    DEBUG_INFO_DESCRIPTION,
    DIALOG,
    DIALOG_CANCEL,
    DIALOG_ENTER,
    DIALOG_PROGRESS,
    DIALOG_VALUE,
    DICTIONARY,
    DICTIONARY_KEY,
    DICTIONARY_OF_TYPE,
    DICTIONARY_VALUE,

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
    ERROR_DUPLICATE_KEY,
    ERROR_EMPTY_FIELD,
    ERROR_EDITING_UNSPECIFIED_DOCUMENT,
    ERROR_ILLEGAL_CHUNK,
    ERROR_INCORRECT_ARRAY_BRACKETS,
    ERROR_INCORRECT_DICTIONARY_BRACKETS,
    ERROR_INDEX_NOT_INTEGER,
    ERROR_INDEX_NOT_IN_RANGE,
    ERROR_KEY_IS_NOT_NAME,
    ERROR_LOADING_IMAGE,
    ERROR_LOADING_XFA,
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
    LOG_TREE_NODE_CREATED,

    MENU_BAR_ABOUT,
    MENU_BAR_CLOSE,
    MENU_BAR_COMPARE_WITH,
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
    PDF_OBJECT_TREE,
    PLAINTEXT,
    PLAINTEXT_DESCRIPTION,

    RAW_BYTES,

    SAVE,
    SAVE_IMAGE,
    SAVE_OVERWRITE,
    SAVE_RAW_BYTES_TO_FILE,
    SAVE_SUCCESS,
    SAVE_TO_FILE,
    SAVE_TO_STREAM,
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
    XREF_DESCRIPTION,
    XREF_NUMBER,
    XREF_OBJECT,
    XREF_READING;

    /**
     * The location of the resource bundles.
     */
    private static final String BUNDLE_LOCATION = "bundles/rups-lang";


    /**
     * Locale Set by User.
     */
    private static Locale USER_LOCALE = Locale.getDefault();

    /**
     * Changes the Locale of the application.
     *
     * @param locale new Locale to use
     */
    public static void setLocale(final Locale locale) {
        if ( locale != null ) {
            USER_LOCALE = locale;
        } else {
            USER_LOCALE = Locale.getDefault();
        }

        ResourceBundle.clearCache();
    }

    /**
     * Getter for the user set locale
     * @return locale
     */
    public static Locale getLocale() {
        return USER_LOCALE;
    }

    /**
     * Retrieves the String appropriate for the selected Locale.
     *
     * @return the correct String value
     */
    public String getString() {
        return ResourceBundle.getBundle(BUNDLE_LOCATION, USER_LOCALE).getString(name());
    }
}
