package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.ArrayList;
import java.util.List;

public class TreeSearchHandler {

    private final TreeView<ANode> treeView;
    private final Label statusLabel;

    private final List<TreeItem<ANode>> searchResults = new ArrayList<>();
    private int currentIndex = -1;

    public TreeSearchHandler(TreeView<ANode> treeView, Label statusLabel) {
        this.treeView = treeView;
        this.statusLabel = statusLabel;
    }

    public boolean search(String query) {
        query = query.trim().toLowerCase();
        searchResults.clear();
        currentIndex = -1;

        if (query.isEmpty()) {
            statusLabel.setText("Please enter a search term.");
            return false;
        }

        findMatches(treeView.getRoot(), query);

        if (!searchResults.isEmpty()) {
            currentIndex = 0;
            selectItem(searchResults.get(currentIndex));
            statusLabel.setText("Found " + searchResults.size() + " matches");
            return true;
        } else {
            statusLabel.setText("No match found for: \"" + query + "\"");
            return false;
        }
    }

    public void showFirst(String query) {
        if (searchResults.isEmpty() && !search(query)) return;

        currentIndex = 0;
        selectItem(searchResults.get(currentIndex));
        statusLabel.setText("Match 1 of " + searchResults.size());
    }

    public void showNext(String query) {
        if (searchResults.isEmpty() && !search(query)) return;

        currentIndex = (currentIndex + 1) % searchResults.size();
        selectItem(searchResults.get(currentIndex));
        statusLabel.setText("Match " + (currentIndex + 1) + " of " + searchResults.size());
    }

    public void selectAll(String query) {
        if (searchResults.isEmpty() && !search(query)) return;

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

    private void selectItem(TreeItem<ANode> item) {
        MultipleSelectionModel<TreeItem<ANode>> model = treeView.getSelectionModel();
        model.clearSelection();
        model.select(item);
        Platform.runLater(() -> treeView.scrollTo(treeView.getRow(item)));
    }
}
