package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import HumanAnatomyViewer.model.Model;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * WindowPresenter handles the main logic and interaction for the application
 * window. It connects the data model, GUI (via the controller), and 3D scene
 * rendering.
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
    private String lastQuery = "";

    private final UndoRedoManager undoRedoManager = new UndoRedoManager();   //undo redo functionality
    private SubScene subScene; // make this a field

    private boolean isExploded = false;
    private final Map<Node, Point3D> originalPositions = new HashMap<>();

    //for dark mode
    private boolean darkModeEnabled = false;

    /**
     * Constructor sets up all GUI components and logic connections.
     *
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
     * Recursively converts an ANode-based model into a JavaFX TreeItem-based
     * tree.
     *
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
     * Listens to selection changes in the TreeView and updates the 3D view
     * accordingly.
     */
    private void setupTreeSelectionListener() {
        ListChangeListener<TreeItem<ANode>> listener = change -> {
            if (modelInterface.isInSelectionUpdate()) {
                return;
            }

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
// === Tree Interaction Buttons ===

// Expands the selected node(s) in the TreeView to show child elements
        controller.getExpandButton().setOnAction(e -> expandSelected());

// Collapses the selected node(s) in the TreeView to hide child elements
        controller.getCollapseButton().setOnAction(e -> collapseSelected());

// Selects all descendant nodes (children, grandchildren, etc.) of the selected item
        controller.getSelectButton().setOnAction(e -> selectAllDescendants());

// Clears any current selection in the TreeView
        controller.getDeselectButton().setOnAction(e
                -> controller.getActiveTreeView().getSelectionModel().clearSelection()
        );

// === File Management ===
// Prompts the user to select a model directory and loads all 3D model files from it
        controller.getMenuLoadFiles().setOnAction(e -> promptUserToSelectModelDirectory());

// === AI Search ===
// Performs a natural-language AI-based search over anatomical terms
        controller.getAISearchButton().setOnAction(e -> handleAISearch());

        //show selected items
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

        // hide selected objects
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

        // Set up an action listener on the ColorPicker in the UI
        controller.getColorPicker().setOnAction(e -> {

            //  Step 1: Get the new color selected by the user
            Color newColor = controller.getColorPicker().getValue();

            //  Step 2: Capture current (old) colors for all selected file IDs
            // This will be used to restore the previous state during an undo
            Map<String, Color> oldColorMap = modelInterface.getCurrentColorsForSelected();

            //  Step 3: Build a map assigning the new color to the same file IDs
            // This is needed to apply the new state and for redo support
            Map<String, Color> newColorMap = new HashMap<>();
            for (String fileId : oldColorMap.keySet()) {
                newColorMap.put(fileId, newColor);
            }

            //  Step 4: Register this change with the UndoRedoManager
            // A SimpleCommand is created with undo and redo logic
            undoRedoManager.add(new SimpleCommand("Color Change",
                    // Undo logic: revert to previous colors and update the ColorPicker UI
                    () -> {
                        modelInterface.applyColorsFromMap(oldColorMap);

                        // Optional: reset the color picker value to match the undone color
                        Color oldRepresentativeColor = oldColorMap.values().stream()
                                .findFirst().orElse(Color.rgb(200, 200, 200)); // fallback color
                        controller.getColorPicker().setValue(oldRepresentativeColor);
                    },
                    // Redo logic: reapply the new colors and update the ColorPicker UI
                    () -> {
                        modelInterface.applyColorsFromMap(newColorMap);
                        controller.getColorPicker().setValue(newColor);
                    }
            ));

            // 🔸 Step 5: Apply the new color immediately to the selected models
            modelInterface.applyColorsFromMap(newColorMap);
        });

// === Search Button Event Handlers ===
// When "Find" button is clicked or Enter is pressed in the search field
        controller.getFindButton().setOnAction(e -> handleFind());
        controller.getSearchTextField().setOnAction(e -> handleFind()); // Pressing Enter triggers the same

// Navigate to the first match found in the search
        controller.getFirstButton().setOnAction(e -> handleFirst());

// Navigate to the next match in the current search results
        controller.getNextButton().setOnAction(e -> handleNext());

// Select and highlight all matches for the search query
        controller.getAllButton().setOnAction(e -> handleAll());

// === Undo/Redo Event Handlers ===
// Click on Undo button → undo the last command via UndoRedoManager
        controller.getUndoButton().setOnAction(e -> undoRedoManager.undo());

// Click on Redo button → redo the last undone command
        controller.getRedoButton().setOnAction(e -> undoRedoManager.redo());

// === Undo/Redo Button Enable Bindings ===
// Disable the Undo button if there's nothing to undo
        controller.getUndoButton().disableProperty().bind(undoRedoManager.canUndoProperty().not());

// Disable the Redo button if there's nothing to redo
        controller.getRedoButton().disableProperty().bind(undoRedoManager.canRedoProperty().not());

        // Set up the explode button to animate the "exploding" or reassembling of 3D model parts
        controller.getExplodeButton().setOnAction(e -> {

            // If the model is not currently exploded, perform the explode + reassemble animation
            if (!isExploded) {

                // Clear previously stored original positions
                originalPositions.clear();

                // Prepare lists to store animations for explosion and reassembly
                List<TranslateTransition> explodeAnimations = new ArrayList<>();
                List<TranslateTransition> assembleAnimations = new ArrayList<>();

                // Calculate the global center of the entire 3D model group
                Bounds bounds = modelInterface.getInnerGroup().getLayoutBounds();
                Point3D globalCenter = new Point3D(
                        (bounds.getMinX() + bounds.getMaxX()) / 2,
                        (bounds.getMinY() + bounds.getMaxY()) / 2,
                        (bounds.getMinZ() + bounds.getMaxZ()) / 2
                );

                double explodeFactor = 1.5; // Controls how far each part explodes from the center

                // Loop through each node (anatomical part) in the 3D group
                for (Node node : modelInterface.getInnerGroup().getChildren()) {

                    // Save the original translation (position) of the node
                    Point3D original = new Point3D(
                            node.getTranslateX(), node.getTranslateY(), node.getTranslateZ()
                    );
                    originalPositions.put(node, original);

                    // Compute the node's center in the scene
                    Bounds nodeBounds = node.getBoundsInParent();
                    Point3D nodeCenter = new Point3D(
                            (nodeBounds.getMinX() + nodeBounds.getMaxX()) / 2,
                            (nodeBounds.getMinY() + nodeBounds.getMaxY()) / 2,
                            (nodeBounds.getMinZ() + nodeBounds.getMaxZ()) / 2
                    );

                    // Get direction vector from global center to node center
                    Point3D direction = nodeCenter.subtract(globalCenter);

                    // Avoid zero-length vectors by introducing a random small direction
                    if (direction.magnitude() == 0) {
                        direction = new Point3D(Math.random(), Math.random(), Math.random());
                    }

                    // Normalize the direction to unit length
                    direction = direction.normalize();

                    // Multiply by a constant to determine how far to "explode" this part
                    Point3D offset = direction.multiply(explodeFactor * 120); // 120 is base offset

                    // Create explode animation for this node
                    TranslateTransition explode = new TranslateTransition(Duration.seconds(1.2), node);
                    explode.setByX(offset.getX());
                    explode.setByY(offset.getY());
                    explode.setByZ(offset.getZ());
                    explode.setInterpolator(Interpolator.EASE_OUT);
                    explodeAnimations.add(explode);

                    // Create matching assemble animation to restore the original position
                    TranslateTransition assemble = new TranslateTransition(Duration.seconds(1.2), node);
                    assemble.setToX(original.getX());
                    assemble.setToY(original.getY());
                    assemble.setToZ(original.getZ());
                    assemble.setInterpolator(Interpolator.EASE_BOTH);
                    assembleAnimations.add(assemble);
                }

                // Add a short pause between explosion and reassembly
                PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

                // Combine all explode animations into one parallel animation
                ParallelTransition explodeAll = new ParallelTransition();
                explodeAll.getChildren().addAll(explodeAnimations);

                // Combine all assemble animations into one parallel animation
                ParallelTransition assembleAll = new ParallelTransition();
                assembleAll.getChildren().addAll(assembleAnimations);

                // Create a sequence: explode → pause → assemble
                SequentialTransition sequence = new SequentialTransition(explodeAll, pause, assembleAll);

                // After reassembly, mark model as not exploded
                sequence.setOnFinished(event -> isExploded = false);

                // Play the full sequence
                sequence.play();

                // Temporarily mark as exploded to block button from repeating
                isExploded = true;

            } else {
                // If the model is already exploded, skip animation and just reassemble

                for (Node node : modelInterface.getInnerGroup().getChildren()) {
                    Point3D original = originalPositions.getOrDefault(node, new Point3D(0, 0, 0));

                    TranslateTransition assemble = new TranslateTransition(Duration.seconds(1.2), node);
                    assemble.setToX(original.getX());
                    assemble.setToY(original.getY());
                    assemble.setToZ(original.getZ());
                    assemble.setInterpolator(Interpolator.EASE_BOTH);
                    assemble.play();
                }

                isExploded = false;
            }
        });
        //dark mode
        controller.getMenuEnableDarkMode().setOnAction(e -> enableDarkMode());
        //full screen
        controller.getMenuToggleFullScreen().setOnAction(e -> toggleFullScreen());


    }

    public void promptUserToSelectModelDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Model Directory");

        File selectedDir = chooser.showDialog(stage); // Use stage for modality
        if (selectedDir != null && selectedDir.isDirectory()) {
            modelInterface.setCustomDirectory(selectedDir);
            System.out.println("✔ Custom model directory set: " + selectedDir.getAbsolutePath());
        } else {
            System.out.println("⚠ No directory selected or invalid.");
        }
    }

    public void setCustomModelDirectory(File directory) {
        modelInterface.setCustomDirectory(directory); // ✔ connects to the method you added
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
                PointLight pointLight = new PointLight(Color.GRAY);
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
            subScene = new SubScene(root3D, 800, 800, true, SceneAntialiasing.BALANCED);
            subScene.setCamera(camera);
            subScene.setFill(Color.rgb(200, 200, 200));
            subScene.setOnMouseClicked(e -> subScene.requestFocus());

            subScene.setOnKeyPressed(e -> {
                KeyCode code = e.getCode();
                switch (code) {
                    case Z ->
                        interactionHandler.resetTransform();
                    case I ->
                        interactionHandler.zoom(50);
                    case O ->
                        interactionHandler.zoom(-50);
                    case LEFT ->
                        interactionHandler.rotateY(-10);
                    case RIGHT ->
                        interactionHandler.rotateY(10);
                    case UP ->
                        interactionHandler.rotateX(-10);
                    case DOWN ->
                        interactionHandler.rotateX(10);
                }
            });

            subScene.widthProperty().bind(controller.getVisualizationPane().widthProperty());
            subScene.heightProperty().bind(controller.getVisualizationPane().heightProperty());

            controller.getVisualizationPane().getChildren().setAll(subScene);

            interactionHandler = new SceneInteractionHandler(contentGroup, camera);
            interactionHandler.setupMouseInteraction(controller.getVisualizationPane());
        }
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



    public void handleAISearch() {
        String query = controller.getSearchTextField().getText();
        if (query == null || query.isBlank()) {
            controller.getSearchStatusLabel().setText("Please enter a query.");
            return;
        }

        List<String> leafLabels = getLeafLabelsFromTree(controller.getActiveTreeView());

        boolean isColorQuery = query.toLowerCase().matches(".*\\b(color|paint|fill)\\b.*");

        if (isColorQuery) {
            runColorSuggestionFlow(query, leafLabels);
        } else {
            runRegexSearchFlow(query, leafLabels);
        }
    }

    private List<String> getLeafLabelsFromTree(TreeView<ANode> tree) {
        List<String> labels = new ArrayList<>();
        collectLeafLabels(tree.getRoot(), labels);
        return labels;
    }

    private void collectLeafLabels(TreeItem<ANode> item, List<String> list) {
        if (item.getChildren().isEmpty()) {
            if (item.getValue() != null && item.getValue().name() != null) {
                list.add(item.getValue().name().toLowerCase());
            }
        } else {
            for (TreeItem<ANode> child : item.getChildren()) {
                collectLeafLabels(child, list);
            }
        }
    }

    private void runRegexSearchFlow(String query, List<String> leafLabels) {
        AIRegexTask task = new AIRegexTask(query, leafLabels);

        task.setOnSucceeded(e -> {
            String regex = task.getValue();
            if (regex == null || regex.isEmpty()) {
                controller.getSearchStatusLabel().setText("❌ No regex returned from AI.");
                return;
            }

            boolean success = searchHandler.search(regex);
            if (success) {
                searchHandler.selectAll(regex);
                controller.getSearchStatusLabel().setText("✅ Found and selected matches.");
            } else {
                controller.getSearchStatusLabel().setText("⚠ AI returned regex, but no matches found.");
            }
        });

        task.setOnFailed(e -> {
            controller.getSearchStatusLabel().setText("❌ AI search failed.");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void runColorSuggestionFlow(String query, List<String> leafLabels) {
        new Thread(() -> {
            try {
                Map<String, String> colorMap = AISearchService.getColorMapFromQuery(query, leafLabels);

                if (colorMap.isEmpty()) {
                    Platform.runLater(() -> controller.getSearchStatusLabel().setText("⚠ No color matches found."));
                    return;
                }

                Platform.runLater(() -> {
                    Map<String, Color> fileIdToColor = new HashMap<>();

                    for (Map.Entry<String, String> entry : colorMap.entrySet()) {
                        String term = entry.getKey().toLowerCase();
                        String hex = entry.getValue();

                        // Select tree nodes via searchHandler
                        boolean found = searchHandler.search(term);
                        if (found) {
                            searchHandler.selectAll(term);
                        }

                        List<TreeItem<ANode>> matchingItems = findMatchingTreeItems(term);
                        for (TreeItem<ANode> item : matchingItems) {
                            if (item.getValue() != null) {
                                for (String fileId : item.getValue().fileIds()) {
                                    fileIdToColor.put(fileId, Color.web(hex));
                                }
                            }
                        }
                    }

                    modelInterface.loadAndDisplayModelsByFileIds(fileIdToColor.keySet());
                    modelInterface.applyColorsFromMap(fileIdToColor);
                    modelInterface.syncTreeSelectionFromFileIds();
                    refreshViewLayout();
                    controller.getSearchStatusLabel().setText("🎨 Applied AI-suggested colors.");
                });

            } catch (Exception e) {
                Platform.runLater(() -> controller.getSearchStatusLabel().setText("❌ AI color suggestion failed."));
                e.printStackTrace();
            }
        }).start();
    }

    private List<TreeItem<ANode>> findMatchingTreeItems(String term) {
        List<TreeItem<ANode>> result = new ArrayList<>();
        matchTreeItems(controller.getActiveTreeView().getRoot(), term, result);
        return result;
    }

    private void matchTreeItems(TreeItem<ANode> node, String term, List<TreeItem<ANode>> result) {
        if (node.getValue() != null && node.getValue().name().toLowerCase().contains(term)) {
            result.add(node);
        }
        for (TreeItem<ANode> child : node.getChildren()) {
            matchTreeItems(child, term, result);
        }
    }

//enable dark mode
    private void enableDarkMode() {
        Scene scene = stage.getScene();
        String darkStyle = getClass().getResource("/HumanAnatomy/modena_dark.css").toExternalForm();

        if (!darkModeEnabled) {
            scene.getStylesheets().add(darkStyle);
            darkModeEnabled = true;
            controller.getMenuEnableDarkMode().setText("Disable Dark Mode");
        } else {
            scene.getStylesheets().remove(darkStyle);
            darkModeEnabled = false;
            controller.getMenuEnableDarkMode().setText("Enable Dark Mode");
        }
    }

    // Enable or disable full screen and update the menu text accordingly
    private void toggleFullScreen() {
        boolean goingFullScreen = !stage.isFullScreen(); // determine new state
        stage.setFullScreen(goingFullScreen);            // apply full screen toggle

        // Update menu text to reflect the new state
        controller.getMenuToggleFullScreen().setText(
                goingFullScreen ? "Exit Full Screen" : "Show Full Screen"
        );
    }

}
