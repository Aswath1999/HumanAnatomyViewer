package HumanAnatomyViewer.window;

/**
 * SimpleCommand is a lightweight implementation of the Command interface.
 * It encapsulates undo and redo logic using Java's Runnable interface,
 * allowing arbitrary code blocks to be stored and executed later.
 * This design is ideal for one-off or small reversible actions (e.g., changing color,
 * hiding models, toggling visibility) without needing to create a separate Command class each time.
 */
public class SimpleCommand implements Command {

    // Human-readable name or description of the command (used for logs, UI, etc.)
    private final String name;

    // Runnable to execute when undoing the command
    private final Runnable undoRunnable;

    // Runnable to execute when redoing the command
    private final Runnable redoRunnable;

    /**
     * Constructor to create a command using undo/redo code blocks.
     * @param name          name or description of the command
     * @param undoRunnable  code to run when undoing (can be null if undo not supported)
     * @param redoRunnable  code to run when redoing (can be null if redo not supported)
     */
    public SimpleCommand(String name, Runnable undoRunnable, Runnable redoRunnable) {
        this.name = name;
        this.undoRunnable = undoRunnable;
        this.redoRunnable = redoRunnable;
    }

    /**
     * Executes the undo logic, if available.
     */
    @Override
    public void undo() {
        undoRunnable.run();
    }

    /**
     * Executes the redo logic, if available.
     */
    @Override
    public void redo() {
        redoRunnable.run();
    }

    /**
     * Returns the name or description of the command.
     *
     * @return command name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Checks whether the command can be undone.
     *
     * @return true if undo logic is provided; false otherwise
     */
    @Override
    public boolean canUndo() {
        return undoRunnable != null;
    }

    /**
     * Checks whether the command can be redone.
     *
     * @return true if redo logic is provided; false otherwise
     */
    @Override
    public boolean canRedo() {
        return redoRunnable != null;
    }
}