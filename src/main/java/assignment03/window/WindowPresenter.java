package assignment03.window;

import assignment03.model.ANode;
import assignment03.model.Model;
import assignment03.model.WordCloudItem;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter class in MVP pattern.
 * Manages interactions between the view (UI) and the model.
 * Handles tree loading, selection, word cloud updates, expand/collapse/select functionality.
 */
public class WindowPresenter {

    private final TreeView<ANode> treeView;
    private final WindowController controller;

    /**
     * Initializes the presenter, sets up the tree view and listeners.
     */
    public WindowPresenter(Stage stage, WindowController controller, Model model) {
        this.controller = controller;
        this.treeView = controller.getAnatomyTreeView();

        // Initialize the tree with the part-of root node
        TreeViewSetup.setup(treeView, model.getPartOfRoot());

        // Enable multiple selection
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Update word cloud when selection changes
        treeView.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<TreeItem<ANode>>) change -> updateWordCloud()
        );
    }

    // ----------------------------------
    // WORD CLOUD LOGIC
    // ----------------------------------

    /**
     * Updates the word cloud based on the selected tree items.
     * Extracts words, computes frequencies, and scales text nodes in the FlowPane.
     */
    private void updateWordCloud() {
        List<String> words = new ArrayList<>();

        for (TreeItem<ANode> item : treeView.getSelectionModel().getSelectedItems()) {
            if (item != null && item.getValue() != null) {
                String[] tokens = item.getValue().name().split("\\W+");
                for (String word : tokens) {
                    if (!word.isBlank()) {
                        words.add(word.toLowerCase());
                    }
                }
            }
        }

        var cloudItems = WordCloudItem.computeItems(new ArrayList<>(words));

        var flowPane = controller.getWordCloudPane();
        flowPane.getChildren().clear();
        for (var item : cloudItems) {
            Text text = new Text(item.word());
            text.setFont(Font.font(64 * item.relativeHeight()));
            flowPane.getChildren().add(text);
        }
    }

    // ----------------------------------
    // EXPAND / COLLAPSE LOGIC
    // ----------------------------------

    /**
     * Expands all nodes if nothing is selected; otherwise expands selected nodes and descendants.
     */
    public void handleExpand() {
        var selected = treeView.getSelectionModel().getSelectedItems();
        System.out.println("Expand triggered. Selected count: " + treeView.getSelectionModel().getSelectedItems().size());
        if (selected.isEmpty()) {
            expand(treeView.getRoot());
        } else {
            for (TreeItem<ANode> item : selected) {
                expand(item);
                System.out.println("Expanding node: " + item.getValue().name());

            }
        }
    }

    /**
     * Collapses all nodes if nothing is selected; otherwise collapses selected nodes and descendants.
     */
    public void handleCollapse() {
        var selected = treeView.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            collapse(treeView.getRoot());
        } else {
            for (TreeItem<ANode> item : selected) {
                collapse(item);
            }
        }
    }

    /**
     * Recursively expands a TreeItem and all its children.
     */
    private void expand(TreeItem<ANode> item) {
        if (item != null) {
            item.setExpanded(true);
            for (TreeItem<ANode> child : item.getChildren()) {
                expand(child);
            }
        }
    }

    /**
     * Recursively collapses a TreeItem and all its children.
     */
    private void collapse(TreeItem<ANode> item) {
        if (item != null) {
            for (TreeItem<ANode> child : item.getChildren()) {
                collapse(child);
            }
            item.setExpanded(false);
        }
    }

    // ----------------------------------
    // SELECT LOGIC
    // ----------------------------------

    /**
     * Selects all expanded nodes if none are selected;
     * otherwise selects all descendants of the selected nodes.
     */
    public void handleSelectAll() {
        var selectionModel = treeView.getSelectionModel();
        List<TreeItem<ANode>> selected = selectionModel.getSelectedItems();

        if (selected.isEmpty()) {
            List<TreeItem<ANode>> expanded = new ArrayList<>();
            collectExpanded(treeView.getRoot(), expanded);
            selectionModel.clearSelection();
            expanded.forEach(selectionModel::select);
        } else {
            selectionModel.clearSelection();
            for (TreeItem<ANode> item : selected) {
                List<TreeItem<ANode>> descendants = new ArrayList<>();
                collectDescendants(item, descendants);
                descendants.forEach(selectionModel::select);
            }
        }
    }

    /**
     * Clears all selections in the tree view.
     */
    public void handleSelectNone() {
        treeView.getSelectionModel().clearSelection();
    }

    /**
     * Recursively collects all expanded nodes starting from a given item.
     */
    private void collectExpanded(TreeItem<ANode> item, List<TreeItem<ANode>> expanded) {
        if (item.isExpanded()) {
            expanded.add(item);
            for (TreeItem<ANode> child : item.getChildren()) {
                collectExpanded(child, expanded);
            }
        }
    }

    /**
     * Recursively collects all descendants of a given TreeItem.
     */
    private void collectDescendants(TreeItem<ANode> item, List<TreeItem<ANode>> list) {
        for (TreeItem<ANode> child : item.getChildren()) {
            list.add(child);
            collectDescendants(child, list);
        }
    }
}
