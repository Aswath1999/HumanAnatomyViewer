package assignment4.window;

import assignment4.model.ANode;
import assignment4.model.Cladogram;
import assignment4.model.NewickExporter;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;


public class WindowPresenter {
    private final WindowController controller;
    private final ANode root;
    private final TreeViewSetup treeViewSetup;
    private final Stage stage;

    // Track current layout mode: "fit" or "expand", initially null (no drawing)
    private String currentMode = null;

    // constructor
    public WindowPresenter(Stage stage, WindowController controller, ANode root) {
        this.controller = controller;
        this.root = root;
        this.treeViewSetup = new TreeViewSetup(controller);
        this.stage = stage;

        // create groups for buttons to only be able to choose one at a time
        ToggleGroup layoutToggle = new ToggleGroup();
        controller.getEqualButton().setToggleGroup(layoutToggle);
        controller.getUniformButton().setToggleGroup(layoutToggle);
        // set up the action for the menu bar
        controller.getCloseMenu().setOnAction(e -> Platform.exit());
        controller.getFitMenu().setOnAction(e -> {currentMode="fit";drawSelectedLayoutFit();});
        controller.getExpandMenu().setOnAction(e -> {currentMode="expand";drawSelectedLayoutExpand();});
        controller.getScreenMenu().setOnAction(e -> toggleFullscreen());

        // Set a default selection
        controller.getEqualButton().setSelected(true);

        // Hook up button actions
        controller.getCloseMenu().setOnAction(e -> Platform.exit());
        controller.getFitButton().setOnAction(e -> {currentMode="fit";drawSelectedLayoutFit();});
        controller.getExpandButton().setOnAction(e -> {currentMode="expand";drawSelectedLayoutExpand();});

        // On toggle layout buttons (equal/uniform), redraw if a mode is already selected
        controller.getEqualButton().setOnAction(e -> {
            if (currentMode != null) {
                if (currentMode.equals("fit")) {
                    drawSelectedLayoutFit();
                } else if (currentMode.equals("expand")) {
                    drawSelectedLayoutExpand();
                }
            }
        });
        controller.getUniformButton().setOnAction(e -> {
            if (currentMode != null) {
                if (currentMode.equals("fit")) {
                    drawSelectedLayoutFit();
                } else if (currentMode.equals("expand")) {
                    drawSelectedLayoutExpand();
                }
            }
        });

        // controller for Newick format
        controller.getNewickMenu().setOnAction(e -> {
            String newickString = NewickExporter.toNewick(root);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Newick File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            fileChooser.setInitialFileName("tree.newick.txt");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(newickString);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Save Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Newick tree saved successfully!");
                    alert.showAndWait();
                } catch (IOException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Save Error");
                    alert.setHeaderText("Could not save file");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        clearTreeView();
        controller.getLabelHBox().setText("Nodes: 0, Edges: 0, Leaves: 0"); // clear info label

        // Add listener for fullscreen changes
        stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { // Entering fullscreen
                Platform.runLater(() -> {
                    if (currentMode != null) {
                        if (currentMode.equals("fit")) {
                            drawSelectedLayoutFit(); // Redraw in fit mode
                        } else if (currentMode.equals("expand")) {
                            drawSelectedLayoutExpand(); // Redraw in expand mode
                        }
                    }
                });
            } else { // Exiting fullscreen
                Platform.runLater(() -> {
                    if (currentMode != null) {
                        if (currentMode.equals("fit")) {
                            drawSelectedLayoutFit(); // Redraw in fit mode
                        } else if (currentMode.equals("expand")) {
                            drawSelectedLayoutExpand(); // Redraw in expand mode
                        }
                    }
                });
            }
        });

    }

    // method to clear the tree view
    private void clearTreeView() {
        controller.getStackPane().getChildren().clear();
        controller.getEdgeGroup().getChildren().clear();
        controller.getNodeGroup().getChildren().clear();
        controller.getLabelGroup().getChildren().clear();

    }

    // method to count the number of leaves in the cladogram
    private int countLeaves(ANode node) {
        if (node.children().isEmpty()) return 1;
        return node.children().stream().mapToInt(this::countLeaves).sum();
    }

    // method to draw the selected layout
    private void drawSelectedLayoutFit() {
        // get the selected layout
        Map<ANode, Point2D> layout = getSelectedLayout();
        // get the width and height of the scroll pane
        double width = controller.getScrollPane().getViewportBounds().getWidth();
        double height = controller.getScrollPane().getViewportBounds().getHeight();
        // create the tree with the layout
        Group tree = DrawCladogram.apply(root, layout, width, height);
        controller.getStackPane().getChildren().setAll(tree);

        treeViewSetup.render(layout);
    }

    // method to draw the selected layout
    private void drawSelectedLayoutExpand() {
        // get the selected layout
        Map<ANode, Point2D> layout = getSelectedLayout();
        // get the width and height of the scroll pane
        double width = controller.getScrollPane().getViewportBounds().getWidth() - 400;
        double height = countLeaves(root) * 16;
        // create the tree with the layout
        Group tree = DrawCladogram.apply(root, layout, width, height);
        controller.getStackPane().getChildren().setAll(tree);

        treeViewSetup.render(layout);
    }

    // method to get the selected layout
    private Map<ANode, Point2D> getSelectedLayout() {
        // choose what layout to present
        if (controller.getEqualButton().isSelected()) {
            return Cladogram.layoutEqualLeafDepth(root);
        } else {
            return Cladogram.layoutUniformEdgeLength(root);
        }
    }

    // method to toggle fullscreen
    private void toggleFullscreen() {
        stage.setFullScreenExitHint("Press ESC to exit fullscreen");
        stage.setFullScreen(true);
    }

}


