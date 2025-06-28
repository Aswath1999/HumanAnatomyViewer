package HumanAnatomyViewer.window;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoRedoManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();
    private boolean inUndoRedo = false;

    public void add(Command command) {
        if (inUndoRedo) return;
        if (command.canUndo()) undoStack.push(command);
        else undoStack.clear();
        redoStack.clear();
        command.redo();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        inUndoRedo = true;
        try {
            Command cmd = undoStack.pop();
            cmd.undo();
            if (cmd.canRedo()) redoStack.push(cmd);
            else redoStack.clear();
        } finally {
            inUndoRedo = false;
        }
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        inUndoRedo = true;
        try {
            Command cmd = redoStack.pop();
            cmd.redo();
            if (cmd.canUndo()) undoStack.push(cmd);
            else undoStack.clear();
        } finally {
            inUndoRedo = false;
        }
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}