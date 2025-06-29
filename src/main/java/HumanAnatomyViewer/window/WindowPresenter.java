package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import HumanAnatomyViewer.model.Model;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import javafx.animation.PauseTransition;
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

    private boolean isExploded = false;
    private final Map<Node, Point3D> originalPositions = new HashMap<>();

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

            // Save old colors per fileId
            Map<String, Color> oldColorMap = modelInterface.getCurrentColorsForSelected();

            // Build new color map with same fileIds but new color
            Map<String, Color> newColorMap = new HashMap<>();
            for (String fileId : oldColorMap.keySet()) {
                newColorMap.put(fileId, newColor);
            }

            // Register Undo/Redo
            undoRedoManager.add(new SimpleCommand("Color Change",
                    () -> {
                        modelInterface.applyColorsFromMap(oldColorMap);
                        controller.getColorPicker().setValue(oldColorMap.values().stream().findFirst().orElse(Color.GRAY));
                    },
                    () -> {
                        modelInterface.applyColorsFromMap(newColorMap);
                        controller.getColorPicker().setValue(newColor);
                    }
            ));

            // Apply color now
            modelInterface.applyColorsFromMap(newColorMap);
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
        controller.getExplodeButton().setOnAction(e -> {
            if (!isExploded) {
                originalPositions.clear();

                List<TranslateTransition> explodeAnimations = new ArrayList<>();
                List<TranslateTransition> assembleAnimations = new ArrayList<>();

                // === 1. Compute center of the whole model group ===
                Bounds bounds = modelInterface.getInnerGroup().getBoundsInParent();
                Point3D globalCenter = new Point3D(
                        (bounds.getMinX() + bounds.getMaxX()) / 2,
                        (bounds.getMinY() + bounds.getMaxY()) / 2,
                        (bounds.getMinZ() + bounds.getMaxZ()) / 2
                );

                double explodeFactor = 1.5;  // 🔧 Adjust this to push parts further out

                for (Node node : modelInterface.getInnerGroup().getChildren()) {
                    // === 2. Save original position for assembly later ===
                    Point3D original = new Point3D(node.getTranslateX(), node.getTranslateY(), node.getTranslateZ());
                    originalPositions.put(node, original);

                    // === 3. Calculate node center and explosion direction ===
                    Bounds nodeBounds = node.getBoundsInParent();
                    Point3D nodeCenter = new Point3D(
                            (nodeBounds.getMinX() + nodeBounds.getMaxX()) / 2,
                            (nodeBounds.getMinY() + nodeBounds.getMaxY()) / 2,
                            (nodeBounds.getMinZ() + nodeBounds.getMaxZ()) / 2
                    );

                    Point3D direction = nodeCenter.subtract(globalCenter).normalize();
                    double distance = nodeCenter.distance(globalCenter);
                    Point3D offset = direction.multiply(distance * explodeFactor);

                    // === 4. Create Explode Animation ===
                    TranslateTransition explode = new TranslateTransition(Duration.seconds(1.5), node);
                    explode.setByX(offset.getX());
                    explode.setByY(offset.getY());
                    explode.setByZ(offset.getZ());
                    explode.setInterpolator(Interpolator.EASE_OUT);
                    explodeAnimations.add(explode);

                    // === 5. Create Assemble Animation ===
                    TranslateTransition assemble = new TranslateTransition(Duration.seconds(1.5), node);
                    assemble.setToX(original.getX());
                    assemble.setToY(original.getY());
                    assemble.setToZ(original.getZ());
                    assemble.setInterpolator(Interpolator.EASE_BOTH);
                    assembleAnimations.add(assemble);
                }

                // === 6. Create Pause Transition ===
                PauseTransition pause = new PauseTransition(Duration.seconds(2));

                // === 7. Chain Explode → Pause → Assemble ===
                ParallelTransition explodeAll = new ParallelTransition();
                explodeAll.getChildren().addAll(explodeAnimations);

                ParallelTransition assembleAll = new ParallelTransition();
                assembleAll.getChildren().addAll(assembleAnimations);

                SequentialTransition sequence = new SequentialTransition(explodeAll, pause, assembleAll);
                sequence.setOnFinished(event -> isExploded = false); // Reset flag after animation
                sequence.play();

            } else {
                // === Manual Assembly when already exploded ===
                for (Node node : modelInterface.getInnerGroup().getChildren()) {
                    Point3D original = originalPositions.getOrDefault(node, new Point3D(0, 0, 0));

                    TranslateTransition assemble = new TranslateTransition(Duration.seconds(1.5), node);
                    assemble.setToX(original.getX());
                    assemble.setToY(original.getY());
                    assemble.setToZ(original.getZ());
                    assemble.setInterpolator(Interpolator.EASE_BOTH);
                    assemble.play();
                }

                isExploded = false;
            }
        });
/*
        controller.getExplodeButton().setOnAction(e -> {
            if (!isExploded) {
                originalPositions.clear();

                List<TranslateTransition> explodeAnimations = new ArrayList<>();
                List<TranslateTransition> assembleAnimations = new ArrayList<>();

                for (Node node : modelInterface.getInnerGroup().getChildren()) {
                    Point3D original = new Point3D(node.getTranslateX(), node.getTranslateY(), node.getTranslateZ());
                    originalPositions.put(node, original);

                    // === EXPLODE Animation ===

                    double offsetX = Math.random() * 300 - 150;
                    double offsetY = 200;
                    double offsetZ = Math.random() * 300 - 150;

                    TranslateTransition explode = new TranslateTransition(Duration.seconds(1.5), node);
                    explode.setByX(offsetX);
                    explode.setByY(offsetY);
                    explode.setByZ(offsetZ);
                    explode.setInterpolator(Interpolator.EASE_OUT);
                    explodeAnimations.add(explode);

                    // === ASSEMBLE Animation ===
                    TranslateTransition assemble = new TranslateTransition(Duration.seconds(1.5), node);
                    assemble.setToX(original.getX());
                    assemble.setToY(original.getY());
                    assemble.setToZ(original.getZ());
                    assemble.setInterpolator(Interpolator.EASE_BOTH);
                    assembleAnimations.add(assemble);
                }

                // === Pause between explode and assemble ===
                PauseTransition pause = new PauseTransition(Duration.seconds(2));

                // === Chain: Explode → Pause → Assemble ===
                ParallelTransition explodeAll = new ParallelTransition();
                explodeAll.getChildren().addAll(explodeAnimations);

                ParallelTransition assembleAll = new ParallelTransition();
                assembleAll.getChildren().addAll(assembleAnimations);

                SequentialTransition sequence = new SequentialTransition(explodeAll, pause, assembleAll);
                sequence.setOnFinished(event -> isExploded = false); // Reset flag after assemble
                sequence.play();

            } else {
                // If user presses button again while exploded (manual assemble)
                for (Node node : modelInterface.getInnerGroup().getChildren()) {
                    Point3D original = originalPositions.getOrDefault(node, new Point3D(0, 0, 0));

                    TranslateTransition assemble = new TranslateTransition(Duration.seconds(1.5), node);
                    assemble.setToX(original.getX());
                    assemble.setToY(original.getY());
                    assemble.setToZ(original.getZ());
                    assemble.setInterpolator(Interpolator.EASE_BOTH);
                    assemble.play();
                }

                isExploded = false;
            }
        });
*/

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



    private Point3D getGroupCenter(Group group) {
        Bounds bounds = group.getBoundsInParent();
        return new Point3D(
                (bounds.getMinX() + bounds.getMaxX()) / 2.0,
                (bounds.getMinY() + bounds.getMaxY()) / 2.0,
                (bounds.getMinZ() + bounds.getMaxZ()) / 2.0
        );
    }
}
