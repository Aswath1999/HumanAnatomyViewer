package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import java.io.File;

public class WindowController {

    // === Buttons ===
    @FXML private Button expandButton, collapseButton, findButton;
    @FXML private Button firstButton, nextButton, allButton;
/*    @FXML private Button isAButton, partOfButton;*/
    @FXML private Button selectButton, deselectButton;
    @FXML private Button showButton, hideButton;

    // === Text fields, labels, color ===
    @FXML private TextField searchTextField;
    @FXML private Label searchStatusLabel;
    @FXML private ColorPicker colorPicker;

    // === Tree Views and Tabs ===
    @FXML private TreeView<ANode> partOfTreeView;
    @FXML private TreeView<ANode> isATreeView;
    @FXML private TabPane treeTabPane;

    // === Visualization pane ===
    @FXML private Pane visualizationPane;

    // === Root layout for fallback access ===
    @FXML private AnchorPane rootPane;


    @FXML
    private Button undoButton;

    @FXML
    private Button redoButton;

    private File customModelDirectory = null;


    @FXML private Button aiSearchButton; // ✅ New AI search button



    @FXML private MenuItem menuLoadFiles;

    @FXML private MenuItem menuEnableDarkMode;
    @FXML private MenuItem menuToggleFullScreen;

    public MenuItem getMenuLoadFiles() {
        return menuLoadFiles;
    }

    public MenuItem getMenuEnableDarkMode() {return menuEnableDarkMode;}
    public MenuItem getMenuToggleFullScreen() {return menuToggleFullScreen;}

    private WindowPresenter presenter;

    public void initializePresenter(WindowPresenter presenter) {
        this.presenter = presenter;
    }

    // === Event handler stubs (delegated to presenter) ===
    @FXML private void handleExpand() {}
    @FXML private void handleCollapse() {}
    @FXML private void handleFind() {}
    @FXML private void handleFirst() {}
    @FXML private void handleNext() {}
    @FXML private void handleAll() {}
    @FXML private void handleIsA() {}
    @FXML private void handlePartOf() {}
    @FXML private void handleSelect() {}
    @FXML private void handleDeselect() {}
    @FXML private void handleShow() {}
    @FXML private void handleHide() {}
    @FXML
    private void handleUndo() {}

    @FXML private void handleAISearch() {} // Handler for AI search

    @FXML
    private void handleRedo() {}

    @FXML private Button explodeButton;
    public Button getExplodeButton() { return explodeButton; }




    // === Getters for presenter ===
    public Button getExpandButton() { return expandButton; }
    public Button getCollapseButton() { return collapseButton; }
    public Button getFindButton() { return findButton; }
    public Button getFirstButton() { return firstButton; }
    public Button getNextButton() { return nextButton; }
    public Button getAllButton() { return allButton; }
/*    public Button getIsAButton() { return isAButton; }
    public Button getPartOfButton() { return partOfButton; }*/
    public Button getSelectButton() { return selectButton; }
    public Button getDeselectButton() { return deselectButton; }
    public Button getShowButton() { return showButton; }
    public Button getHideButton() { return hideButton; }

    public TextField getSearchTextField() { return searchTextField; }
    public Label getSearchStatusLabel() { return searchStatusLabel; }
    public ColorPicker getColorPicker() { return colorPicker; }
    public Pane getVisualizationPane() { return visualizationPane; }

    public TreeView<ANode> getPartOfTreeView() { return partOfTreeView; }
    public TreeView<ANode> getIsATreeView() { return isATreeView; }

    public TabPane getTreeTabPane() { return treeTabPane; }
    public Button getAISearchButton() { return aiSearchButton; }

    public Button getUndoButton() {
        return undoButton;
    }

    public Button getRedoButton() {
        return redoButton;
    }

    /**
     * Returns the currently visible/active tree view based on selected tab.
     */
    public TreeView<ANode> getActiveTreeView() {
        int index = treeTabPane.getSelectionModel().getSelectedIndex();
        return (index == 0) ? partOfTreeView : isATreeView;
    }
}
