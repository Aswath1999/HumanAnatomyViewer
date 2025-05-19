package assignment4.window;

import assignment4.model.ANode;
import assignment4.model.Cladogram;
import assignment4.view.DrawCladogram;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Map;

public class WindowController {
    private WindowPresenter presenter;
    private boolean currentlyExpanded = true; // ✅ Start in expanded mode

    @FXML private MenuItem closeMenuItem;
    @FXML private Label edgeCountLabel;
    @FXML private Label nodeCountLabel;
    @FXML private Label leafCountLabel;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem toggleFullScreenMenuItem;
    @FXML private RadioButton equalDepthRadio;
    @FXML private RadioButton uniformLengthRadio;
    @FXML private Button fitButton;
    @FXML private Button expandButton;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane stackPane;

    private ANode root;  // Tree root (you must set this before drawing)
    public void setPresenter(WindowPresenter presenter) {
        this.presenter = presenter;
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (presenter != null) {
            presenter.saveAsNewick(root);
        }
    }
    public void initialize() {
        ToggleGroup layoutToggle = new ToggleGroup();
        equalDepthRadio.setToggleGroup(layoutToggle);
        uniformLengthRadio.setToggleGroup(layoutToggle);
        uniformLengthRadio.setSelected(true); // ✅ Start with Uniform layout

        // React to layout changes immediately
        layoutToggle.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            drawCladogram(currentlyExpanded); // Redraw with last-used scale mode
        });
    }



    private void drawCladogram(boolean expand) {
        if (root == null) return;

        // Choose layout
        Map<ANode, Point2D> layout = equalDepthRadio.isSelected()
                ? Cladogram.layoutEqualLeafDepth(root)
                : Cladogram.layoutUniformEdgeLength(root);

        // Choose drawing area size
        double width, height;
        if (expand) {
            width = scrollPane.getViewportBounds().getWidth() - 400;
            int leafCount = (int) layout.keySet().stream().filter(n -> n.children().isEmpty()).count();
            height = leafCount * 16;
        } else {
            width = scrollPane.getViewportBounds().getWidth();
            height = scrollPane.getViewportBounds().getHeight();
        }

        // Draw and update StackPane
        Group drawing = DrawCladogram.apply(root, layout, width, height);
        stackPane.getChildren().setAll(drawing);

        // Update counts
        int nodeCount = layout.size();
        int edgeCount = layout.keySet().stream().mapToInt(n -> n.children().size()).sum();
        int leafCount = (int) layout.keySet().stream().filter(n -> n.children().isEmpty()).count();

        nodeCountLabel.setText(String.valueOf(nodeCount));
        edgeCountLabel.setText(String.valueOf(edgeCount));
        leafCountLabel.setText(String.valueOf(leafCount));
    }

    @FXML
    void handleFit(ActionEvent event) {
        currentlyExpanded = false;
        drawCladogram(false);
    }

    @FXML
    void handleExpand(ActionEvent event) {
        currentlyExpanded = true;
        drawCladogram(true);
    }


    @FXML
    void handleClose(ActionEvent event) {
        Stage stage = (Stage) stackPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleToggleFullScreen(ActionEvent event) {
        Scene scene = stackPane.getScene();
        Stage stage = (Stage) scene.getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }


    public void setRoot(ANode root) {
        this.root = root;

        // Defer initial drawing until after scene is shown and layout is ready
        javafx.application.Platform.runLater(() -> drawCladogram(currentlyExpanded));
    }



}
