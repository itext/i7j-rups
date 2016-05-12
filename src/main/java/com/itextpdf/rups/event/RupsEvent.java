package com.itextpdf.rups.event;

/**
 * Abstract Event Class that other events should extend from.
 */
public abstract class RupsEvent {

    public static final byte OPEN_FILE_EVENT = 0;
    public static final byte OPEN_DOCUMENT_POST_EVENT = 1;
    public static final byte CLOSE_DOCUMENT_EVENT = 2;
    public static final byte SAVE_TO_FILE_EVENT = 3;
    public static final byte COMPARE_WITH_FILE_EVENT = 4;
    public static final byte COMPARE_POST_EVENT = 5;
    public static final byte ROOT_NODE_CLICKED_EVENT = 6;
    public static final byte TREE_NODE_CLICKED_EVENT = 7;
    public static final byte OPEN_STRUCTURE_EVENT = 8;
    public static final byte OPEN_PLAIN_TEXT_EVENT = 9;

    public abstract int getType();

    public abstract Object getContent();
}
