package assignment5.window;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.PointLight;
import javafx.scene.AmbientLight;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
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

    private final Group root3D = new Group();         // Root node for the 3D scene
    private final Group contentGroup = new Group();   // Group holding the 3D axes or models
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    public WindowPresenter(WindowController controller) {
        this.controller = controller;
        setup3DScene();      // Build axes, lights, camera
        hookUpControls();    // Connect UI controls
    }

    private void setup3DScene() {
        // === Axes ===
        Axes axes = new Axes(200);  // X/Y/Z visual axes
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
        camera.setTranslateZ(-900);

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
    }

    private void hookUpControls() {
        // === Buttons ===
        controller.getRotateLeftButton().setOnAction(e -> {
            rotateY(-10);
            highlightButton(controller.getRotateLeftButton());
        });

        controller.getRotateRightButton().setOnAction(e -> {
            rotateY(10);
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
        controller.getMenuRotateLeft().setOnAction(e -> rotateY(-10));
        controller.getMenuRotateRight().setOnAction(e -> rotateY(10));
        controller.getMenuRotateUp().setOnAction(e -> rotateX(-10));
        controller.getMenuRotateDown().setOnAction(e -> rotateX(10));

        controller.getMenuZoomIn().setOnAction(e -> camera.setTranslateZ(camera.getTranslateZ() + 50));
        controller.getMenuZoomOut().setOnAction(e -> camera.setTranslateZ(camera.getTranslateZ() - 50));
        controller.getMenuReset().setOnAction(e -> resetView());

        // === Close ===
        controller.getCloseButton().setOnAction(e -> closeWindow());
        controller.getMenuClose().setOnAction(e -> closeWindow());

        controller.getMenuOpen().setOnAction(e -> openObjFile());

    }

    private void closeWindow() {
        Stage stage = (Stage) controller.getCloseButton().getScene().getWindow();
        stage.close();
    }

    private void resetView() {
        contentGroup.getTransforms().setAll(new Rotate(0, Rotate.X_AXIS));
        camera.setTranslateZ(-900);
    }

    private void rotateX(double angle) {
        applyGlobalRotation(contentGroup, Rotate.X_AXIS, angle);
    }

    private void rotateY(double angle) {
        applyGlobalRotation(contentGroup, Rotate.Y_AXIS, angle);
    }

    private static void applyGlobalRotation(Group contentGroup, Point3D axis, double angle) {
        Transform currentTransform = contentGroup.getLocalToParentTransform();
        Rotate rotate = new Rotate(angle, axis);
        Transform newTransform = rotate.createConcatenation(currentTransform);
        contentGroup.getTransforms().setAll(newTransform);
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
    private void openObjFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open OBJ File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ Files", "*.obj"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                TriangleMesh mesh = ObjParser.load(file.getAbsolutePath());
                MeshView meshView = new MeshView(mesh);

                // === Material setup ===
                PhongMaterial material = new PhongMaterial();
                material.setSpecularColor(Color.WHITE); // Highlight
                String objPath = file.getAbsolutePath();
                String texturePath = objPath.substring(0, objPath.lastIndexOf('.')) + ".png";
                File textureFile = new File(texturePath);

                if (textureFile.exists()) {
                    Image textureImage = new Image(textureFile.toURI().toString());
                    material.setDiffuseMap(textureImage);
                } else {
                    material.setDiffuseColor(Color.GREEN);
                }

                meshView.setMaterial(material);

                // === Clear previous model (keep axes) ===
                contentGroup.getTransforms().clear(); // Reset rotation
                if (contentGroup.getChildren().size() > 1) {
                    contentGroup.getChildren().remove(1, contentGroup.getChildren().size());
                }

                // === Align corner to origin ===
                Bounds bounds = meshView.getBoundsInLocal();
                double minX = bounds.getMinX();
                double minY = bounds.getMinY();
                double minZ = bounds.getMinZ();

                meshView.setTranslateX(-minX);
                meshView.setTranslateY(-minY);
                meshView.setTranslateZ(-minZ);

                // === Wrap in group to isolate scale ===
                Group modelGroup = new Group(meshView);

                double maxDim = Math.max(bounds.getWidth(), Math.max(bounds.getHeight(), bounds.getDepth()));
                double scaleX = 200 / bounds.getWidth();
                double scaleY = 200 / bounds.getHeight();
                double scaleZ = 200 / bounds.getDepth();

                modelGroup.setScaleX(scaleX);
                modelGroup.setScaleY(scaleY);
                modelGroup.setScaleZ(scaleZ);


                // === Add to scene ===
                contentGroup.getChildren().add(modelGroup);

                // === (Optional) Rotate whole scene for better view ===
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
