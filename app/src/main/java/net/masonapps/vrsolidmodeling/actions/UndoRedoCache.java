package net.masonapps.vrsolidmodeling.actions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Stack;

/**
 * Created by Bob on 7/21/2017.
 */

public class UndoRedoCache {
    private static final int MAX_UNDO_STACK_COUNT = 20;
    private Stack<Action> undoStack = new Stack<>();
    private Stack<Action> redoStack = new Stack<>();
    @Nullable
    private Action current = null;

    public static void applySaveData() {
    }

    @Nullable
    public Action undo() {
        if (undoStack.empty()) return null;
        if (current != null)
            redoStack.push(current);
        current = undoStack.pop();
        current.undoAction();
        return current;
    }

    public int getUndoCount() {
        return undoStack.size();
    }

    @Nullable
    public Action redo() {
        if (redoStack.empty()) return null;
        if (current != null)
            undoStack.push(current);
        current = redoStack.pop();
        current.redoAction();
        return current;
    }

    public int getRedoCount() {
        return redoStack.size();
    }

    public void save(@NonNull Action action) {
        if (undoStack.size() >= MAX_UNDO_STACK_COUNT)
            undoStack.remove(0);
        redoStack.clear();
        if (current != null)
            undoStack.push(current);
        current = action;
    }

    public void clear() {
        current = null;
        undoStack.clear();
        redoStack.clear();
    }

}
