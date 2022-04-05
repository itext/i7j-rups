package com.itextpdf.rups.mock;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDropEvent;

public class MockedDropTargetDropEvent extends DropTargetDropEvent {
    public MockedDropTargetDropEvent(DropTargetContext dtc, Point cursorLocn, int dropAction,
            int srcActions) {
        super(dtc, cursorLocn, dropAction, srcActions);
    }

    public MockedDropTargetDropEvent(DropTargetContext dtc, Point cursorLocn, int dropAction, int srcActions,
            boolean isLocal) {
        super(dtc, cursorLocn, dropAction, srcActions, isLocal);
    }

    public MockedDropTargetDropEvent(Transferable transferable, DropTarget dropTarget) {
        super(dropTarget.getDropTargetContext(), new Point(0,0), DnDConstants.ACTION_MOVE, DnDConstants.ACTION_COPY_OR_MOVE);

        this.mockedTransferable = transferable;
    }

    public Transferable mockedTransferable;

    @Override
    public void acceptDrop(int dropAction) {
        // sure :)
    }

    @Override
    public Transferable getTransferable() {
        return this.mockedTransferable;
    }

    @Override
    public void dropComplete(boolean success) {
        // Great success!
    }
}
