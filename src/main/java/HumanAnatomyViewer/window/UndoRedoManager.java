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

    /**
     * Adds a new command to the undo stack and executes it immediately (via redo).

     * This method should be used when a user performs a new action (e.g., color change, hide/show).
     * It handles undo/redo stack management, including clearing redo history if necessary.
     *
     * @param command The command to add and execute.
     */
    public void add(Command command) {
        // Prevent recursive or nested calls during undo/redo operations
        if (!inUndoRedo) {
            // If the command supports undo, store it in the undo stack
            if (command.canUndo()) {
                undoStack.push(command);
            } else {
                // If it cannot be undone, clear the undo history (optional design choice)
                undoStack.clear();
            }

            // Clear the redo stack because this is a new user-initiated action
            redoStack.clear();

            // Immediately execute the command via its redo() logic
            command.redo();

            // Update UI properties or observers (e.g., button enable/disable states)
            updateProperties();
        }
    }

    /**
     * Undoes the most recent command from the undo stack.
     *
     * Moves the command to the redo stack if redo is supported.
     */
    public void undo() {
        // Check if there is anything to undo
        if (!undoStack.isEmpty()) {
            inUndoRedo = true; // prevent recursive triggers while undoing
            try {
                // Get the most recent command
                Command cmd = undoStack.pop();

                // Execute its undo logic
                cmd.undo();

                // If the command can be redone, push it to the redo stack
                if (cmd.canRedo()) {
                    redoStack.push(cmd);
                } else {
                    // Otherwise clear redo stack (e.g., irreversible action)
                    redoStack.clear();
                }
            } finally {
                // Always reset the inUndoRedo flag and update UI or observers
                inUndoRedo = false;
                updateProperties();
            }
        }
    }

    /**
     * Redoes the most recent command from the redo stack.
     *
     * Moves the command back to the undo stack if undo is supported.
     */
    public void redo() {
        // Check if there is anything to redo
        if (!redoStack.isEmpty()) {
            inUndoRedo = true; // prevent recursion while redoing
            try {
                // Get the most recent command
                Command cmd = redoStack.pop();

                // Execute its redo logic
                cmd.redo();

                // If the command supports undo, return it to the undo stack
                if (cmd.canUndo()) {
                    undoStack.push(cmd);
                } else {
                    // Otherwise clear undo history (optional design)
                    undoStack.clear();
                }
            } finally {
                // Always reset the inUndoRedo flag and refresh UI/observers
                inUndoRedo = false;
                updateProperties();
            }
        }
    }

    /**
     * Updates the observable properties that indicate whether
     * undo and redo operations are currently available.
     *
     * This is typically called after a change to the undo/redo stacks
     * (e.g., after add, undo, or redo) to update UI bindings.
     */
    private void updateProperties() {
        // Set the 'canUndo' property to true if there is at least one command to undo
        canUndo.set(!undoStack.isEmpty());

        // Set the 'canRedo' property to true if there is at least one command to redo
        canRedo.set(!redoStack.isEmpty());
    }

    /**
     * Exposes the read-only property bound to the undo availability state.
     *
     * UI elements (like an Undo button) can bind to this to enable/disable themselves.
     *
     * @return a read-only boolean property representing whether undo is possible
     */
    public ReadOnlyBooleanProperty canUndoProperty() {
        return canUndo;
    }

    /**
     * Exposes the read-only property bound to the redo availability state.
     *
     * UI elements (like a Redo button) can bind to this to enable/disable themselves.
     *
     * @return a read-only boolean property representing whether redo is possible
     */
    public ReadOnlyBooleanProperty canRedoProperty() {
        return canRedo;
    }


}