package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * TreeSearchHandler manages search and navigation operations on a TreeView<ANode>.
 * It allows:
 * - Searching nodes by name
 * - Navigating between results (first, next)
 * - Selecting all matches
 *
 * The class uses a Supplier<TreeView<ANode>> to dynamically retrieve the current TreeView,
 * which is important when multiple views (like "Is-A" or "Part-Of") can be active.
 */
public class TreeSearchHandler {

    private final Supplier<TreeView<ANode>> treeViewSupplier; // Provides the current active TreeView on demand
    private final Label statusLabel;                          // Displays search status messages to the user

    private final List<TreeItem<ANode>> searchResults = new ArrayList<>(); // Holds all matched tree items
    private int currentIndex = -1;                                         // Index of current item for navigation
    private TreeItem<ANode> lastSearchRoot = null;                         // Used to detect if tree structure changed

    /**
     * Constructor for the search handler.
     *
     * @param treeViewSupplier A supplier that provides the currently active TreeView
     * @param statusLabel A label to display user-facing status messages
     */
    public TreeSearchHandler(Supplier<TreeView<ANode>> treeViewSupplier, Label statusLabel) {
        this.treeViewSupplier = treeViewSupplier;
        this.statusLabel = statusLabel;
    }

    /**
     * Performs a search across the currently active TreeView based on the given query.
     *
     * @param query The search term to look for in node names
     * @return true if matches are found, false otherwise
     */
    public boolean search(String query) {
        TreeView<ANode> treeView = treeViewSupplier.get(); // Always get latest active tree

        query = query.trim().toLowerCase(); // Normalize query
        searchResults.clear();              // Clear any previous results
        currentIndex = -1;

        if (query.isEmpty()) {
            statusLabel.setText("Please enter a search term.");
            return false;
        }

        TreeItem<ANode> root = treeView.getRoot();
        lastSearchRoot = root;
        findMatches(root, query); // Recursively collect matches

        if (!searchResults.isEmpty()) {
            currentIndex = 0;
            selectItem(treeView, searchResults.get(currentIndex)); // Highlight first match
            statusLabel.setText("Found " + searchResults.size() + " matches");
            return true;
        } else {
            statusLabel.setText("No match found for: \"" + query + "\"");
            return false;
        }
    }

    /**
     * Highlights the first match result for the given query.
     * Re-triggers search if results are stale or tree has changed.
     *
     * @param query The search term
     */
    public void showFirst(String query) {
        TreeView<ANode> treeView = treeViewSupplier.get();

        if (treeRootChanged(treeView) || searchResults.isEmpty()) {
            if (!search(query)) return;
        }

        currentIndex = 0;
        selectItem(treeView, searchResults.get(currentIndex));
        statusLabel.setText("Match 1 of " + searchResults.size());
    }

    /**
     * Highlights the next match result, cycling back to the start if at the end.
     *
     * @param query The search term
     */
    public void showNext(String query) {
        TreeView<ANode> treeView = treeViewSupplier.get();

        if (treeRootChanged(treeView) || searchResults.isEmpty()) {
            if (!search(query)) return;
        }

        currentIndex = (currentIndex + 1) % searchResults.size(); // Wrap-around cycling
        selectItem(treeView, searchResults.get(currentIndex));
        statusLabel.setText("Match " + (currentIndex + 1) + " of " + searchResults.size());
    }

    /**
     * Selects all nodes that match the current query.
     *
     * @param query The search term
     */
    public void selectAll(String query) {
        TreeView<ANode> treeView = treeViewSupplier.get();

        if (treeRootChanged(treeView) || searchResults.isEmpty()) {
            if (!search(query)) return;
        }

        MultipleSelectionModel<TreeItem<ANode>> model = treeView.getSelectionModel();
        model.clearSelection(); // Reset previous selections

        for (TreeItem<ANode> item : searchResults) {
            model.select(item);
        }

        statusLabel.setText(searchResults.size() + " matches selected");
    }

    /**
     * Recursively searches the tree for nodes whose names contain the query.
     *
     * @param root The node to start the search from
     * @param query The search string, already lowercased
     */
    private void findMatches(TreeItem<ANode> root, String query) {
        if (root.getValue().name().toLowerCase().contains(query)) {
            searchResults.add(root);
        }
        for (TreeItem<ANode> child : root.getChildren()) {
            findMatches(child, query);
        }
    }

    /**
     * Selects a specific TreeItem and scrolls to it in the TreeView.
     *
     * @param treeView The TreeView where the item resides
     * @param item The TreeItem to highlight
     */
    private void selectItem(TreeView<ANode> treeView, TreeItem<ANode> item) {
        MultipleSelectionModel<TreeItem<ANode>> model = treeView.getSelectionModel();
        model.clearSelection();
        model.select(item);

        // Ensure item is scrolled into view (on UI thread)
        Platform.runLater(() -> treeView.scrollTo(treeView.getRow(item)));
    }

    /**
     * Determines whether the root of the current TreeView has changed
     * since the last search (e.g., due to tab switch).
     *
     * @param treeView The current TreeView
     * @return true if the root has changed, false otherwise
     */
    private boolean treeRootChanged(TreeView<ANode> treeView) {
        return treeView.getRoot() != lastSearchRoot;
    }
}
