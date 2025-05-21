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

    private WindowPresenter presenter;       // Reference to presenter (used for logic like saving)
    private boolean currentlyExpanded = true; // ✅ Start in expanded mode (custom height for better spacing)

    // FXML-injected UI elements
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

    private ANode root;  // Reference to root of the tree (must be set externally before drawing)

    /**
     * Sets the presenter that will handle business logic like saving.
     */
    public void setPresenter(WindowPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Handles the save menu action. Delegates to presenter to save tree in Newick format.
     */
    @FXML
    void handleSave(ActionEvent event) {
        if (presenter != null) {
            presenter.saveAsNewick(root);
        }
    }

    /**
     * Called automatically after FXML is loaded.
     * Initializes layout toggle behavior and sets default layout mode.
     */
    public void initialize() {
        // Group the two layout mode radio buttons
        ToggleGroup layoutToggle = new ToggleGroup();
        equalDepthRadio.setToggleGroup(layoutToggle);
        uniformLengthRadio.setToggleGroup(layoutToggle);

        // ✅ Start with uniform edge length layout as default
        uniformLengthRadio.setSelected(true);

        // Redraw cladogram whenever layout mode changes
        layoutToggle.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            drawCladogram(currentlyExpanded); // Preserve current expand/collapse setting
        });
    }

    /**
     * Draws the cladogram based on the current root node and layout settings.
     *
     * @param expand whether to expand the vertical space based on number of leaves
     */
    private void drawCladogram(boolean expand) {
        if (root == null) return; // Nothing to draw

        // Choose layout algorithm based on selected radio button
        Map<ANode, Point2D> layout = equalDepthRadio.isSelected()
                ? Cladogram.layoutEqualLeafDepth(root)
                : Cladogram.layoutUniformEdgeLength(root);

        // Determine drawing area size
        double width, height;
        if (expand) {
            // Expanded mode: wide with height based on number of leaves
            width = scrollPane.getViewportBounds().getWidth() - 400;
            int leafCount = (int) layout.keySet().stream().filter(n -> n.children().isEmpty()).count();
            height = leafCount * 16;
        } else {
            // Fit mode: fit to scrollpane viewport
            width = scrollPane.getViewportBounds().getWidth();
            height = scrollPane.getViewportBounds().getHeight();
        }

        // Create the drawing (a JavaFX Group) and insert into the StackPane
        Group drawing = DrawCladogram.apply(root, layout, width, height);
        stackPane.getChildren().setAll(drawing); // Replace old drawing

        // Update UI labels with counts
        int nodeCount = layout.size();
        int edgeCount = layout.keySet().stream().mapToInt(n -> n.children().size()).sum();
        int leafCount = (int) layout.keySet().stream().filter(n -> n.children().isEmpty()).count();

        nodeCountLabel.setText(String.valueOf(nodeCount));
        edgeCountLabel.setText(String.valueOf(edgeCount));
        leafCountLabel.setText(String.valueOf(leafCount));
    }

    /**
     * Handles "Fit" button click: shrinks drawing to fit current viewport.
     */
    @FXML
    void handleFit(ActionEvent event) {
        currentlyExpanded = false;
        drawCladogram(false);
    }

    /**
     * Handles "Expand" button click: expands height to space out leaves.
     */
    @FXML
    void handleExpand(ActionEvent event) {
        currentlyExpanded = true;
        drawCladogram(true);
    }

    /**
     * Handles the "Close" menu item. Closes the window.
     */
    @FXML
    void handleClose(ActionEvent event) {
        Stage stage = (Stage) stackPane.getScene().getWindow();
        stage.close();
    }

    /**
     * Toggles full screen mode on or off.
     */
    @FXML
    void handleToggleFullScreen(ActionEvent event) {
        Scene scene = stackPane.getScene();
        Stage stage = (Stage) scene.getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    /**
     * Sets the root node for the tree and triggers initial drawing.
     * This must be called after the controller is fully loaded.
     */
    public void setRoot(ANode root) {
        this.root = root;

        // Use JavaFX's runLater to ensure the UI is ready before drawing
        javafx.application.Platform.runLater(() -> drawCladogram(currentlyExpanded));
    }
}
