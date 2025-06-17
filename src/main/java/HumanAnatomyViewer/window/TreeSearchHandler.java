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

public class TreeSearchHandler {

    private final Supplier<TreeView<ANode>> treeViewSupplier;
    private final Label statusLabel;

    private final List<TreeItem<ANode>> searchResults = new ArrayList<>();
    private int currentIndex = -1;
    private TreeItem<ANode> lastSearchRoot = null;

    public TreeSearchHandler(Supplier<TreeView<ANode>> treeViewSupplier, Label statusLabel) {
        this.treeViewSupplier = treeViewSupplier;
        this.statusLabel = statusLabel;
    }

    public boolean search(String query) {
        TreeView<ANode> treeView = treeViewSupplier.get(); // 🔁 always get fresh reference

        query = query.trim().toLowerCase();
        searchResults.clear();
        currentIndex = -1;

        if (query.isEmpty()) {
            statusLabel.setText("Please enter a search term.");
            return false;
        }

        TreeItem<ANode> root = treeView.getRoot();
        lastSearchRoot = root;
        findMatches(root, query);

        if (!searchResults.isEmpty()) {
            currentIndex = 0;
            selectItem(treeView, searchResults.get(currentIndex));
            statusLabel.setText("Found " + searchResults.size() + " matches");
            return true;
        } else {
            statusLabel.setText("No match found for: \"" + query + "\"");
            return false;
        }
    }

    public void showFirst(String query) {
        TreeView<ANode> treeView = treeViewSupplier.get();
        if (treeRootChanged(treeView) || searchResults.isEmpty()) {
            if (!search(query)) return;
        }

        currentIndex = 0;
        selectItem(treeView, searchResults.get(currentIndex));
        statusLabel.setText("Match 1 of " + searchResults.size());
    }

    public void showNext(String query) {
        TreeView<ANode> treeView = treeViewSupplier.get();
        if (treeRootChanged(treeView) || searchResults.isEmpty()) {
            if (!search(query)) return;
        }

        currentIndex = (currentIndex + 1) % searchResults.size();
        selectItem(treeView, searchResults.get(currentIndex));
        statusLabel.setText("Match " + (currentIndex + 1) + " of " + searchResults.size());
    }

    public void selectAll(String query) {
        TreeView<ANode> treeView = treeViewSupplier.get();
        if (treeRootChanged(treeView) || searchResults.isEmpty()) {
            if (!search(query)) return;
        }

        MultipleSelectionModel<TreeItem<ANode>> model = treeView.getSelectionModel();
        model.clearSelection();

        for (TreeItem<ANode> item : searchResults) {
            model.select(item);
        }

        statusLabel.setText(searchResults.size() + " matches selected");
    }

    private void findMatches(TreeItem<ANode> root, String query) {
        if (root.getValue().name().toLowerCase().contains(query)) {
            searchResults.add(root);
        }
        for (TreeItem<ANode> child : root.getChildren()) {
            findMatches(child, query);
        }
    }

    private void selectItem(TreeView<ANode> treeView, TreeItem<ANode> item) {
        MultipleSelectionModel<TreeItem<ANode>> model = treeView.getSelectionModel();
        model.clearSelection();
        model.select(item);

        Platform.runLater(() -> treeView.scrollTo(treeView.getRow(item)));
    }

    private boolean treeRootChanged(TreeView<ANode> treeView) {
        return treeView.getRoot() != lastSearchRoot;
    }
}
