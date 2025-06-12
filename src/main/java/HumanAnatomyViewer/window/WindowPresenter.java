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

    private TreeItem<ANode> partOfRootItem;
    private TreeItem<ANode> isARootItem;

    private final Group innerGroup = new Group();   // actual models
    private final Group contentGroup = new Group(); // centered and rotated container
    private final Group root3D = new Group();       // top-level scene root

    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Map<String, Group> loadedModels = new HashMap<>();

    private Transform totalTransform = new Rotate();
    private double xPrev, yPrev;

    private List<TreeItem<ANode>> searchResults = new ArrayList<>();
    private int currentSearchIndex = -1;

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
        return item;
    }

    private void setupTreeSelection() {
        controller.getTreeView().getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<TreeItem<ANode>>) change -> {
                    // optional: log or update UI based on selection
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
        for (TreeItem<ANode> item : controller.getTreeView().getSelectionModel().getSelectedItems()) {
            expand(item);
        }
    }

    private void handleCollapse() {
        for (TreeItem<ANode> item : controller.getTreeView().getSelectionModel().getSelectedItems()) {
            collapse(item);
        }
    }

    private void expand(TreeItem<ANode> item) {
        if (item != null) {
            item.setExpanded(true);
            item.getChildren().forEach(this::expand);
        }
    }

    private void collapse(TreeItem<ANode> item) {
        if (item != null) {
            item.getChildren().forEach(this::collapse);
            item.setExpanded(false);
        }
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
        var selectedItems = controller.getTreeView().getSelectionModel().getSelectedItems();

        // Step 1: Clear inner group & transforms
        innerGroup.getChildren().clear();
        innerGroup.getTransforms().clear();
        contentGroup.getTransforms().clear();
        totalTransform = new Rotate(); // Reset accumulated rotation

        boolean anyNewModelLoaded = false;

        // Step 2: Load and add selected models
        for (TreeItem<ANode> item : selectedItems) {
            ANode node = item.getValue();
            if (node == null) continue;

            for (String fileId : node.fileIds()) {
                Group modelGroup = loadedModels.get(fileId);

                // If not loaded, load from file
                if (modelGroup == null) {
                    try {
                        String path = "/HumanAnatomy/BodyParts/" + fileId + ".obj";
                        var url = getClass().getResource(path);
                        if (url == null) continue;

                        File file = new File(url.toURI());
                        modelGroup = ObjIO.openObjFile(file);

                        if (modelGroup != null) {
                            loadedModels.put(fileId, modelGroup);
                            anyNewModelLoaded = true;
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to load model: " + fileId);
                        e.printStackTrace();
                        continue;
                    }
                }

                if (modelGroup != null && !innerGroup.getChildren().contains(modelGroup)) {
                    innerGroup.getChildren().add(modelGroup);
                }
            }
        }

        // Step 3: Allow layout pass, then recenter and adjust camera
        Platform.runLater(() -> {
            innerGroup.applyCss();
            innerGroup.layout();

            centerContentGroup();    // Properly reposition center to origin
            autoAdjustCamera();      // Zoom camera out based on new bounding box
        });

        setup3DScene(); // Ensure it's displayed
    }

    private void handleHide() {
        var selectedItems = controller.getTreeView().getSelectionModel().getSelectedItems();
        for (TreeItem<ANode> item : selectedItems) {
            ANode node = item.getValue();
            if (node == null) continue;
            for (String fileId : node.fileIds()) {
                Group model = loadedModels.get(fileId);
                if (model != null) innerGroup.getChildren().remove(model);
            }
        }
    }

    private void centerContentGroup() {

        innerGroup.layout();   // Trigger layout update
        Bounds bounds = innerGroup.getBoundsInParent();
        double cx = (bounds.getMinX() + bounds.getMaxX()) / 2;
        double cy = (bounds.getMinY() + bounds.getMaxY()) / 2;
        double cz = (bounds.getMinZ() + bounds.getMaxZ()) / 2;
        innerGroup.getTransforms().clear();
        innerGroup.getTransforms().add(new Translate(-cx, -cy, -cz));
    }

    private void autoAdjustCamera() {
        innerGroup.applyCss();
        innerGroup.layout();

        Bounds bounds = innerGroup.getBoundsInParent();

        double width = bounds.getWidth();
        double height = bounds.getHeight();
        double depth = bounds.getDepth();

        double maxDim = Math.max(Math.max(width, height), depth);
        double distance = maxDim * 2.2; // Increase multiplier for safety margin

        // Center camera at (0,0,-distance)
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-distance);
        System.out.println("Bounds: " + bounds);
        System.out.println("Camera Z: " + camera.getTranslateZ());

    }

    private void handleColorChange() {
        Color newColor = controller.getColorPicker().getValue();
        var selectedItems = controller.getTreeView().getSelectionModel().getSelectedItems();

        for (TreeItem<ANode> item : selectedItems) {
            ANode node = item.getValue();
            if (node == null) continue;

            for (String fileId : node.fileIds()) {
                Group modelGroup = loadedModels.get(fileId);
                if (modelGroup != null) {
                    applyColorToGroup(modelGroup, newColor);
                }
            }
        }
    }
    private void applyColorToGroup(Group group, Color color) {
        group.getChildren().forEach(node -> {
            if (node instanceof javafx.scene.shape.Shape3D shape) {
                var material = shape.getMaterial();
                if (material instanceof javafx.scene.paint.PhongMaterial phong) {
                    phong.setDiffuseColor(color);
                } else {
                    javafx.scene.paint.PhongMaterial newMaterial = new javafx.scene.paint.PhongMaterial(color);
                    shape.setMaterial(newMaterial);
                }
            } else if (node instanceof Group subGroup) {
                applyColorToGroup(subGroup, color); // Recurse into nested groups
            }
        });
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
                    case Z: resetView(); break;
                    case I: zoomIn(); break;
                    case O: zoomOut(); break;
                    case LEFT: rotateY(10); break;
                    case RIGHT: rotateY(-10); break;
                    case UP: rotateX(-10); break;
                    case DOWN: rotateX(10); break;
                }
            });


            subScene.setOnMouseClicked(e -> subScene.requestFocus());

            subScene.widthProperty().bind(controller.getVisualizationPane().widthProperty());
            subScene.heightProperty().bind(controller.getVisualizationPane().heightProperty());

            controller.getVisualizationPane().getChildren().clear();
            controller.getVisualizationPane().getChildren().add(subScene);

            setupMouseRotation(controller.getVisualizationPane());
            setupMouseScroll(controller.getVisualizationPane());

            contentGroup.getTransforms().setAll(new Rotate(0, Rotate.X_AXIS));
        }
    }
    private void rotateX(double angle) {
        applyGlobalRotation(contentGroup, Rotate.X_AXIS, angle);
    }

    private void rotateY(double angle) {
        applyGlobalRotation(contentGroup, Rotate.Y_AXIS, angle);
    }


    private void setupMouseRotation(Pane pane) {
        pane.setOnMousePressed(e -> {
            xPrev = e.getSceneX();
            yPrev = e.getSceneY();
        });

        pane.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - xPrev;
            double dy = e.getSceneY() - yPrev;

            Point3D axis = new Point3D(dy, -dx, 0);
            if (axis.magnitude() == 0) return;

            double angle = Math.sqrt(dx * dx + dy * dy) * 0.5;
            applyGlobalRotation(contentGroup, axis, angle);

            xPrev = e.getSceneX();
            yPrev = e.getSceneY();
        });
    }

    private void setupMouseScroll(Pane pane) {
        pane.setOnScroll(e -> {
            if (e.isShiftDown()) {
                moveCamera(e.getDeltaX(), e.getDeltaY());
            } else {
                if (e.getDeltaY() > 0) zoomIn();
                else zoomOut();
            }
        });
    }

    private void moveCamera(double dx, double dy) {
        camera.setTranslateX(camera.getTranslateX() - dx);
        camera.setTranslateY(camera.getTranslateY() - dy);
    }

    private void zoomIn() {
        zoomCameraTowardsObject(50);
    }

    private void zoomOut() {
        zoomCameraTowardsObject(-50);
    }

    private void zoomCameraTowardsObject(double zoomAmount) {
        Point3D camPos = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        Point3D center = getContentCenterInScene();
        Point3D direction = center.subtract(camPos).normalize();

        double minDistance = 50;  // Don't get closer than this
        double maxDistance = 2000; // Optional max distance

        double currentDistance = center.distance(camPos);
        double newDistance = currentDistance - zoomAmount;

        if (newDistance < minDistance || newDistance > maxDistance) {
            return; // Do not zoom if too close or too far
        }

        Point3D newPos = camPos.add(direction.multiply(zoomAmount));
        camera.setTranslateX(newPos.getX());
        camera.setTranslateY(newPos.getY());
        camera.setTranslateZ(newPos.getZ());
    }


    private Point3D getContentCenterInScene() {
        Bounds bounds = contentGroup.getBoundsInParent();
        return new Point3D(
                (bounds.getMinX() + bounds.getMaxX()) / 2.0,
                (bounds.getMinY() + bounds.getMaxY()) / 2.0,
                (bounds.getMinZ() + bounds.getMaxZ()) / 2.0
        );
    }

    private void applyGlobalRotation(Group group, Point3D axis, double angle) {
        Point3D pivot = getContentCenterInScene();
        Rotate rotate = new Rotate(angle, pivot.getX(), pivot.getY(), pivot.getZ(), axis);
        totalTransform = rotate.createConcatenation(totalTransform);
        group.getTransforms().setAll(totalTransform);
    }

    private void resetView() {
        totalTransform = new Rotate();  // Clear rotation
        contentGroup.getTransforms().setAll(totalTransform); // Apply cleared rotation

        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-500);

        // Re-center content relative to origin
        innerGroup.getTransforms().clear();
        centerContentGroup();  // Adds centering Translate


        xPrev = yPrev = 0;
    }


    // ==== Search Handlers ====

    public void handleFind() {
        String query = controller.getSearchTextField().getText().trim().toLowerCase();
        searchResults.clear();              // ✅ Always clear before new search
        currentSearchIndex = -1;

        if (query.isEmpty()) return;

        TreeItem<ANode> root = controller.getTreeView().getRoot();
        findMatchingItems(root, query);

        if (!searchResults.isEmpty()) {
            currentSearchIndex = 0;
            selectTreeItem(searchResults.get(currentSearchIndex));
        } else {
            System.out.println("No match found for: " + query);
        }
    }

    public void handleFirst() {
        if (searchResults.isEmpty()) {
            handleFind(); // ✅ Refresh results if they were stale or cleared
            return;
        }
        currentSearchIndex = 0;
        selectTreeItem(searchResults.get(currentSearchIndex));
    }

    public void handleNext() {
        if (searchResults.isEmpty()) {
            handleFind(); // ✅ Refresh results in case user didn't click "Find" again
            return;
        }

        currentSearchIndex = (currentSearchIndex + 1) % searchResults.size();
        selectTreeItem(searchResults.get(currentSearchIndex));
    }

    public void handleAll() {
        if (searchResults.isEmpty()) return;
        var selectionModel = controller.getTreeView().getSelectionModel();
        selectionModel.clearSelection();
        for (TreeItem<ANode> match : searchResults) {
            selectionModel.select(match);
        }
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
