package assignment6.window;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.Group;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;

public class MouseDragHandler {

    private static double xPrev;
    private static double yPrev;

    public static void enable(Pane pane, Group group,PerspectiveCamera camera) {
        pane.setOnMousePressed((MouseEvent e) -> {
            xPrev = e.getSceneX();
            yPrev = e.getSceneY();
        });

        pane.setOnMouseDragged((MouseEvent e) -> {
            double dx = e.getSceneX() - xPrev;
            double dy = e.getSceneY() - yPrev;

            if(e.isControlDown()){
                camera.setTranslateZ(camera.getTranslateZ() + dy);
            }else if(e.isShiftDown()){
                // Shift + drag = pan
                camera.setTranslateX(camera.getTranslateX() - dx);
                camera.setTranslateY(camera.getTranslateY() - dy);
            }else {
                // Regular drag = rotation
                double dragDistance = Math.sqrt(dx * dx + dy * dy);
                if (dragDistance == 0) return;

                Point3D axis = new Point3D(dy, dx, 0).normalize();
                double angle = dragDistance * 0.3;

                WindowPresenter.applyGlobalRotation(group, axis, angle);
            }

            xPrev = e.getSceneX();
            yPrev = e.getSceneY();
        });

        // Scroll handling
        pane.setOnScroll((ScrollEvent e) -> {
            if (e.isShiftDown()) {
                // Shift + scroll = pan
                camera.setTranslateX(camera.getTranslateX() - e.getDeltaX());
                camera.setTranslateY(camera.getTranslateY() - e.getDeltaY());
            } else {
                // Normal scroll = zoom (Z axis)
                camera.setTranslateZ(camera.getTranslateZ() + e.getDeltaY());
            }
        });
    }
}
