package HumanAnatomyViewer.window;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

/**
 * SceneInteractionHandler enables mouse interaction (rotation, zoom, pan)
 * for 3D content displayed in a JavaFX scene.
 */
public class SceneInteractionHandler {

    private final Group contentGroup;            // The group containing all 3D content
    private final PerspectiveCamera camera;      // The camera viewing the 3D scene

    private double xPrev, yPrev;                 // Tracks previous mouse coordinates for drag
    private Transform totalTransform = new Rotate(); // Stores the combined rotation transforms

    /**
     * Constructor initializing the handler with the content group and camera.
     */
    public SceneInteractionHandler(Group contentGroup, PerspectiveCamera camera) {
        this.contentGroup = contentGroup;
        this.camera = camera;
    }

    /**
     * Sets up mouse event listeners on the given pane for interaction.
     */
    public void setupMouseInteraction(Pane pane) {
        pane.setOnMousePressed(this::onMousePressed);  // Capture initial mouse press position
        pane.setOnMouseDragged(this::onMouseDragged);  // Rotate content on drag
        pane.setOnScroll(this::onScroll);              // Zoom or pan on scroll
    }

    /**
     * Called when mouse is pressed: record current position.
     */
    private void onMousePressed(MouseEvent e) {
        xPrev = e.getSceneX();
        yPrev = e.getSceneY();
    }

    /**
     * Called when mouse is dragged: applies rotation based on drag direction.
     */
    private void onMouseDragged(MouseEvent e) {
        double dx = e.getSceneX() - xPrev;
        double dy = e.getSceneY() - yPrev;

        // Determine rotation axis from mouse movement
        Point3D axis = new Point3D(dy, -dx, 0);
        if (axis.magnitude() == 0) return; // Ignore zero movement

        // Determine rotation angle
        double angle = Math.sqrt(dx * dx + dy * dy) * 0.5;

        // Use center of 3D content as pivot for rotation
        Point3D pivot = getContentCenter();

        // Create rotation transform and apply it to the existing transformation
        Rotate rotate = new Rotate(angle, pivot.getX(), pivot.getY(), pivot.getZ(), axis);
        totalTransform = rotate.createConcatenation(totalTransform);
        contentGroup.getTransforms().setAll(totalTransform);

        // Update previous mouse position
        xPrev = e.getSceneX();
        yPrev = e.getSceneY();
    }

    /**
     * Called when mouse wheel is scrolled: zooms in/out or pans if Shift is held.
     */
    private void onScroll(ScrollEvent e) {
        if (e.isShiftDown()) {
            // Panning when Shift is pressed
            camera.setTranslateX(camera.getTranslateX() - e.getDeltaX());
            camera.setTranslateY(camera.getTranslateY() - e.getDeltaY());
        } else {
            // Zoom in/out
            zoom(e.getDeltaY() > 0 ? 50 : -50);
        }
    }

    /**
     * Adjusts camera zoom by moving it closer to or farther from content center.
     */
    public void zoom(double zoomAmount) {
        Point3D camPos = new Point3D(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        Point3D center = getContentCenter();
        Point3D direction = center.subtract(camPos).normalize(); // Direction from camera to center

        double minDistance = 50;     // Prevent zooming in too close
        double maxDistance = 2000;   // Prevent zooming out too far

        double currentDistance = center.distance(camPos);
        double newDistance = currentDistance - zoomAmount;

        // Abort if new zoom level is out of bounds
        if (newDistance < minDistance || newDistance > maxDistance) return;

        // Calculate new camera position along the direction vector
        Point3D newPos = camPos.add(direction.multiply(zoomAmount));
        camera.setTranslateX(newPos.getX());
        camera.setTranslateY(newPos.getY());
        camera.setTranslateZ(newPos.getZ());
    }

    /**
     * Calculates the center point of the content group in 3D space.
     */
    private Point3D getContentCenter() {
        var bounds = contentGroup.getBoundsInParent();
        return new Point3D(
                (bounds.getMinX() + bounds.getMaxX()) / 2.0,
                (bounds.getMinY() + bounds.getMaxY()) / 2.0,
                (bounds.getMinZ() + bounds.getMaxZ()) / 2.0
        );
    }

    /**
     * Resets all accumulated transformations to identity (no rotation).
     */
    public void resetTransform() {
        totalTransform = new Rotate(); // Reset transform to identity
        contentGroup.getTransforms().setAll(totalTransform);
    }

    /**
     * Returns the current transformation applied to the content group.
     */
    public Transform getCurrentTransform() {
        return totalTransform;
    }

    /**
     * Rotates the content around the global X-axis by the given angle (in degrees).
     */
    public void rotateX(double angle) {
        applyGlobalRotation(Rotate.X_AXIS, angle);
    }

    /**
     * Rotates the content around the global Y-axis by the given angle (in degrees).
     */
    public void rotateY(double angle) {
        applyGlobalRotation(Rotate.Y_AXIS, angle);
    }

    /**
     * Applies rotation around a specified axis with respect to content center.
     */
    private void applyGlobalRotation(Point3D axis, double angle) {
        Point3D pivot = getContentCenter();
        Rotate rotate = new Rotate(angle, pivot.getX(), pivot.getY(), pivot.getZ(), axis);
        totalTransform = rotate.createConcatenation(totalTransform);
        contentGroup.getTransforms().setAll(totalTransform);
    }
}
