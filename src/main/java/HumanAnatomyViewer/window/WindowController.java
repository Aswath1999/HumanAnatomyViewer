package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

public class WindowController {

    @FXML private Button expandButton, collapseButton, findButton;
    @FXML private Button firstButton, nextButton, allButton;
    @FXML private Button isAButton, partOfButton;
    @FXML private Button selectButton, deselectButton;
    @FXML private Button showButton, hideButton;

    @FXML private TextField searchTextField;
    @FXML private TreeView<ANode> treeView; // ✅ CORRECT

    @FXML private ColorPicker colorPicker;
    @FXML private Pane visualizationPane;

    private WindowPresenter presenter;

    public void initializePresenter(WindowPresenter presenter) {
        this.presenter = presenter;
    }


    // FXML event handlers — will delegate to presenter
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
    @FXML private Label searchStatusLabel;




    // === Getters for Presenter Access ===
    public Button getExpandButton() { return expandButton; }
    public Button getCollapseButton() { return collapseButton; }
    public Button getFindButton() { return findButton; }
    public Button getFirstButton() { return firstButton; }
    public Button getNextButton() { return nextButton; }
    public Button getAllButton() { return allButton; }
    public Button getIsAButton() { return isAButton; }
    public Button getPartOfButton() { return partOfButton; }
    public Button getSelectButton() { return selectButton; }
    public Button getDeselectButton() { return deselectButton; }
    public Button getShowButton() { return showButton; }
    public Button getHideButton() { return hideButton; }

    public TextField getSearchTextField() { return searchTextField; }

    public TreeView<ANode> getTreeView() { return treeView; }

    public ColorPicker getColorPicker() { return colorPicker; }

    public Pane getVisualizationPane() { return visualizationPane; }


    public Label getSearchStatusLabel() { return searchStatusLabel; }

}
