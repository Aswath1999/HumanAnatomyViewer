package HumanAnatomyViewer.window;

/**
 * Command interface defines the contract for implementing
 * undoable and redoable actions in the Human Anatomy Viewer application.

 */
public interface Command {

    /**
     * Reverses the action performed by this command.
     * For example, hiding a model should unhide it.
     */
    void undo();

    /**
     * Re-applies the action after it has been undone.
     * For example, reapplying a color change to a model.
     */
    void redo();

    /**
     * Returns the name or description of the command.
     * This can be useful for UI elements like tooltips, logs, or history tracking.
     *
     * @return a human-readable name of the command
     */
    String name();

    /**
     * Indicates whether this command supports being undone.
     *
     * @return true if undo is possible, false otherwise
     */
    boolean canUndo();

    /**
     * Indicates whether this command supports being redone.
     *
     * @return true if redo is possible, false otherwise
     */
    boolean canRedo();
}