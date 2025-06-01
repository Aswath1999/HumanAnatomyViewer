package assignment5.window;

import assignment5.model.ObjIO;
import assignment5.model.ObjParser;
import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.paint.PhongMaterial;
import java.io.File;
import javafx.scene.image.Image;

public class WindowPresenter {

    private final WindowController controller;

    // Root 3D scene graph nodes
    private final Group root3D = new Group();       // Full 3D scene root
    private final Group contentGroup = new Group(); // Holds model and axes (everything that's transformable)
    private final PerspectiveCamera camera = new PerspectiveCamera(true); // Perspective camera for 3D view

    private static final double AXES_LENGTH = 200.0; // Reference size for the axes and bounding cube

    public WindowPresenter(WindowController controller) {
        this.controller = controller;
        setup3DScene();     // Initialize the 3D scene with camera, lighting, and axes
        hookUpControls();   // Link UI controls to behavior
    }

    /**
     * Set up the 3D scene components:
     * - Axes
     * - Lighting
     * - Camera
     * - SubScene embedded in the UI
     */
    private void setup3DScene() {
        // Add coordinate axes (X: red, Y: green, Z: blue)
        Axes axes = new Axes(20);
        contentGroup.getChildren().add(axes);

        // Add point light to simulate directional lighting
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(-500);
        pointLight.setTranslateY(-500);
        pointLight.setTranslateZ(-500);

        // Add ambient light for even base illumination
        AmbientLight ambientLight = new AmbientLight(Color.DARKGRAY);
        root3D.getChildren().addAll(contentGroup, pointLight, ambientLight);

        // Set up perspective camera
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setTranslateZ(-900); // Pull back to view content

        // Create a SubScene for rendering 3D content
        SubScene subScene = new SubScene(root3D, 600, 600, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.SKYBLUE); // Background color

        // Bind subScene size to UI pane and add to layout
        Pane centerPane = controller.getCenterPane();
        subScene.widthProperty().bind(centerPane.widthProperty());
        subScene.heightProperty().bind(centerPane.heightProperty());
        centerPane.getChildren().add(subScene);

        // Initial transform (identity)
        contentGroup.getTransforms().setAll(new Rotate(0, Rotate.X_AXIS));
    }

    /**
     * Connect all UI controls (buttons and menu items) to their respective 3D operations.
     */
    private void hookUpControls() {
        // Buttons: rotate, zoom, reset
        controller.getRotateLeftButton().setOnAction(e -> {
            rotateY(-10); // Rotate around Y axis to the left (CCW)
            highlightButton(controller.getRotateLeftButton());
        });

        controller.getRotateRightButton().setOnAction(e -> {
            rotateY(10); // Rotate around Y axis to the right (CW)
            highlightButton(controller.getRotateRightButton());
        });

        controller.getRotateUpButton().setOnAction(e -> {
            rotateX(10); // Tilt up (rotate around global X axis)
            highlightButton(controller.getRotateUpButton());
        });

        controller.getRotateDownButton().setOnAction(e -> {
            rotateX(-10); // Tilt down
            highlightButton(controller.getRotateDownButton());
        });

        controller.getZoomInButton().setOnAction(e -> {
            camera.setTranslateZ(camera.getTranslateZ() + 50); // Move camera closer
            highlightButton(controller.getZoomInButton());
        });

        controller.getZoomOutButton().setOnAction(e -> {
            camera.setTranslateZ(camera.getTranslateZ() - 50); // Move camera farther
            highlightButton(controller.getZoomOutButton());
        });

        controller.getResetButton().setOnAction(e -> {
            resetView(); // Reset camera and rotation
            highlightButton(controller.getResetButton());
        });

        // Menu items (same actions as buttons, no highlight)
        controller.getMenuRotateLeft().setOnAction(e -> rotateY(10));
        controller.getMenuRotateRight().setOnAction(e -> rotateY(-10));
        controller.getMenuRotateUp().setOnAction(e -> rotateX(-10));
        controller.getMenuRotateDown().setOnAction(e -> rotateX(10));
        controller.getMenuZoomIn().setOnAction(e -> camera.setTranslateZ(camera.getTranslateZ() + 50));
        controller.getMenuZoomOut().setOnAction(e -> camera.setTranslateZ(camera.getTranslateZ() - 50));
        controller.getMenuReset().setOnAction(e -> resetView());

        // File menu: open and close
        controller.getCloseButton().setOnAction(e -> closeWindow());
        controller.getMenuClose().setOnAction(e -> closeWindow());
        controller.getMenuOpen().setOnAction(e -> openObjFile());
    }

    // Close the window by accessing the stage
    private void closeWindow() {
        Stage stage = (Stage) controller.getCloseButton().getScene().getWindow();
        stage.close();
    }

    // Reset the view to default camera position and no rotation
    private void resetView() {
        contentGroup.getTransforms().clear();
        camera.setTranslateZ(-900);
    }

    // Apply rotation along screen-aligned X axis
    private void rotateX(double angle) {
        applyGlobalRotation(contentGroup, Rotate.X_AXIS, angle);
    }

    // Apply rotation along screen-aligned Y axis
    private void rotateY(double angle) {
        applyGlobalRotation(contentGroup, Rotate.Y_AXIS, angle);
    }

    /**
     * Apply a global (screen-space) rotation around a fixed axis.
     * New rotations are added at the front of the list, so they are not affected by existing ones.
     */
    private static void applyGlobalRotation(Group group, Point3D axis, double angle) {
        Rotate rotate = new Rotate(angle, axis);
        group.getTransforms().add(0, rotate); // Prepend for global effect
    }

    /**
     * Adds a temporary visual border effect to indicate which button was clicked.
     */
    private void highlightButton(Button button) {
        button.setStyle("-fx-border-color: blue;");
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> button.setStyle("-fx-border-color: transparent;"));
        pause.play();
    }

    /**
     * Load and display an OBJ 3D model:
     * - Center it at origin
     * - Uniformly scale it to fit the axes box
     * - Apply material and texture
     */
    private void openObjFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open OBJ File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ Files", "*.obj"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                // ✅ Load mesh using ObjIO
                TriangleMesh mesh = ObjIO.loadMesh(file);


                // Extract points array
                float[] points = mesh.getPoints().toArray(null);

                // Choose the corner vertex to align to (0,0,0)
                int cornerVertexIndex = 7 - 1; // Vertex 7 in OBJ (indexing from 0), gets vertex of (0,0,0)
                // gets the index from the vertex, as every vertex contains 3 (x,y,z) coordinates, thats why times 3
                // so 6 * 3 = 18, which is the index of the vertex (0) of position x
                float cornerX = points[cornerVertexIndex * 3];
                float cornerY = points[cornerVertexIndex * 3 + 1];
                float cornerZ = points[cornerVertexIndex * 3 + 2];


                // Create material
                PhongMaterial material = new PhongMaterial();
                material.setSpecularColor(Color.WHITE);

                String texturePath = file.getAbsolutePath().replace(".obj", ".png");
                File textureFile = new File(texturePath);

                if (textureFile.exists()) {
                    Image textureImage = new Image(textureFile.toURI().toString());
                    material.setDiffuseMap(textureImage);
                } else {
                    material.setDiffuseColor(Color.GREEN);
                }

                // Create MeshView
                MeshView meshView = new MeshView(mesh);
                meshView.setMaterial(material);

                // Translate so that chosen corner is at origin
                meshView.setTranslateX(-cornerX);
                meshView.setTranslateY(-cornerY);
                meshView.setTranslateZ(-cornerZ);

                // TEMP group for bounds calculation
     /*           Group tempGroup = new Group(meshView);
                Scene tempScene = new Scene(tempGroup); // Forces layout / bounds update
                Bounds bounds = meshView.getBoundsInParent();

                // Align min corner to origin
                meshView.setTranslateX(-bounds.getMinX());
                meshView.setTranslateY(-bounds.getMinY());
                meshView.setTranslateZ(-bounds.getMinZ());

                // Scale to fit
                double maxDim = Math.max(bounds.getWidth(), Math.max(bounds.getHeight(), bounds.getDepth()));
                double scale = AXES_LENGTH / maxDim;

                Group modelGroup = new Group(meshView);
                modelGroup.setScaleX(scale);
                modelGroup.setScaleY(scale);
                modelGroup.setScaleZ(scale);*/

                Group modelGroup = new Group(meshView);
                // Clear previous model (preserve axes)
                contentGroup.getTransforms().clear();
                if (contentGroup.getChildren().size() > 1) {
                    contentGroup.getChildren().remove(1, contentGroup.getChildren().size());
                }

                contentGroup.getChildren().add(modelGroup);

                // Optional view rotation
                contentGroup.getTransforms().addAll(
                        new Rotate(-45, Rotate.Y_AXIS),
                        new Rotate(-35.26, Rotate.X_AXIS)
                );

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
