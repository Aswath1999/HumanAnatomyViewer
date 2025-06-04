package assignment6.window;

import assignment6.model.ObjIO;
import assignment6.model.Axes;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.File;
import java.util.ArrayList;


public class WindowPresenter {

    private final assignment6.window.WindowController controller;

    private final Group root3D = new Group();         // Root node for the 3D scene
    private final Group contentGroup = new Group();   // Group holding the 3D axes or models
    private final Group innerGroup = new Group();     // Inner group for transformations
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private double xPrev; // Previous mouse X position for rotation
    private double yPrev; // Previous mouse Y position for rotation
    private final java.util.List<Group> meshViews = new ArrayList<>();
    private Axes axes; // Axes object for 3D scene


    public WindowPresenter(WindowController controller) {
        this.controller = controller;
        setup3DScene();      // Build axes, lights, camera
        hookUpControls();    // Connect UI controls
    }

    private void setup3DScene() {
        // === Axes ===
        // If you dont want the axis: press clear, that will also remove the axes
        Axes axes = new Axes(20);  // X/Y/Z visual axes
        innerGroup.getChildren().add(axes);
        contentGroup.getChildren().add(innerGroup); // Add inner group for transformations

        // === Lighting ===
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(-500);
        pointLight.setTranslateY(-500);
        pointLight.setTranslateZ(-500);

        AmbientLight ambientLight = new AmbientLight(Color.DARKGRAY);
        root3D.getChildren().addAll(contentGroup, pointLight, ambientLight);

        // === Camera ===
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setTranslateZ(-200);

        // === SubScene for 3D rendering ===
        SubScene subScene = new SubScene(root3D, 600, 600, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.SKYBLUE);

        // === Attach to centerPane from FXML ===
        Pane centerPane = controller.getCenterPane();
        subScene.widthProperty().bind(centerPane.widthProperty());
        subScene.heightProperty().bind(centerPane.heightProperty());
        centerPane.getChildren().add(subScene);

        // Enable mouse rotation
        setupMouseRotation(controller.getCenterPane());

        // Enable mouse scroll zoom
        setupMouseScroll(centerPane);

        // Start with identity transform
        contentGroup.getTransforms().setAll(new Rotate(0, Rotate.X_AXIS));

        // === shortcuts ===
        // Add keyboard shortcuts (scene-level)
        subScene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case Z -> resetView();
                case I -> camera.setTranslateZ(camera.getTranslateZ() + 50);
                case O -> camera.setTranslateZ(camera.getTranslateZ() - 50);
                case LEFT -> rotateY(10);
                case RIGHT -> rotateY(-10);
                case UP -> rotateX(-10);
                case DOWN -> rotateX(10);
            }
        });

        // Request focus to receive key input
        subScene.setOnMouseClicked(e -> subScene.requestFocus());
    }

    // == Hook up UI controls to actions ==
    private void hookUpControls() {
        // === Buttons ===
        controller.getRotateLeftButton().setOnAction(e -> {
            rotateY(10);
            highlightButton(controller.getRotateLeftButton());
        });

        controller.getRotateRightButton().setOnAction(e -> {
            rotateY(-10);
            highlightButton(controller.getRotateRightButton());
        });

        controller.getRotateUpButton().setOnAction(e -> {
            rotateX(-10);
            highlightButton(controller.getRotateUpButton());
        });

        controller.getRotateDownButton().setOnAction(e -> {
            rotateX(10);
            highlightButton(controller.getRotateDownButton());
        });

        controller.getZoomInButton().setOnAction(e -> {
            zoomIn();
            highlightButton(controller.getZoomInButton());
        });

        controller.getZoomOutButton().setOnAction(e -> {
            zoomOut();
            highlightButton(controller.getZoomOutButton());
        });

        controller.getResetButton().setOnAction(e -> {
            resetView();
            highlightButton(controller.getResetButton());
        });

        controller.getClearButton().setOnAction(e -> clearModels());

        controller.getMenuClear().setOnAction(e -> clearModels());

        // === Menu Items ===
        controller.getMenuRotateLeft().setOnAction(e -> rotateY(10));
        controller.getMenuRotateRight().setOnAction(e -> rotateY(-10));
        controller.getMenuRotateUp().setOnAction(e -> rotateX(-10));
        controller.getMenuRotateDown().setOnAction(e -> rotateX(10));

        controller.getMenuZoomIn().setOnAction(e -> zoomIn());
        controller.getMenuZoomOut().setOnAction(e -> zoomOut());
        controller.getMenuReset().setOnAction(e -> resetView());

        // === Close ===
        controller.getCloseButton().setOnAction(e -> closeWindow());
        controller.getMenuClose().setOnAction(e -> closeWindow());

        controller.getMenuOpen().setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ Files", "*.obj"));

            java.util.List<File> files = fileChooser.showOpenMultipleDialog(null);
            if (files != null) {
                for (File f : files) {
                    System.out.println(f.getAbsolutePath());
                }
            } else {
                System.out.println("No files selected.");
            }

            if (files != null) {
                for (File file : files) {
                    Group model = ObjIO.openObjFile(file);
                    if (model != null) {
                        innerGroup.getChildren().add(model);
                        meshViews.add(model);
                    }
                }
            }
        });

        innerGroup.getChildren().addListener((InvalidationListener) e -> {
            Bounds bounds = innerGroup.getBoundsInLocal();
            double centerX = (bounds.getMinX() + bounds.getMaxX()) / 2.0;
            double centerY = (bounds.getMinY() + bounds.getMaxY()) / 2.0;
            double centerZ = (bounds.getMinZ() + bounds.getMaxZ()) / 2.0;

            innerGroup.getTransforms().setAll(new Translate(-centerX, -centerY, -centerZ));
        });



    }

    private void closeWindow() {
        Stage stage = (Stage) controller.getCloseButton().getScene().getWindow();
        stage.close();
    }

    // method to reset the view
    private void resetView() {
        totalTransform = new Rotate();  // reset to identity
        contentGroup.getTransforms().setAll(totalTransform);

        // Reset translation of content Group
        contentGroup.setTranslateX(0);
        contentGroup.setTranslateY(0);
        contentGroup.setTranslateZ(0);

        // Reset camera position
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-200); // Initial zoom level

        // reset rotation tracking
        xPrev = 0;
        yPrev = 0;

        // Center the content group after resetting
        centerContentGroup();
    }
    // == Zoom in/out methods ==
    private void zoomIn() {
        zoomCameraTowardsObject(50);
    }
    private void zoomOut() {
        zoomCameraTowardsObject(-50);
    }

    // Center the content group in the scene
    private void centerContentGroup() {
        // Calculate the center of the content group
        Bounds bounds = contentGroup.getBoundsInParent();
        double centerX = (bounds.getMinX() + bounds.getMaxX()) / 2.0;
        double centerY = (bounds.getMinY() + bounds.getMaxY()) / 2.0;
        double centerZ = (bounds.getMinZ() + bounds.getMaxZ()) / 2.0;

        // Translate the content group to center it in the scene
        contentGroup.setTranslateX(contentGroup.getTranslateX() - centerX);
        contentGroup.setTranslateY(contentGroup.getTranslateY() - centerY);
        contentGroup.setTranslateZ(contentGroup.getTranslateZ() - centerZ);
    }


    private void rotateX(double angle) {
        applyGlobalRotation(contentGroup, Rotate.X_AXIS, angle);
    }

    private void rotateY(double angle) {
        applyGlobalRotation(contentGroup, Rotate.Y_AXIS, angle);
    }

    // == Helper method to apply rotation around a global axis ==
    private Transform totalTransform = new Rotate();  // starts as identity

    private void applyGlobalRotation(Group contentGroup, Point3D axis, double angle) {
        Rotate rotate = new Rotate(angle, axis);
        totalTransform = rotate.createConcatenation(totalTransform);  // screen-space
        contentGroup.getTransforms().setAll(totalTransform);
    }

    // == Mouse Rotation Setup ==
    public void setupMouseRotation(Pane pane) {
        // Initialize previous mouse position
        pane.setOnMousePressed(e -> {
            xPrev = e.getSceneX();
            yPrev = e.getSceneY();
        });

        // Handle mouse drag to rotate the content group
        pane.setOnMouseDragged(e -> {
            // Calculate the change in mouse position
            double dx = e.getSceneX() - xPrev;
            double dy = e.getSceneY() - yPrev;

            // If no movement, do nothing
            Point3D axis = new Point3D(dy, -dx, 0);
            if (axis.magnitude() == 0) return;

            // Normalize the axis and calculate the angle based on distance
            double distance = Math.sqrt(dx * dx + dy * dy);
            double angle = distance * 0.5; // adjust sensitivity here

            // Apply rotation around the axis
            applyGlobalRotation(contentGroup, axis, angle);

            // Update previous mouse position
            xPrev = e.getSceneX();
            yPrev = e.getSceneY();
        });
    }

    // == set up mouse scroll zoom ==
    public void setupMouseScroll(Pane pane) {
        // Handle mouse scroll to zoom in/out
        pane.setOnScroll(e -> {
            // Prevent default scrolling behavior
            double deltaX = e.getDeltaX();
            double deltaY = e.getDeltaY();

            if (e.isShiftDown()) {
                // Pan: move camera in X and Y when Shift is held
                moveCamera(deltaX, deltaY);
            } else {
                if (deltaY > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        });
    }

    private void moveCamera(double dx, double dy) {
        // Move camera horizontally and vertically by -dx, -dy (invert to get correct pan direction)
        camera.setTranslateX(camera.getTranslateX() - dx);
        camera.setTranslateY(camera.getTranslateY() - dy);
        // Move contentGroup in opposite direction to camera to keep view stable (no rotation effect)
        contentGroup.setTranslateX(contentGroup.getTranslateX() + dx);
        contentGroup.setTranslateY(contentGroup.getTranslateY() + dy);
    }

    // Get the center point of the content group in scene coordinates
    private Point3D getContentCenterInScene() {
        Bounds bounds = contentGroup.getBoundsInParent();
        double centerX = (bounds.getMinX() + bounds.getMaxX()) / 2.0;
        double centerY = (bounds.getMinY() + bounds.getMaxY()) / 2.0;
        double centerZ = (bounds.getMinZ() + bounds.getMaxZ()) / 2.0;

        return new Point3D(centerX, centerY, centerZ);
    }

    // Zoom camera towards or away from the object's current center
    private void zoomCameraTowardsObject(double zoomAmount) {
        Point3D cameraPos = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        Point3D center = getContentCenterInScene();

        // Compute vector from camera to center
        Point3D vectorToCenter = center.subtract(cameraPos);

        // Normalize vector
        double length = vectorToCenter.magnitude();
        if (length == 0) return; // avoid division by zero

        Point3D direction = vectorToCenter.normalize();

        // Move camera along the direction vector by zoomAmount
        Point3D newCameraPos = cameraPos.add(direction.multiply(zoomAmount));
        camera.setTranslateX(newCameraPos.getX());
        camera.setTranslateY(newCameraPos.getY());
        camera.setTranslateZ(newCameraPos.getZ());

    }

    // == Clear all models from the scene ==
    private void clearModels() {
        innerGroup.getChildren().clear();  // Wipe everything, including axes and models
        meshViews.clear();
    }


    /**
     * Adds a temporary blue border around a clicked button.
     */
    private void highlightButton(Button button) {
        button.setStyle("-fx-border-color: blue; ");

        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> button.setStyle("-fx-border-color: transparent; "));
        pause.play();
    }

}
