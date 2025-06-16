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

    private final Stage stage;
    private final WindowController controller;
    private final Model model;

    private TreeItem<ANode> partOfRootItem;
    private TreeItem<ANode> isARootItem;

    private final Group innerGroup = new Group();
    private final Group contentGroup = new Group();
    private final Group root3D = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    private SceneInteractionHandler interactionHandler;
    private ModelInterface modelInterface;
    private TreeSearchHandler searchHandler;

    public WindowPresenter(Stage stage, WindowController controller, Model model) {
        this.stage = stage;
        this.controller = controller;
        this.model = model;

        this.modelInterface = new ModelInterface(innerGroup, controller.getTreeView());
        this.searchHandler = new TreeSearchHandler(controller.getTreeView(), controller.getSearchStatusLabel());

        initializeTrees();
        setupTreeSelectionListener();
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

    private void setupTreeSelectionListener() {
        controller.getTreeView().getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<TreeItem<ANode>>) change -> {
                    if (modelInterface.isInSelectionUpdate()) return;

                    modelInterface.getSelectedFileIds().clear();
                    for (TreeItem<ANode> item : controller.getTreeView().getSelectionModel().getSelectedItems()) {
                        if (item.getValue() != null) {
                            modelInterface.getSelectedFileIds().addAll(item.getValue().fileIds());
                        }
                    }
                    modelInterface.applyDrawModeBasedOnSelection();
                }
        );
    }

    private void setupButtonHandlers() {
        controller.getIsAButton().setOnAction(e -> controller.getTreeView().setRoot(isARootItem));
        controller.getPartOfButton().setOnAction(e -> controller.getTreeView().setRoot(partOfRootItem));
        controller.getExpandButton().setOnAction(e -> expandSelected());
        controller.getCollapseButton().setOnAction(e -> collapseSelected());
        controller.getSelectButton().setOnAction(e -> selectAllDescendants());
        controller.getDeselectButton().setOnAction(e -> controller.getTreeView().getSelectionModel().clearSelection());
        controller.getShowButton().setOnAction(e -> handleShow());
        controller.getHideButton().setOnAction(e -> modelInterface.hideModels(controller.getTreeView().getSelectionModel().getSelectedItems()));
        controller.getColorPicker().setOnAction(e -> modelInterface.applyColorToSelected(controller.getColorPicker().getValue()));

        controller.getFindButton().setOnAction(e -> handleFind());
        controller.getFirstButton().setOnAction(e -> handleFirst());
        controller.getNextButton().setOnAction(e -> handleNext());
        controller.getAllButton().setOnAction(e -> handleAll());
        controller.getSearchTextField().setOnAction(e -> handleFind());
    }

    private void expandSelected() {
        controller.getTreeView().getSelectionModel().getSelectedItems().forEach(this::expand);
    }

    private void collapseSelected() {
        controller.getTreeView().getSelectionModel().getSelectedItems().forEach(this::collapse);
    }

    private void expand(TreeItem<ANode> item) {
        item.setExpanded(true);
        item.getChildren().forEach(this::expand);
    }

    private void collapse(TreeItem<ANode> item) {
        item.getChildren().forEach(this::collapse);
        item.setExpanded(false);
    }

    private void selectAllDescendants() {
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

    private void handleShow() {
        modelInterface.loadAndDisplayModels(controller.getTreeView().getSelectionModel().getSelectedItems());

        Platform.runLater(() -> {
            innerGroup.applyCss();
            innerGroup.layout();
            centerContentGroup();
            autoAdjustCamera();
        });

        setup3DScene();
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

    // ==== Search Handlers (delegated to TreeSearchHandler) ====

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
