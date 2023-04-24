package com.itextpdf.rups.io;

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.event.NodeAddArrayChildEvent;
import com.itextpdf.rups.event.NodeAddDictChildEvent;
import com.itextpdf.rups.event.NodeDeleteArrayChildEvent;
import com.itextpdf.rups.event.NodeDeleteDictChildEvent;
import com.itextpdf.rups.event.NodeUpdateArrayChildEvent;
import com.itextpdf.rups.event.NodeUpdateDictChildEvent;
import com.itextpdf.rups.event.RupsEvent;
import com.itextpdf.rups.model.ErrorDialogPane;
import com.itextpdf.rups.view.itext.PdfObjectPanel;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PdfObjectTreeEdit implements UndoableEdit {

    enum ObjectType {
        ARRAY, DICT
    }
    enum ActionType {
        ADD, DELETE, UPDATE
    }

    ObjectType objectType;
    ActionType actionType;
    PdfReaderController controller;
//    PdfObjectPanel objectPanel;
    int editType; // The type of edit, as defined by the static bytes within RupsEvent.java (Refactor to Enum?)
    PdfObjectTreeNode parent; // Parent Node
    PdfObjectTreeNode oldChild; // Child Node
    PdfObjectTreeNode child; // Child Node
    int index; // Index at which the edit occurred.
    PdfName key; // Index at which the edit occurred.
    List<UndoableEdit> addedEdits;
    boolean dead;

    // TODO: Change this to fire NodeUpdateDictChildEvents instead
    // TODO: Abstract this into separate Array and Dict subclasses? - seems messy though..
    public PdfObjectTreeEdit(PdfReaderController controller, RupsEvent editEvent) {
        this.controller = controller;
        this.addedEdits = new LinkedList<>();
        this.dead = false;
        extractEditData(editEvent);
    }

    private void extractEditData(RupsEvent editEvent) {
        // TODO: Maybe we can add some granularity to these events?
        // i.e. (abs)RupsEvent>(abs)NodeArrayEvent>NodeAddArrayEvent so the common array interface of Index, Parent and Value are
        // always present and (abs)RupsEvent>(abs)NodeDictEvent>NodeAddDictEvent so that Key, Parent and Value are always present?

        editType = editEvent.getType();
        switch (editType) {
            case RupsEvent.NODE_ADD_DICT_CHILD_EVENT:
                NodeAddDictChildEvent.Content addDictContent = (NodeAddDictChildEvent.Content)editEvent.getContent();
                parent = addDictContent.parent;
                child = PdfObjectTreeNode.getInstance(addDictContent.value);
                index = addDictContent.index;
                key = addDictContent.key;
                objectType = ObjectType.DICT;
                actionType = ActionType.ADD;
                break;
            case RupsEvent.NODE_ADD_ARRAY_CHILD_EVENT:
                NodeAddArrayChildEvent.Content addArrayContent = (NodeAddArrayChildEvent.Content)editEvent.getContent();
                parent = addArrayContent.parent;
                child = PdfObjectTreeNode.getInstance(addArrayContent.value);
                index = addArrayContent.index;
                objectType = ObjectType.ARRAY;
                actionType = ActionType.ADD;
                break;
            case RupsEvent.NODE_DELETE_DICT_CHILD_EVENT:
                NodeDeleteDictChildEvent.Content deleteDictContent = (NodeDeleteDictChildEvent.Content)editEvent.getContent();
                parent = deleteDictContent.parent;
                key = deleteDictContent.key;
                child = deleteDictContent.parent.getDictionaryChildNode(key);
                index = deleteDictContent.parent.getIndex(this.child);
                objectType = ObjectType.DICT;
                actionType = ActionType.DELETE;
                break;
            case RupsEvent.NODE_DELETE_ARRAY_CHILD_EVENT:
                NodeDeleteArrayChildEvent.Content deleteArrayContent = (NodeDeleteArrayChildEvent.Content)editEvent.getContent();
                parent = deleteArrayContent.parent;
                index = deleteArrayContent.index;
                child = (PdfObjectTreeNode) parent.getChildAt(index);
                objectType = ObjectType.ARRAY;
                actionType = ActionType.DELETE;
                break;
            case RupsEvent.NODE_UPDATE_DICT_CHILD_EVENT:
                NodeUpdateDictChildEvent.Content updateDictContent = (NodeUpdateDictChildEvent.Content)editEvent.getContent();
                parent = updateDictContent.parent;
                key = updateDictContent.key;
                oldChild = parent.getDictionaryChildNode(key);
                child = PdfObjectTreeNode.getInstance(updateDictContent.value);
                index = updateDictContent.index;
                objectType = ObjectType.DICT;
                actionType = ActionType.UPDATE;
                break;
            case RupsEvent.NODE_UPDATE_ARRAY_CHILD_EVENT:
                NodeUpdateArrayChildEvent.Content updateArrayContent = (NodeUpdateArrayChildEvent.Content)editEvent.getContent();
                parent = updateArrayContent.parent;
                index = updateArrayContent.index;
                child = PdfObjectTreeNode.getInstance(updateArrayContent.value);
                oldChild = (PdfObjectTreeNode) parent.getChildAt(index);
                objectType = ObjectType.ARRAY;
                actionType = ActionType.UPDATE;
                break;
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        if(!canUndo()) throw new CannotUndoException();
        RupsEvent newChild;
            switch (this.editType) {
                case RupsEvent.NODE_ADD_DICT_CHILD_EVENT:
                    newChild = new NodeDeleteDictChildEvent(key, parent);
                    break;
                case RupsEvent.NODE_ADD_ARRAY_CHILD_EVENT:
                    newChild = new NodeDeleteArrayChildEvent(index, parent);
                    break;
                case RupsEvent.NODE_DELETE_DICT_CHILD_EVENT:
                    newChild = new NodeAddDictChildEvent(key, child.getPdfObject(), parent, index);
                    break;
                case RupsEvent.NODE_DELETE_ARRAY_CHILD_EVENT:
                    newChild = new NodeAddArrayChildEvent(child.getPdfObject(), parent, index);
                    break;
                case RupsEvent.NODE_UPDATE_DICT_CHILD_EVENT:
                    newChild = new NodeUpdateDictChildEvent(key, oldChild.getPdfObject(), parent, index);
                    break;
                case RupsEvent.NODE_UPDATE_ARRAY_CHILD_EVENT:
                    newChild = new NodeUpdateArrayChildEvent(oldChild.getPdfObject(), parent, index);
                    break;
                default:
                    throw new CannotUndoException();
            }
            for (int i = addedEdits.size() - 1; i > -1; i--) {
                addedEdits.get(i).undo();
            }
        updateController(newChild, CannotRedoException.class);
    }

    private void updateController(RupsEvent newChild, Class<? extends Exception> exceptionClass) {
        try {
            controller.update(controller, newChild);
            // TODO: Work out why Tree view is repainted but table view is not. <<<<<<<<< This!
            // TODO: Work out why Tree re-rendering doesn't update the table data.
            // TODO: Work out why Update changes aren't reflected in the tree data.
            controller.render(parent);
//            controller.getObjectPanel().repaint();
        } catch (Exception ex) {
            ErrorDialogPane.showErrorDialog(controller.getEditorTabs().getParent(), ex);
            System.err.print(ex.getLocalizedMessage());
//            if( ex instanceof exceptionClass.getDeclaringClass()){
//                throw ex;
//            }
            try {
                throw exceptionClass.getDeclaredConstructor().newInstance(ex.getLocalizedMessage());
            } catch (Exception e) {
                throw new RuntimeException(ex.getLocalizedMessage());
            }
        }
    }

    @Override
    public boolean canUndo() {
        if(!validateSelf()) return false;
        return true;
    }

    @Override
    public void redo() throws CannotRedoException {
        if(!canRedo()) throw new CannotRedoException();

        try {
            RupsEvent newChild;
            switch (editType) {
                case RupsEvent.NODE_ADD_DICT_CHILD_EVENT:
                    newChild = new NodeAddDictChildEvent(key,child.getPdfObject(),parent,index);
                    break;
                case RupsEvent.NODE_ADD_ARRAY_CHILD_EVENT:
                    newChild = new NodeAddArrayChildEvent(child.getPdfObject(),parent,index);
                    break;
                case RupsEvent.NODE_DELETE_DICT_CHILD_EVENT:
                    newChild = new NodeDeleteDictChildEvent(key,parent);
                    break;
                case RupsEvent.NODE_DELETE_ARRAY_CHILD_EVENT:
                    newChild = new NodeDeleteArrayChildEvent(index,parent);
                    break;
                case RupsEvent.NODE_UPDATE_DICT_CHILD_EVENT:
                    newChild = new NodeUpdateDictChildEvent(key,child.getPdfObject(),parent,index);
                    break;
                case RupsEvent.NODE_UPDATE_ARRAY_CHILD_EVENT:
                    newChild = new NodeUpdateArrayChildEvent(child.getPdfObject(),parent,index);
                    break;
                default:
                    throw new CannotRedoException();
            }
            updateController(newChild, CannotRedoException.class);

            for (int i = 0; i < addedEdits.size(); i++) {
                addedEdits.get(i).redo();
            }
        } catch (CannotRedoException ex) {
            // ErrorDialogPane.showErrorDialog( mainWindow, ex);
            ErrorDialogPane.showErrorDialog( controller.getEditorTabs().getParent(), ex);
            System.err.print(ex.getLocalizedMessage());
            throw ex;
        } catch (Exception ex) {
            // ErrorDialogPane.showErrorDialog( mainWindow, ex);
            ErrorDialogPane.showErrorDialog( controller.getEditorTabs().getParent(), ex);
            System.err.print(ex.getLocalizedMessage());
            throw new CannotRedoException();
        }
    }

    @Override
    public boolean canRedo() {
        if(!validateSelf()) return false;
        return true;
    }

    @Override
    public void die() {
        this.dead = true;
    }

    // TODO: if self is Add event, accept Update eventsto the same key or index.
    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        return false;
//
//        if (!(anEdit instanceof PdfObjectTreeEdit)) {
//            return false;
//        }
//        PdfObjectTreeEdit objectTreeEdit = (PdfObjectTreeEdit) anEdit;
//
//        if(objectTreeEdit.parent == child){
//            return true;
//        }
//
//        if (!checkParity(objectTreeEdit)){
//            return false;
//        }
//
//        return addedEdits.add(anEdit);
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit) {
        if (!(anEdit instanceof PdfObjectTreeEdit)) {
            return false;
        }
        if (isSignificant()){
            return false;
        }
        PdfObjectTreeEdit objectTreeEdit = (PdfObjectTreeEdit) anEdit;

        if (!checkParity(objectTreeEdit)){
            return false;
        }

        dead = true;
        return true;
    }

    // TODO: Make updates significant.
    @Override
    public boolean isSignificant() {
//        boolean significant = (!addedEdits.isEmpty())
//                || (actionType == ActionType.ADD)
//                || (actionType == ActionType.DELETE);
//
        return true;
    }

    @Override
    public String getPresentationName() {
        String returnString;
        switch(actionType){
            case ADD:
                returnString = "Addition";
            break;
            case DELETE:
                returnString = "Removal";
                break;
            case UPDATE:
                returnString = "Update";
                break;
            default:
                return "Error";
        }
        returnString.concat(" of");
        switch (objectType){
            case DICT:
                returnString.concat(" a Dict value");
                break;
            case ARRAY:
                returnString.concat(" an Array value");
        }
        return returnString;
    }

    @Override
    public String getUndoPresentationName() {
        return "Undo the ".concat(getPresentationName());
    }

    @Override
    public String getRedoPresentationName() {
        return "Redo the ".concat(getPresentationName());
    }

    private boolean validateSelf() {
        if (dead) return false;

        switch(this.editType){
            case RupsEvent.NODE_ADD_DICT_CHILD_EVENT:
            case RupsEvent.NODE_ADD_ARRAY_CHILD_EVENT:
            case RupsEvent.NODE_UPDATE_DICT_CHILD_EVENT:
            case RupsEvent.NODE_UPDATE_ARRAY_CHILD_EVENT:
            case RupsEvent.NODE_DELETE_DICT_CHILD_EVENT:
            case RupsEvent.NODE_DELETE_ARRAY_CHILD_EVENT:
                return true;
            default:
                return false;
        }
    }

    private boolean checkParity(PdfObjectTreeEdit objectTreeEdit) {

        if(objectTreeEdit.controller != controller){
            return false;
        }

//        if(objectTreeEdit.objectPanel != objectPanel){
//            return false;
//        }

        if(objectTreeEdit.objectType != objectType){
            return false;
        }

        if(objectTreeEdit.index != index){
            return false;
        }

        if(objectTreeEdit.parent != parent){
            return false;
        }

        if(objectTreeEdit.key != key){
            return false;
        }
        return true;
    }
}
