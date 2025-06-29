package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import HumanAnatomyViewer.model.Model;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * WindowPresenter handles the main logic and interaction for the application window.
 * It connects the data model, GUI (via the controller), and 3D scene rendering.
 */
public class WindowPresenter {

    // === Core application components ===
    private final Stage stage;                        // The JavaFX window (top-level stage)
    private final WindowController controller;        // FXML controller providing access to GUI components
    private final Model model;                        // The logical model storing tree hierarchy and metadata

    // === Tree structure ===
    private TreeItem<ANode> partOfRootItem;           // Root node for the "part-of" hierarchy tree
    private TreeItem<ANode> isARootItem;              // Root node for the "is-a" hierarchy tree

    // === 3D scene components ===
    private final Group innerGroup = new Group();     // Contains loaded 3D models, which can be transformed
    private final Group contentGroup = new Group();   // Wraps innerGroup and is rotated/scaled
    private final Group root3D = new Group();         // Top-level 3D group containing models and lights
    private final PerspectiveCamera camera = new PerspectiveCamera(true); // Camera for 3D scene

    // === Logic handlers ===
    private SceneInteractionHandler interactionHandler;   // Manages mouse/keyboard 3D interaction
    private ModelInterface modelInterface;                // Loads, displays, and styles 3D models
    private TreeSearchHandler searchHandler;              // Manages searching within the TreeView



    private final UndoRedoManager undoRedoManager = new UndoRedoManager();   //undo redo functionality
    private SubScene subScene; // make this a field

    /**
     * Constructor sets up all GUI components and logic connections.
     * @param stage JavaFX stage (window)
     * @param controller WindowController (FXML controller)
     * @param model The data model representing anatomy and file structure
     */
    public WindowPresenter(Stage stage, WindowController controller, Model model) {
        this.stage = stage;
        this.controller = controller;
        this.model = model;

        // Set up model interaction with currently active TreeView
        this.modelInterface = new ModelInterface(innerGroup, controller.getActiveTreeView());

        // Provide TreeView via supplier for dynamic tab switching
        this.searchHandler = new TreeSearchHandler(controller::getActiveTreeView, controller.getSearchStatusLabel());

        initializeTrees();               // Build tree structure for UI
        setupTreeSelectionListener();    // Link TreeView selection with 3D view
        setupButtonHandlers();           // Connect UI buttons to logic
    }

    /**
     * Initializes the "Part-Of" and "Is-A" trees from the model.
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
     * Recursively converts an ANode-based model into a JavaFX TreeItem-based tree.
     * @param node ANode model node
     * @return TreeItem representation
     */
    private TreeItem<ANode> buildTreeItem(ANode node) {
        TreeItem<ANode> item = new TreeItem<>(node);
        for (ANode child : node.children()) {
            item.getChildren().add(buildTreeItem(child));
        }
        item.setExpanded(false);
        return item;
    }

    /**
     * Listens to selection changes in the TreeView and updates the 3D view accordingly.
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
     * Wires up all buttons in the GUI to their corresponding event logic.
     */
    private void setupButtonHandlers() {
        controller.getExpandButton().setOnAction(e -> expandSelected());
        controller.getCollapseButton().setOnAction(e -> collapseSelected());
        controller.getSelectButton().setOnAction(e -> selectAllDescendants());
        controller.getDeselectButton().setOnAction(e ->
                controller.getActiveTreeView().getSelectionModel().clearSelection());

        /*controller.getShowButton().setOnAction(e -> handleShow());*/
        /*controller.getHideButton().setOnAction(e ->
                modelInterface.hideModels(controller.getActiveTreeView().getSelectionModel().getSelectedItems()));*/
        controller.getShowButton().setOnAction(e -> {
            // ⛔ Important: capture current visible state BEFORE changing it!
            Set<String> beforeVisible = new HashSet<>(modelInterface.getCurrentlyVisibleFileIds());
            System.out.println("Undo: Showing previous model IDs: " + beforeVisible);
            // Capture selected TreeItems
            List<TreeItem<ANode>> selectedItems = new ArrayList<>(
                    controller.getActiveTreeView().getSelectionModel().getSelectedItems());

            // Extract fileIds to show
            Set<String> showFileIds = selectedItems.stream()
                    .map(TreeItem::getValue)
                    .filter(Objects::nonNull)
                    .flatMap(anode -> anode.fileIds().stream())
                    .collect(Collectors.toSet());




            // === Add Undo/Redo Command ===
            undoRedoManager.add(new SimpleCommand("Show Models",
                    () -> {
                        System.out.println("Undo: Showing previous model IDs: " + beforeVisible);
                        modelInterface.loadAndDisplayModelsByFileIds(beforeVisible);
                        modelInterface.syncTreeSelectionFromFileIds();
                        refreshViewLayout();
                    },
                    () -> {
                        System.out.println("Redo: Re-showing file IDs: " + showFileIds);
                        modelInterface.loadAndDisplayModelsByFileIds(showFileIds);
                        modelInterface.syncTreeSelectionFromFileIds();
                        refreshViewLayout();
                    }
            ));

            // === Do initial show ===
            modelInterface.loadAndDisplayModelsByFileIds(showFileIds);
            modelInterface.syncTreeSelectionFromFileIds();
            refreshViewLayout();
        });


        controller.getHideButton().setOnAction(e -> {
            List<TreeItem<ANode>> selectedItems = new ArrayList<>(
                    controller.getActiveTreeView().getSelectionModel().getSelectedItems());

            Set<String> fileIdsToHide = selectedItems.stream()
                    .map(TreeItem::getValue)
                    .filter(Objects::nonNull)
                    .flatMap(anode -> anode.fileIds().stream())
                    .collect(Collectors.toSet());

            Set<String> beforeVisible = modelInterface.getCurrentlyVisibleFileIds();

            // Simulate what would be visible after hiding
            Set<String> afterHideVisible = new HashSet<>(beforeVisible);
            afterHideVisible.removeAll(fileIdsToHide);

            undoRedoManager.add(new SimpleCommand("Hide Models",
                    () -> {
                        // Undo: restore full original set
                        modelInterface.loadAndDisplayModelsByFileIds(beforeVisible);
                        modelInterface.syncTreeSelectionFromFileIds();
                        refreshViewLayout();
                    },
                    () -> {
                        // Redo: apply the hide again
                        modelInterface.loadAndDisplayModelsByFileIds(afterHideVisible);
                        modelInterface.syncTreeSelectionFromFileIds();
                        refreshViewLayout();
                    }
            ));

            // Initial hide execution
            modelInterface.loadAndDisplayModelsByFileIds(afterHideVisible);
            modelInterface.syncTreeSelectionFromFileIds();
            refreshViewLayout();
        });

        controller.getColorPicker().setOnAction(e -> {
            Color newColor = controller.getColorPicker().getValue();
            Color oldColor = modelInterface.getFirstSelectedColor(); // use new method

            undoRedoManager.add(new SimpleCommand("Color Change",
                    () -> {modelInterface.applyColorToSelected(oldColor);
            controller.getColorPicker().setValue(oldColor);}, // Update UI;}
                    () -> {modelInterface.applyColorToSelected(newColor);
                controller.getColorPicker().setValue(newColor);
            }
            ));
        });

        controller.getFindButton().setOnAction(e -> handleFind());
        controller.getFirstButton().setOnAction(e -> handleFirst());
        controller.getNextButton().setOnAction(e -> handleNext());
        controller.getAllButton().setOnAction(e -> handleAll());

        controller.getSearchTextField().setOnAction(e -> handleFind());

        controller.getUndoButton().setOnAction(e -> undoRedoManager.undo());
        controller.getRedoButton().setOnAction(e -> undoRedoManager.redo());

        controller.getUndoButton().disableProperty().bind(undoRedoManager.canUndoProperty().not());
        controller.getRedoButton().disableProperty().bind(undoRedoManager.canRedoProperty().not());
    }

    // === Tree Expand/Collapse/Selection ===

    /**
     * Expands all descendants of currently selected nodes.
     */
    private void expandSelected() {
        controller.getActiveTreeView().getSelectionModel().getSelectedItems().forEach(this::expand);
    }

    /**
     * Collapses all descendants of currently selected nodes.
     */
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
     * Selects all descendants of currently selected nodes in the TreeView.
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

    /**
     * Recursively selects all child nodes of the given parent node.
     */
    private void collectDescendants(TreeItem<ANode> parent, MultipleSelectionModel<TreeItem<ANode>> model) {
        for (TreeItem<ANode> child : parent.getChildren()) {
            model.select(child);
            collectDescendants(child, model);
        }
    }

    // === 3D Visualization Handling ===
    private void refresh3DScene() {
        setup3DScene();
        Platform.runLater(() -> {
            innerGroup.applyCss();
            innerGroup.layout();
            centerContentGroup();
            autoAdjustCamera();
        });
    }
    /**
     * Handles showing selected models in the 3D scene.
     */
    private void handleShow() {
        modelInterface.loadAndDisplayModels(controller.getActiveTreeView().getSelectionModel().getSelectedItems());

        Platform.runLater(() -> {
            innerGroup.applyCss();
            innerGroup.layout();
            centerContentGroup();
            autoAdjustCamera();
        });

        setup3DScene(); // Setup 3D scene only once
    }

    /**
     * Moves the model group so that its center aligns with the origin (0,0,0).
     */
    private void centerContentGroup() {
        innerGroup.layout();
        /*Bounds bounds = innerGroup.getBoundsInParent();*/
        Bounds bounds = innerGroup.getLayoutBounds();
        double cx = (bounds.getMinX() + bounds.getMaxX()) / 2;
        double cy = (bounds.getMinY() + bounds.getMaxY()) / 2;
        double cz = (bounds.getMinZ() + bounds.getMaxZ()) / 2;
        innerGroup.getTransforms().setAll(new Translate(-cx, -cy, -cz));
    }

    /**
     * Adjusts the camera distance so the model fits entirely in view.
     */
    private void autoAdjustCamera() {
        innerGroup.applyCss();
        innerGroup.layout();
        Bounds bounds = innerGroup.getBoundsInParent();
        double maxDim = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-maxDim * 2.2);
    }

    /**
     * Initializes the 3D scene including camera, lighting, and interaction.
     */
   /* private void setup3DScene() {
        if (root3D.getChildren().isEmpty()) {
            contentGroup.getChildren().add(innerGroup);
            root3D.getChildren().add(contentGroup);

            // Add lights to the scene
            PointLight pointLight = new PointLight(Color.WHITE);
            pointLight.setTranslateX(-500);
            pointLight.setTranslateY(-500);
            pointLight.setTranslateZ(-500);

            AmbientLight ambientLight = new AmbientLight(Color.DARKGRAY);
            root3D.getChildren().addAll(pointLight, ambientLight);

            // Set up camera properties
            camera.setNearClip(0.1);
            camera.setFarClip(10000);
            camera.setTranslateZ(-500);

            // Create the SubScene that renders the 3D content
            SubScene subScene = new SubScene(root3D, 600, 600, true, SceneAntialiasing.BALANCED);
            subScene.setCamera(camera);
            subScene.setFill(Color.LIGHTGRAY);
            subScene.setOnMouseClicked(e -> subScene.requestFocus());

            // Set keyboard controls for camera interaction
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

            // Make 3D canvas resize with the window
            subScene.widthProperty().bind(controller.getVisualizationPane().widthProperty());
            subScene.heightProperty().bind(controller.getVisualizationPane().heightProperty());
            controller.getVisualizationPane().getChildren().setAll(subScene);

            // Initialize mouse-based rotation and zoom
            interactionHandler = new SceneInteractionHandler(contentGroup, camera);
            interactionHandler.setupMouseInteraction(controller.getVisualizationPane());
        }
    }*/

    private void setup3DScene() {
        if (subScene == null) {
            if (!contentGroup.getChildren().contains(innerGroup)) {
                contentGroup.getChildren().add(innerGroup);
            }

            if (!root3D.getChildren().contains(contentGroup)) {
                root3D.getChildren().add(contentGroup);
            }

            // Add lights if not present
            if (root3D.getChildren().stream().noneMatch(n -> n instanceof PointLight || n instanceof AmbientLight)) {
                PointLight pointLight = new PointLight(Color.WHITE);
                pointLight.setTranslateX(-500);
                pointLight.setTranslateY(-500);
                pointLight.setTranslateZ(-500);

                AmbientLight ambientLight = new AmbientLight(Color.DARKGRAY);
                root3D.getChildren().addAll(pointLight, ambientLight);
            }

            // Camera setup
            camera.setNearClip(0.1);
            camera.setFarClip(10000);
            camera.setTranslateZ(-500);

            // Create and configure the SubScene
            subScene = new SubScene(root3D, 600, 600, true, SceneAntialiasing.BALANCED);
            subScene.setCamera(camera);
            subScene.setFill(Color.LIGHTGRAY);
            subScene.setOnMouseClicked(e -> subScene.requestFocus());

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

            subScene.widthProperty().bind(controller.getVisualizationPane().widthProperty());
            subScene.heightProperty().bind(controller.getVisualizationPane().heightProperty());

            controller.getVisualizationPane().getChildren().setAll(subScene);

            interactionHandler = new SceneInteractionHandler(contentGroup, camera);
            interactionHandler.setupMouseInteraction(controller.getVisualizationPane());
        }
    }

    private void hide3DScene() {
        controller.getVisualizationPane().getChildren().clear();
        subScene = null; // Reset so it can be rebuilt later
    }

    // === Search Delegates ===

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

    private void refreshViewLayout() {
        setup3DScene(); // only creates it if needed
        Platform.runLater(() -> {
            innerGroup.applyCss();
            innerGroup.layout();
            centerContentGroup();
            autoAdjustCamera();
        });
    }

}
