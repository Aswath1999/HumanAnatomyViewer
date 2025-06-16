package HumanAnatomyViewer.window;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

public class SceneInteractionHandler {

    private final Group contentGroup;
    private final PerspectiveCamera camera;

    private double xPrev, yPrev;
    private Transform totalTransform = new Rotate();

    public SceneInteractionHandler(Group contentGroup, PerspectiveCamera camera) {
        this.contentGroup = contentGroup;
        this.camera = camera;
    }

    public void setupMouseInteraction(Pane pane) {
        pane.setOnMousePressed(this::onMousePressed);
        pane.setOnMouseDragged(this::onMouseDragged);
        pane.setOnScroll(this::onScroll);
    }

    private void onMousePressed(MouseEvent e) {
        xPrev = e.getSceneX();
        yPrev = e.getSceneY();
    }

    private void onMouseDragged(MouseEvent e) {
        double dx = e.getSceneX() - xPrev;
        double dy = e.getSceneY() - yPrev;

        Point3D axis = new Point3D(dy, -dx, 0);
        if (axis.magnitude() == 0) return;

        double angle = Math.sqrt(dx * dx + dy * dy) * 0.5;
        Point3D pivot = getContentCenter();

        Rotate rotate = new Rotate(angle, pivot.getX(), pivot.getY(), pivot.getZ(), axis);
        totalTransform = rotate.createConcatenation(totalTransform);
        contentGroup.getTransforms().setAll(totalTransform);

        xPrev = e.getSceneX();
        yPrev = e.getSceneY();
    }

    private void onScroll(ScrollEvent e) {
        if (e.isShiftDown()) {
            camera.setTranslateX(camera.getTranslateX() - e.getDeltaX());
            camera.setTranslateY(camera.getTranslateY() - e.getDeltaY());
        } else {
            zoom(e.getDeltaY() > 0 ? 50 : -50);
        }
    }

    public void zoom(double zoomAmount) {
        Point3D camPos = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        Point3D center = getContentCenter();
        Point3D direction = center.subtract(camPos).normalize();

        double minDistance = 50;
        double maxDistance = 2000;

        double currentDistance = center.distance(camPos);
        double newDistance = currentDistance - zoomAmount;

        if (newDistance < minDistance || newDistance > maxDistance) return;

        Point3D newPos = camPos.add(direction.multiply(zoomAmount));
        camera.setTranslateX(newPos.getX());
        camera.setTranslateY(newPos.getY());
        camera.setTranslateZ(newPos.getZ());
    }

    private Point3D getContentCenter() {
        var bounds = contentGroup.getBoundsInParent();
        return new Point3D(
                (bounds.getMinX() + bounds.getMaxX()) / 2.0,
                (bounds.getMinY() + bounds.getMaxY()) / 2.0,
                (bounds.getMinZ() + bounds.getMaxZ()) / 2.0
        );
    }

    public void resetTransform() {
        totalTransform = new Rotate();
        contentGroup.getTransforms().setAll(totalTransform);
    }

    public Transform getCurrentTransform() {
        return totalTransform;
    }


    public void rotateX(double angle) {
        applyGlobalRotation(Rotate.X_AXIS, angle);
    }

    public void rotateY(double angle) {
        applyGlobalRotation(Rotate.Y_AXIS, angle);
    }

    private void applyGlobalRotation(Point3D axis, double angle) {
        Point3D pivot = getContentCenter();
        Rotate rotate = new Rotate(angle, pivot.getX(), pivot.getY(), pivot.getZ(), axis);
        totalTransform = rotate.createConcatenation(totalTransform);
        contentGroup.getTransforms().setAll(totalTransform);
    }


}
