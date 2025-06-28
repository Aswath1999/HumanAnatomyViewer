package HumanAnatomyViewer.window;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;

import java.util.ArrayDeque;
import java.util.Deque;
public class UndoRedoManager {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();
    private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private final BooleanProperty canRedo = new SimpleBooleanProperty(false);
    private boolean inUndoRedo = false;

    public void add(Command command) {
        if (!inUndoRedo) {
            if (command.canUndo()) undoStack.push(command);
            else undoStack.clear();
            redoStack.clear();
            command.redo();
            updateProperties();
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            inUndoRedo = true;
            try {
                Command cmd = undoStack.pop();
                cmd.undo();
                if (cmd.canRedo()) redoStack.push(cmd);
                else redoStack.clear();
            } finally {
                inUndoRedo = false;
                updateProperties();
            }
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            inUndoRedo = true;
            try {
                Command cmd = redoStack.pop();
                cmd.redo();
                if (cmd.canUndo()) undoStack.push(cmd);
                else undoStack.clear();
            } finally {
                inUndoRedo = false;
                updateProperties();
            }
        }
    }

    private void updateProperties() {
        canUndo.set(!undoStack.isEmpty());
        canRedo.set(!redoStack.isEmpty());
    }

    public ReadOnlyBooleanProperty canUndoProperty() {
        return canUndo;
    }

    public ReadOnlyBooleanProperty canRedoProperty() {
        return canRedo;
    }
}