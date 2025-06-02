package assignment4.window;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.StackPane;

public class WindowController {

    @FXML
    private MenuItem closeMenu;

    @FXML
    private Group edgeGroup;

    @FXML
    private RadioButton equalButton;

    @FXML
    private Button expandButton;

    @FXML
    private MenuItem expandMenu;

    @FXML
    private Button fitButton;

    @FXML
    private MenuItem fitMenu;

    @FXML
    private Group labelGroup;

    @FXML
    private Label labelHBox;

    @FXML
    private MenuItem newickMenu;

    @FXML
    private Group nodeGroup;

    @FXML
    private MenuItem screenMenu;

    @FXML
    private StackPane stackPane;

    @FXML
    private Group treeGroup;

    @FXML
    private RadioButton uniformButton;

    @FXML
    private javafx.scene.control.ScrollPane scrollPane;


    // getter
    public javafx.scene.control.ScrollPane getScrollPane() {
        return scrollPane;
    }

    public RadioButton getUniformButton() {
        return uniformButton;
    }

    public Group getTreeGroup() {
        return treeGroup;
    }

    public StackPane getStackPane() {
        return stackPane;
    }

    public MenuItem getScreenMenu() {
        return screenMenu;
    }

    public Group getNodeGroup() {
        return nodeGroup;
    }

    public MenuItem getNewickMenu() {
        return newickMenu;
    }

    public Label getLabelHBox() {
        return labelHBox;
    }

    public Group getLabelGroup() {
        return labelGroup;
    }

    public MenuItem getFitMenu() {
        return fitMenu;
    }

    public Button getFitButton() {
        return fitButton;
    }

    public MenuItem getExpandMenu() {
        return expandMenu;
    }

    public Button getExpandButton() {
        return expandButton;
    }

    public RadioButton getEqualButton() {
        return equalButton;
    }

    public Group getEdgeGroup() {
        return edgeGroup;
    }

    public MenuItem getCloseMenu() {
        return closeMenu;
    }
}
