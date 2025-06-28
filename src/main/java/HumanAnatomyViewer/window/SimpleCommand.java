package HumanAnatomyViewer.window;

public class SimpleCommand implements Command {
    private final String name;
    private final Runnable undoRunnable;
    private final Runnable redoRunnable;

    public SimpleCommand(String name, Runnable undoRunnable, Runnable redoRunnable) {
        this.name = name;
        this.undoRunnable = undoRunnable;
        this.redoRunnable = redoRunnable;
    }

    @Override public void undo() { undoRunnable.run(); }
    @Override public void redo() { redoRunnable.run(); }
    @Override public String name() { return name; }
    @Override public boolean canUndo() { return undoRunnable != null; }
    @Override public boolean canRedo() { return redoRunnable != null; }
}