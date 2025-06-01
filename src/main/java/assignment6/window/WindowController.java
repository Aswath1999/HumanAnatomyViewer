package assignment6.window;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;

public class WindowController {

    private WindowPresenter presenter;

    @FXML private Pane centerPane;

    @FXML private Button closeButton;
    @FXML private Button resetButton;
    @FXML private Button rotateDownButton;
    @FXML private Button rotateLeftButton;
    @FXML private Button rotateRightButton;
    @FXML private Button rotateUpButton;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;

    public Button getClearButton() {
        return clearButton;
    }

    @FXML
    private Button clearButton;


    @FXML private MenuItem menuClose;
    @FXML private MenuItem menuOpen;
    @FXML private MenuItem menuReset;
    @FXML private MenuItem menuRotateDown;
    @FXML private MenuItem menuRotateLeft;
    @FXML private MenuItem menuRotateRight;
    @FXML private MenuItem menuRotateUp;
    @FXML private MenuItem menuZoomIn;
    @FXML private MenuItem menuZoomOut;

/*    // Optional controller actions (used by FXML only if declared there)
    @FXML void handleClose(ActionEvent event) { }
    @FXML void handleFit(ActionEvent event) { }
    @FXML void handledownrotation(ActionEvent event) { }
    @FXML void handleleftrotation(ActionEvent event) { }
    @FXML void handlereset(ActionEvent event) { }
    @FXML void handlerightrotation(ActionEvent event) { }
    @FXML void handleuprotation(ActionEvent event) { }
    @FXML void handlezoomin(ActionEvent event) { }*/

    // === Public getters for presenter ===

    public Pane getCenterPane() {
        return centerPane;
    }

    public Button getResetButton() {
        return resetButton;
    }

    public Button getRotateDownButton() {
        return rotateDownButton;
    }

    public Button getRotateLeftButton() {
        return rotateLeftButton;
    }

    public Button getRotateRightButton() {
        return rotateRightButton;
    }

    public Button getRotateUpButton() {
        return rotateUpButton;
    }

    public Button getZoomInButton() {
        return zoomInButton;
    }

    public Button getZoomOutButton() {
        return zoomOutButton;
    }

    public MenuItem getMenuReset() {
        return menuReset;
    }

    public MenuItem getMenuZoomIn() {
        return menuZoomIn;
    }

    public MenuItem getMenuZoomOut() {
        return menuZoomOut;
    }

    public MenuItem getMenuRotateLeft() {
        return menuRotateLeft;
    }

    public MenuItem getMenuRotateRight() {
        return menuRotateRight;
    }

    public MenuItem getMenuRotateUp() {
        return menuRotateUp;
    }

    public MenuItem getMenuRotateDown() {
        return menuRotateDown;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public MenuItem getMenuClose() {
        return menuClose;
    }
    public MenuItem getMenuOpen() {
        return menuOpen;
    }





}
