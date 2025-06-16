package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import HumanAnatomyViewer.model.Model;
import HumanAnatomyViewer.model.ObjIO;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.PointLight;
import javafx.scene.AmbientLight;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class WindowPresenter {

    private final Stage stage;
    private final WindowController controller;
    private final Model model;

    private SceneInteractionHandler interactionHandler;

    private TreeItem<ANode> partOfRootItem;
    private TreeItem<ANode> isARootItem;

    private final Group innerGroup = new Group();
    private final Group contentGroup = new Group();
    private final Group root3D = new Group();

    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Map<String, Group> loadedModels = new HashMap<>();

    private List<TreeItem<ANode>> searchResults = new ArrayList<>();
    private int currentSearchIndex = -1;

    private final Set<String> selectedFileIds = new HashSet<>();
    private boolean inSelectionUpdate = false;

    public WindowPresenter(Stage stage, WindowController controller, Model model) {
        this.stage = stage;
        this.controller = controller;
        this.model = model;

        initializeTrees();
        setupTreeSelection();
        setupButtonHandlers();
    }

    private void initializeTrees() {
        partOfRootItem = buildTreeItem(model.getPartOfRoot());
        isARootItem = buildTreeItem(model.getIsARoot());

        TreeView<ANode> tree = controller.getTreeView();
        tree.setRoot(partOfRootItem);
        tree.setShowRoot(true);
        tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private TreeItem<ANode> buildTreeItem(ANode node) {
        TreeItem<ANode> item = new TreeItem<>(node);
        for (ANode child : node.children()) {
            item.getChildren().add(buildTreeItem(child));
        }
        item.setExpanded(false);
        return item;
    }

    private void setupTreeSelection() {
        controller.getTreeView().getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<TreeItem<ANode>>) change -> {
                    if (inSelectionUpdate) return;
                    inSelectionUpdate = true;

                    selectedFileIds.clear();
                    for (TreeItem<ANode> item : controller.getTreeView().getSelectionModel().getSelectedItems()) {
                        if (item.getValue() != null) {
                            selectedFileIds.addAll(item.getValue().fileIds());
                        }
                    }

                    applyDrawModeBasedOnSelection();
                    inSelectionUpdate = false;
                }
        );
    }

    private void setupButtonHandlers() {
        controller.getIsAButton().setOnAction(e -> switchToIsATree());
        controller.getPartOfButton().setOnAction(e -> switchToPartOfTree());
        controller.getExpandButton().setOnAction(e -> handleExpand());
        controller.getCollapseButton().setOnAction(e -> handleCollapse());
        controller.getSelectButton().setOnAction(e -> handleSelectAll());
        controller.getDeselectButton().setOnAction(e -> handleSelectNone());
        controller.getShowButton().setOnAction(e -> handleShow());
        controller.getHideButton().setOnAction(e -> handleHide());
        controller.getFindButton().setOnAction(e -> handleFind());
        controller.getFirstButton().setOnAction(e -> handleFirst());
        controller.getNextButton().setOnAction(e -> handleNext());
        controller.getAllButton().setOnAction(e -> handleAll());
        controller.getSearchTextField().setOnAction(e -> handleFind());
        controller.getColorPicker().setOnAction(e -> handleColorChange());
    }

    private void switchToIsATree() {
        controller.getTreeView().setRoot(isARootItem);
    }

    private void switchToPartOfTree() {
        controller.getTreeView().setRoot(partOfRootItem);
    }

    private void handleExpand() {
        controller.getTreeView().getSelectionModel().getSelectedItems()
                .forEach(this::expand);
    }

    private void handleCollapse() {
        controller.getTreeView().getSelectionModel().getSelectedItems()
                .forEach(this::collapse);
    }

    private void expand(TreeItem<ANode> item) {
        item.setExpanded(true);
        item.getChildren().forEach(this::expand);
    }

    private void collapse(TreeItem<ANode> item) {
        item.getChildren().forEach(this::collapse);
        item.setExpanded(false);
    }

    private void handleSelectAll() {
        TreeView<ANode> tree = controller.getTreeView();
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

    private void handleSelectNone() {
        controller.getTreeView().getSelectionModel().clearSelection();
    }

    private void handleShow() {
        innerGroup.getChildren().clear();
        innerGroup.getTransforms().clear();
        contentGroup.getTransforms().clear();

        for (TreeItem<ANode> item : controller.getTreeView().getSelectionModel().getSelectedItems()) {
            ANode node = item.getValue();
            if (node == null) continue;

            for (String fileId : node.fileIds()) {
                Group modelGroup = loadedModels.computeIfAbsent(fileId, id -> {
                    try {
                        var url = getClass().getResource("/HumanAnatomy/BodyParts/" + id + ".obj");
                        if (url != null) {
                            return ObjIO.openObjFile(new File(url.toURI()));
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading model: " + id);
                        e.printStackTrace();
                    }
                    return null;
                });

                if (modelGroup != null && !innerGroup.getChildren().contains(modelGroup)) {
                    setupClickHandler(modelGroup, fileId);
                    innerGroup.getChildren().add(modelGroup);
                }
            }
        }

        applyDrawModeBasedOnSelection();

        Platform.runLater(() -> {
            innerGroup.applyCss();
            innerGroup.layout();
            centerContentGroup();
            autoAdjustCamera();
        });

        setup3DScene();
    }

    private void setupClickHandler(Group modelGroup, String fileId) {
        for (javafx.scene.Node node : modelGroup.getChildren()) {
            if (node instanceof javafx.scene.shape.Shape3D shape) {
                shape.setPickOnBounds(true);
                shape.setOnMouseClicked(event -> {
                    handleModelClick(fileId, event);
                    event.consume();
                });
            }
        }
    }

    private void handleModelClick(String fileId, javafx.scene.input.MouseEvent event) {
        if (!event.isShiftDown() && !event.isControlDown()) {
            selectedFileIds.clear();
        }

        if (selectedFileIds.contains(fileId)) {
            selectedFileIds.remove(fileId);
        } else {
            selectedFileIds.add(fileId);
        }

        syncSelectionFromFileIds();
        applyDrawModeBasedOnSelection();
    }

    private void syncSelectionFromFileIds() {
        var tree = controller.getTreeView();
        var model = tree.getSelectionModel();
        inSelectionUpdate = true;
        model.clearSelection();
        selectMatchingItems(tree.getRoot(), model);
        inSelectionUpdate = false;
    }

    private void selectMatchingItems(TreeItem<ANode> item, MultipleSelectionModel<TreeItem<ANode>> model) {
        if (item.getValue() != null && item.getValue().fileIds().stream().anyMatch(selectedFileIds::contains)) {
            expandPathTo(item);
            model.select(item);
            Platform.runLater(() -> controller.getTreeView().scrollTo(controller.getTreeView().getRow(item)));
        }
        for (TreeItem<ANode> child : item.getChildren()) {
            selectMatchingItems(child, model);
        }
    }

    private void expandPathTo(TreeItem<ANode> item) {
        TreeItem<ANode> parent = item.getParent();
        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }
    }

    private void handleHide() {
        for (TreeItem<ANode> item : controller.getTreeView().getSelectionModel().getSelectedItems()) {
            ANode node = item.getValue();
            if (node == null) continue;
            for (String fileId : node.fileIds()) {
                innerGroup.getChildren().remove(loadedModels.get(fileId));
            }
        }
    }

    private void centerContentGroup() {
        innerGroup.layout();
        Bounds bounds = innerGroup.getBoundsInParent();
        double cx = (bounds.getMinX() + bounds.getMaxX()) / 2;
        double cy = (bounds.getMinY() + bounds.getMaxY()) / 2;
        double cz = (bounds.getMinZ() + bounds.getMaxZ()) / 2;
        innerGroup.getTransforms().setAll(new Translate(-cx, -cy, -cz));
    }

    private void autoAdjustCamera() {
        innerGroup.applyCss();
        innerGroup.layout();
        Bounds bounds = innerGroup.getBoundsInParent();
        double maxDim = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-maxDim * 2.2);
    }

    private void setup3DScene() {
        if (root3D.getChildren().isEmpty()) {
            contentGroup.getChildren().add(innerGroup);
            root3D.getChildren().add(contentGroup);

            PointLight pointLight = new PointLight(Color.WHITE);
            pointLight.setTranslateX(-500);
            pointLight.setTranslateY(-500);
            pointLight.setTranslateZ(-500);

            AmbientLight ambientLight = new AmbientLight(Color.DARKGRAY);
            root3D.getChildren().addAll(pointLight, ambientLight);

            camera.setNearClip(0.1);
            camera.setFarClip(10000);
            camera.setTranslateZ(-500);

            SubScene subScene = new SubScene(root3D, 600, 600, true, SceneAntialiasing.BALANCED);
            subScene.setCamera(camera);
            subScene.setFill(Color.LIGHTGRAY);

            subScene.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case Z -> interactionHandler.resetTransform();
                    case I -> interactionHandler.zoom(50);
                    case O -> interactionHandler.zoom(-50);
                    case LEFT -> interactionHandler.rotateY(-10);
                    case RIGHT -> interactionHandler.rotateY(10);
                    case UP -> interactionHandler.rotateX(-10);
                    case DOWN -> interactionHandler.rotateX(10);
                }
            });



            subScene.setOnMouseClicked(e -> subScene.requestFocus());
            subScene.widthProperty().bind(controller.getVisualizationPane().widthProperty());
            subScene.heightProperty().bind(controller.getVisualizationPane().heightProperty());

            controller.getVisualizationPane().getChildren().setAll(subScene);

            interactionHandler = new SceneInteractionHandler(contentGroup, camera);
            interactionHandler.setupMouseInteraction(controller.getVisualizationPane());
        }
    }


    private void applyDrawModeBasedOnSelection() {
        for (Map.Entry<String, Group> entry : loadedModels.entrySet()) {
            String fileId = entry.getKey();
            Group modelGroup = entry.getValue();
            boolean isSelected = selectedFileIds.contains(fileId);
            setDrawModeRecursive(modelGroup, isSelected ? DrawMode.FILL : DrawMode.LINE);
        }
    }
    private void setDrawModeRecursive(Group group, DrawMode mode) {
        for (javafx.scene.Node node : group.getChildren()) {
            if (node instanceof javafx.scene.shape.Shape3D shape) {
                shape.setDrawMode(mode);
            } else if (node instanceof Group subGroup) {
                setDrawModeRecursive(subGroup, mode);
            }
        }
    }

    private void handleColorChange() {
        Color newColor = controller.getColorPicker().getValue();
        for (String fileId : selectedFileIds) {
            Group group = loadedModels.get(fileId);
            if (group != null) {
                applyColorToFilledShapes(group, newColor);
            }
        }
    }

    private void applyColorToFilledShapes(Group group, Color color) {
        for (javafx.scene.Node node : group.getChildren()) {
            if (node instanceof javafx.scene.shape.Shape3D shape && shape.getDrawMode() == DrawMode.FILL) {
                if (shape.getMaterial() instanceof javafx.scene.paint.PhongMaterial phong) {
                    phong.setDiffuseColor(color);
                } else {
                    shape.setMaterial(new javafx.scene.paint.PhongMaterial(color));
                }
            } else if (node instanceof Group subGroup) {
                applyColorToFilledShapes(subGroup, color);
            }
        }
    }

    // ==== Search Handlers ====

    public boolean handleFind(String query) {
        query = query.trim().toLowerCase();
        searchResults.clear();
        currentSearchIndex = -1;

        if (query.isEmpty()) {
            controller.getSearchStatusLabel().setText("Please enter a search term.");
            return false;
        }

        findMatchingItems(controller.getTreeView().getRoot(), query);
        if (!searchResults.isEmpty()) {
            currentSearchIndex = 0;
            selectTreeItem(searchResults.get(currentSearchIndex));
            controller.getSearchStatusLabel().setText("Found " + searchResults.size() + " matches");
            return true;
        } else {
            controller.getSearchStatusLabel().setText("No match found for: \"" + query + "\"");
            return false;
        }
    }

    public boolean handleFind() {
        return handleFind(controller.getSearchTextField().getText());
    }

    public void handleFirst() {
        String query = controller.getSearchTextField().getText();
        if (searchResults.isEmpty() && !handleFind(query)) return;
        currentSearchIndex = 0;
        selectTreeItem(searchResults.get(currentSearchIndex));
        controller.getSearchStatusLabel().setText("Match 1 of " + searchResults.size());
    }

    public void handleNext() {
        String query = controller.getSearchTextField().getText();
        if (searchResults.isEmpty() && !handleFind(query)) return;
        currentSearchIndex = (currentSearchIndex + 1) % searchResults.size();
        selectTreeItem(searchResults.get(currentSearchIndex));
        controller.getSearchStatusLabel().setText("Match " + (currentSearchIndex + 1) + " of " + searchResults.size());
    }

    public void handleAll() {
        String query = controller.getSearchTextField().getText();
        if (searchResults.isEmpty() && !handleFind(query)) return;
        var model = controller.getTreeView().getSelectionModel();
        model.clearSelection();
        for (TreeItem<ANode> match : searchResults) model.select(match);
        controller.getSearchStatusLabel().setText(searchResults.size() + " matches selected");
    }

    private void findMatchingItems(TreeItem<ANode> item, String query) {
        if (item.getValue().name().toLowerCase().contains(query)) {
            searchResults.add(item);
        }
        for (TreeItem<ANode> child : item.getChildren()) {
            findMatchingItems(child, query);
        }
    }

    private void selectTreeItem(TreeItem<ANode> item) {
        var model = controller.getTreeView().getSelectionModel();
        model.clearSelection();
        model.select(item);
        controller.getTreeView().scrollTo(controller.getTreeView().getRow(item));
    }
}
