package assignment5.window;

import assignment5.model.ObjIO;
import assignment5.model.Axes;
import javafx.animation.PauseTransition;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.File;
import java.util.ArrayList;


public class WindowPresenter {

    private final assignment5.window.WindowController controller;

    private final Group root3D = new Group();         // Root node for the 3D scene
    private final Group contentGroup = new Group();   // Group holding the 3D axes or models
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private double xPrev; // Previous mouse X position for rotation
    private double yPrev; // Previous mouse Y position for rotation
    private final java.util.List<Group> meshViews = new ArrayList<>();


    public WindowPresenter(WindowController controller) {
        this.controller = controller;
        setup3DScene();      // Build axes, lights, camera
        hookUpControls();    // Connect UI controls
    }

    private void setup3DScene() {
        // === Axes ===
        Axes axes = new Axes(20);  // X/Y/Z visual axes
        contentGroup.getChildren().add(axes);

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
        camera.setTranslateZ(-150);

        // === SubScene for 3D rendering ===
        SubScene subScene = new SubScene(root3D, 600, 600, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.SKYBLUE);

        // === Attach to centerPane from FXML ===
        Pane centerPane = controller.getCenterPane();
        subScene.widthProperty().bind(centerPane.widthProperty());
        subScene.heightProperty().bind(centerPane.heightProperty());
        centerPane.getChildren().add(subScene);

        // Start with identity transform
        contentGroup.getTransforms().setAll(new Rotate(0, Rotate.X_AXIS));

        // === shortcuts ===
        // Add keyboard shortcuts (scene-level)
        subScene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT -> rotateY(10);
                case RIGHT -> rotateY(-10);
                case UP -> rotateX(-10);
                case DOWN -> rotateX(10);
                case Z -> camera.setTranslateZ(camera.getTranslateZ() + 50);
                case X -> camera.setTranslateZ(camera.getTranslateZ() - 50);
                case R -> resetView();
                case ESCAPE -> closeWindow();
                default -> {}
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
            camera.setTranslateZ(camera.getTranslateZ() + 50);
            highlightButton(controller.getZoomInButton());
        });

        controller.getZoomOutButton().setOnAction(e -> {
            camera.setTranslateZ(camera.getTranslateZ() - 50);
            highlightButton(controller.getZoomOutButton());
        });

        controller.getResetButton().setOnAction(e -> {
            resetView();
            highlightButton(controller.getResetButton());
        });

        // === Menu Items ===
        controller.getMenuRotateLeft().setOnAction(e -> rotateY(10));
        controller.getMenuRotateRight().setOnAction(e -> rotateY(-10));
        controller.getMenuRotateUp().setOnAction(e -> rotateX(-10));
        controller.getMenuRotateDown().setOnAction(e -> rotateX(10));

        controller.getMenuZoomIn().setOnAction(e -> camera.setTranslateZ(camera.getTranslateZ() + 50));
        controller.getMenuZoomOut().setOnAction(e -> camera.setTranslateZ(camera.getTranslateZ() - 50));
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
                double spacing = 300; // space between models
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    Point3D offset = new Point3D(i * spacing, 0, 0);;

                    Group model = ObjIO.openObjFile(file);
                    if (model != null) {
                        contentGroup.getChildren().add(model);
                        meshViews.add(model);
                    }
                }
            }
        });


    }

    private void closeWindow() {
        Stage stage = (Stage) controller.getCloseButton().getScene().getWindow();
        stage.close();
    }

    private void resetView() {
        totalTransform = new Rotate();  // reset to identity
        contentGroup.getTransforms().setAll(totalTransform);
        camera.setTranslateZ(-900);
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
