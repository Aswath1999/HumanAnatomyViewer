package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import HumanAnatomyViewer.model.Model;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.*;

public class WindowPresenter {

    // === Core application components ===
    private final Stage stage;                        // JavaFX window stage
    private final WindowController controller;        // Provides access to all FXML GUI elements
    private final Model model;                        // Logical data model (tree hierarchy, file structure)

    // === Tree structure ===
    private TreeItem<ANode> partOfRootItem;           // Tree root for "part-of" hierarchy
    private TreeItem<ANode> isARootItem;              // Tree root for "is-a" hierarchy

    // === 3D content containers ===
    private final Group innerGroup = new Group();     // Holds models; can be centered
    private final Group contentGroup = new Group();   // Wraps innerGroup; rotates with mouse
    private final Group root3D = new Group();         // Full 3D scene contents (models + lights)
    private final PerspectiveCamera camera = new PerspectiveCamera(true); // 3D camera

    // === Logic handlers ===
    private SceneInteractionHandler interactionHandler;   // Handles mouse/keyboard interaction
    private ModelInterface modelInterface;                // Handles model loading/display/styling
    private TreeSearchHandler searchHandler;              // Handles search in the tree

    /**
     * Constructor initializes components and handlers.
     */
    public WindowPresenter(Stage stage, WindowController controller, Model model) {
        this.stage = stage;
        this.controller = controller;
        this.model = model;

        // Pass active TreeView (based on selected tab) — used for selection sync & model highlighting
        this.modelInterface = new ModelInterface(innerGroup, controller.getActiveTreeView());
        this.searchHandler = new TreeSearchHandler(controller::getActiveTreeView, controller.getSearchStatusLabel());

        initializeTrees();               // Build and assign the tree structure
        setupTreeSelectionListener();    // Connect selection events to 3D model logic
        setupButtonHandlers();           // Wire up UI buttons to behavior
    }

    /**
     * Builds both tree hierarchies from the model and assigns them to the TreeView.
     */
    private void initializeTrees() {
        partOfRootItem = buildTreeItem(model.getPartOfRoot());
        isARootItem = buildTreeItem(model.getIsARoot());

        controller.getPartOfTreeView().setRoot(partOfRootItem);
        controller.getIsATreeView().setRoot(isARootItem);

        controller.getPartOfTreeView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        controller.getIsATreeView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }

    /**
     * Recursively converts an ANode tree into a TreeItem tree.
     */
    private TreeItem<ANode> buildTreeItem(ANode node) {
        TreeItem<ANode> item = new TreeItem<>(node);
        for (ANode child : node.children()) {
            item.getChildren().add(buildTreeItem(child));
        }
        item.setExpanded(false);  // Tree items are collapsed by default
        return item;
    }

    /**
     * Adds a listener to the TreeView selection to update 3D model selection accordingly.
     */
    private void setupTreeSelectionListener() {
        ListChangeListener<TreeItem<ANode>> listener = change -> {
            if (modelInterface.isInSelectionUpdate()) return;

            modelInterface.getSelectedFileIds().clear();
            for (TreeItem<ANode> item : controller.getActiveTreeView().getSelectionModel().getSelectedItems()) {
                if (item.getValue() != null) {
                    modelInterface.getSelectedFileIds().addAll(item.getValue().fileIds());
                }
            }
            modelInterface.applyDrawModeBasedOnSelection();
        };

        controller.getPartOfTreeView().getSelectionModel().getSelectedItems().addListener(listener);
        controller.getIsATreeView().getSelectionModel().getSelectedItems().addListener(listener);
    }


    /**
     * Sets up all button event handlers using controller’s GUI elements.
     */
    private void setupButtonHandlers() {

        controller.getExpandButton().setOnAction(e -> expandSelected());
        controller.getCollapseButton().setOnAction(e -> collapseSelected());
        controller.getSelectButton().setOnAction(e -> selectAllDescendants());
        controller.getDeselectButton().setOnAction(e ->
                controller.getActiveTreeView().getSelectionModel().clearSelection());

        controller.getShowButton().setOnAction(e -> handleShow());
        controller.getHideButton().setOnAction(e ->
                modelInterface.hideModels(controller.getActiveTreeView().getSelectionModel().getSelectedItems()));

        controller.getColorPicker().setOnAction(e -> modelInterface.applyColorToSelected(controller.getColorPicker().getValue()));

        // Search-related buttons
        controller.getFindButton().setOnAction(e -> handleFind());
        controller.getFirstButton().setOnAction(e -> handleFirst());
        controller.getNextButton().setOnAction(e -> handleNext());
        controller.getAllButton().setOnAction(e -> handleAll());
        controller.getSearchTextField().setOnAction(e -> handleFind());
    }

    // ==== Tree manipulation helpers ====

    private void expandSelected() {
        controller.getActiveTreeView().getSelectionModel().getSelectedItems().forEach(this::expand);
    }

    private void collapseSelected() {
        controller.getActiveTreeView().getSelectionModel().getSelectedItems().forEach(this::collapse);
    }


    private void expand(TreeItem<ANode> item) {
        item.setExpanded(true);
        item.getChildren().forEach(this::expand);
    }

    private void collapse(TreeItem<ANode> item) {
        item.getChildren().forEach(this::collapse);
        item.setExpanded(false);
    }

    /**
     * Selects all descendants of the currently selected nodes.
     */
    private void selectAllDescendants() {
        TreeView<ANode> tree = controller.getActiveTreeView();
        var model = tree.getSelectionModel();
        List<TreeItem<ANode>> base = new ArrayList<>(model.getSelectedItems());
        model.clearSelection();

        for (TreeItem<ANode> item : base) {
            model.select(item);
            collectDescendants(item, model);
        }
    }

    private void collectDescendants(TreeItem<ANode> parent, MultipleSelectionModel<TreeItem<ANode>> model) {
        for (TreeItem<ANode> child : parent.getChildren()) {
            model.select(child);
            collectDescendants(child, model);
        }
    }

    // ==== 3D scene setup and display ====

    private void handleShow() {
        modelInterface.loadAndDisplayModels(controller.getActiveTreeView().getSelectionModel().getSelectedItems());

        Platform.runLater(() -> {
            innerGroup.applyCss();
            innerGroup.layout();
            centerContentGroup();
            autoAdjustCamera();
        });

        setup3DScene(); // Only sets up once
    }

    /**
     * Centers the innerGroup around origin by translating its center to (0,0,0).
     */
    private void centerContentGroup() {
        innerGroup.layout();
        Bounds bounds = innerGroup.getBoundsInParent();
        double cx = (bounds.getMinX() + bounds.getMaxX()) / 2;
        double cy = (bounds.getMinY() + bounds.getMaxY()) / 2;
        double cz = (bounds.getMinZ() + bounds.getMaxZ()) / 2;
        innerGroup.getTransforms().setAll(new Translate(-cx, -cy, -cz));
    }

    /**
     * Moves the camera back based on model size so the whole model fits in view.
     */
    private void autoAdjustCamera() {
        innerGroup.applyCss();
        innerGroup.layout();
        Bounds bounds = innerGroup.getBoundsInParent();
        double maxDim = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-maxDim * 2.2); // Pull camera far enough back
    }

    /**
     * Sets up the 3D scene (lighting, camera, interaction).
     */
    private void setup3DScene() {
        if (root3D.getChildren().isEmpty()) {
            contentGroup.getChildren().add(innerGroup);
            root3D.getChildren().add(contentGroup);

            // Add lighting
            PointLight pointLight = new PointLight(Color.WHITE);
            pointLight.setTranslateX(-500);
            pointLight.setTranslateY(-500);
            pointLight.setTranslateZ(-500);

            AmbientLight ambientLight = new AmbientLight(Color.DARKGRAY);
            root3D.getChildren().addAll(pointLight, ambientLight);

            // Camera configuration
            camera.setNearClip(0.1);
            camera.setFarClip(10000);
            camera.setTranslateZ(-500);

            // Create SubScene for 3D rendering
            SubScene subScene = new SubScene(root3D, 600, 600, true, SceneAntialiasing.BALANCED);
            subScene.setCamera(camera);
            subScene.setFill(Color.LIGHTGRAY);
            subScene.setOnMouseClicked(e -> subScene.requestFocus());

            // Keyboard shortcuts for zoom/rotate/reset
            subScene.setOnKeyPressed(e -> {
                KeyCode code = e.getCode();
                switch (code) {
                    case Z -> interactionHandler.resetTransform();
                    case I -> interactionHandler.zoom(50);
                    case O -> interactionHandler.zoom(-50);
                    case LEFT -> interactionHandler.rotateY(-10);
                    case RIGHT -> interactionHandler.rotateY(10);
                    case UP -> interactionHandler.rotateX(-10);
                    case DOWN -> interactionHandler.rotateX(10);
                }
            });

            // Make 3D canvas resize with window
            subScene.widthProperty().bind(controller.getVisualizationPane().widthProperty());
            subScene.heightProperty().bind(controller.getVisualizationPane().heightProperty());
            controller.getVisualizationPane().getChildren().setAll(subScene);

            // Enable mouse interaction
            interactionHandler = new SceneInteractionHandler(contentGroup, camera);
            interactionHandler.setupMouseInteraction(controller.getVisualizationPane());
        }
    }

    // ==== Search functions (delegated to TreeSearchHandler) ====

    public void handleFind() {
        String query = controller.getSearchTextField().getText();
        searchHandler.search(query);
    }

    public void handleFirst() {
        String query = controller.getSearchTextField().getText();
        searchHandler.showFirst(query);
    }

    public void handleNext() {
        String query = controller.getSearchTextField().getText();
        searchHandler.showNext(query);
    }

    public void handleAll() {
        String query = controller.getSearchTextField().getText();
        searchHandler.selectAll(query);
    }
}
