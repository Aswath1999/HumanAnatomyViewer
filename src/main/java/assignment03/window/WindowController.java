package assignment03.window;
import assignment03.model.ANode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;

public class WindowController {

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private TreeView<ANode> anatomyTreeView;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private Button collapseAllButton;

    @FXML
    private MenuItem collapseAllMenuItem;

    @FXML
    private Button expandAllButton;

    @FXML
    private MenuItem expandAllMenuItem;

    @FXML
    private Button selectAllButton;

    @FXML
    private MenuItem selectAllMenuItem;

    @FXML
    private Button selectNoneButton;

    @FXML
    private MenuItem selectNoneMenuItem;

    @FXML
    private FlowPane wordCloudPane;

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public TreeView<ANode> getAnatomyTreeView() {
        return anatomyTreeView;
    }


    public MenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public Button getCollapseAllButton() {
        return collapseAllButton;
    }

    public MenuItem getCollapseAllMenuItem() {
        return collapseAllMenuItem;
    }

    public MenuItem getExpandAllMenuItem() {
        return expandAllMenuItem;
    }

    public Button getExpandAllButton() {
        return expandAllButton;
    }

    public Button getSelectAllButton() {
        return selectAllButton;
    }

    public MenuItem getSelectAllMenuItem() {
        return selectAllMenuItem;
    }

    public Button getSelectNoneButton() {
        return selectNoneButton;
    }

    public MenuItem getSelectNoneMenuItem() {
        return selectNoneMenuItem;
    }

    public FlowPane getWordCloudPane() {
        return wordCloudPane;
    }
}
