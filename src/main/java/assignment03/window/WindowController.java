package assignment03.window;

import assignment03.model.ANode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;

/**
 * Controller class for the main application window.
 *
 * - Automatically hooked into the FXML via the `fx:controller` attribute.
 * - Uses @FXML annotations to bind to UI components defined in the FXML file.
 * - Connects UI actions (button/menu clicks) to event handler methods.
 * - Delegates logic to a WindowPresenter (following MVP pattern).
 */
public class WindowController {

    // Reference to presenter (business logic handler)
    private WindowPresenter presenter;

    // =====================
    // UI Components injected by FXML
    // These fields are automatically initialized by FXMLLoader
    // =====================
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

    // =====================
    // Event Handlers (triggered by FXML UI interactions)
    // These methods are called when user clicks buttons or menu items
    // =====================

    @FXML private void onExpandAll() {
        presenter.handleExpand(); // Delegates to presenter to expand all nodes
    }

    @FXML private void onCollapseAll() {
        presenter.handleCollapse(); // Collapse all nodes in the TreeView
    }

    @FXML private void onSelectAll() {
        presenter.handleSelectAll(); // Select all nodes or word cloud items
    }

    @FXML private void onSelectNone() {
        presenter.handleSelectNone(); // Deselect all nodes or word cloud items
    }

    @FXML private void onClose() {
        System.exit(0); // Terminates the application (can use Platform.exit() instead for JavaFX cleanup)
    }

    // =====================
    // Presenter Initialization
    // Called externally to wire up the presenter logic to this controller
    // =====================
    public void initializePresenter(WindowPresenter presenter) {
        this.presenter = presenter;
    }

    // =====================
    // Getters to expose UI components to the presenter
    // These allow the presenter to interact with the scene graph
    // =====================
    public TreeView<ANode> getAnatomyTreeView() { return anatomyTreeView; }
    public FlowPane getWordCloudPane() { return wordCloudPane; }

    public MenuItem getAboutMenuItem() { return aboutMenuItem; }
    public MenuItem getCloseMenuItem() { return closeMenuItem; }

    public Button getCollapseAllButton() { return collapseAllButton; }
    public MenuItem getCollapseAllMenuItem() { return collapseAllMenuItem; }

    public Button getExpandAllButton() { return expandAllButton; }
    public MenuItem getExpandAllMenuItem() { return expandAllMenuItem; }

    public Button getSelectAllButton() { return selectAllButton; }
    public MenuItem getSelectAllMenuItem() { return selectAllMenuItem; }

    public Button getSelectNoneButton() { return selectNoneButton; }
    public MenuItem getSelectNoneMenuItem() { return selectNoneMenuItem; }
}
