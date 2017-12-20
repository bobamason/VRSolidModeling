package net.masonapps.vrsolidmodeling.ui;

import android.support.annotation.Nullable;

import java.util.Stack;

/**
 * Created by Bob on 7/21/2017.
 */

public class UndoRedoCache {
    private static final int MAX_UNDO_STACK_COUNT = 10;
    private Stack<Object> undoStack = new Stack<>();
    private Stack<Object> redoStack = new Stack<>();
    @Nullable
    private Object current = null;

    public static void applySaveData() {
    }

    @Nullable
    public Object undo() {
        if (undoStack.empty()) return null;
        if (current != null)
            redoStack.push(current);
        current = undoStack.pop();
        return current;
    }

    public int getUndoCount() {
        return undoStack.size();
    }

    @Nullable
    public Object redo() {
        if (redoStack.empty()) return null;
        if (current != null)
            undoStack.push(current);
        current = redoStack.pop();
        return current;
    }

    public int getRedoCount() {
        return redoStack.size();
    }

    public void save(Object obj) {
        if (undoStack.size() >= MAX_UNDO_STACK_COUNT)
            undoStack.remove(0);
        redoStack.clear();
        if (current != null)
            undoStack.push(current);
        current = obj;
    }

    public void clear() {
        current = null;
        undoStack.clear();
        redoStack.clear();
    }

}
