package assignment03.window;

import assignment03.model.ANode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;

/**
 * The controller connects FXML UI elements to event handlers.
 * It exposes references for the presenter to interact with the scene graph.
 */
public class WindowController {

    private WindowPresenter presenter;

    // Injected by FXML
    @FXML private MenuItem aboutMenuItem;
    @FXML private TreeView<ANode> anatomyTreeView;
    @FXML private MenuItem closeMenuItem;
    @FXML private Button collapseAllButton;
    @FXML private MenuItem collapseAllMenuItem;
    @FXML private Button expandAllButton;
    @FXML private MenuItem expandAllMenuItem;
    @FXML private Button selectAllButton;
    @FXML private MenuItem selectAllMenuItem;
    @FXML private Button selectNoneButton;
    @FXML private MenuItem selectNoneMenuItem;
    @FXML private FlowPane wordCloudPane;

    // Event Handlers for Buttons and Menu Items
    @FXML private void onExpandAll() {
        presenter.handleExpand();
    }

    @FXML private void onCollapseAll() {
        presenter.handleCollapse();
    }

    @FXML private void onSelectAll() {
        presenter.handleSelectAll();
    }

    @FXML private void onSelectNone() {
        presenter.handleSelectNone();
    }
    @FXML
    private void onClose() {
        System.exit(0); // Or Platform.exit(); if preferred
    }


    // Initialization hook for presenter
    public void initializePresenter(WindowPresenter presenter) {
        this.presenter = presenter;
    }

    // Getters for presenter
    public TreeView<ANode> getAnatomyTreeView() { return anatomyTreeView; }
    public FlowPane getWordCloudPane() { return wordCloudPane; }

    public MenuItem getAboutMenuItem() { return aboutMenuItem; }
    public MenuItem getCloseMenuItem() { return closeMenuItem; }
    public Button getCollapseAllButton() { return collapseAllButton; }
    public MenuItem getCollapseAllMenuItem() { return collapseAllMenuItem; }
    public MenuItem getExpandAllMenuItem() { return expandAllMenuItem; }
    public Button getExpandAllButton() { return expandAllButton; }
    public Button getSelectAllButton() { return selectAllButton; }
    public MenuItem getSelectAllMenuItem() { return selectAllMenuItem; }
    public Button getSelectNoneButton() { return selectNoneButton; }
    public MenuItem getSelectNoneMenuItem() { return selectNoneMenuItem; }
}
