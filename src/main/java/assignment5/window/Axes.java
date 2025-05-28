package assignment5.window;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Axes class that creates 3 cylinders representing coordinate axes.
 * The cylinders originate from the corner of the cube and stop near its surface.
 */
public class Axes extends Group {

    // Ratio of the axis radius to the length of the axis
    private static final double AXIS_RADIUS_RATIO = 0.05;

    /**
     * Constructor to create axes with specified length.
     * @param length Length from origin to end of each axis.
     */
    public Axes(double length) {
        double radius = length * AXIS_RADIUS_RATIO;

        // Apply a small margin to pull axis tips inward from exact cube face
        double effectiveLength = length * 0.98;  // Slightly shorter to avoid visual overshoot

        // Red X Axis
        Group xAxisGroup = new Group();
        Cylinder xAxis = new Cylinder(radius, effectiveLength);
        xAxis.setMaterial(makeMaterial(Color.RED));
        xAxis.getTransforms().add(new Translate(0, effectiveLength / 2, 0));
        xAxisGroup.getTransforms().add(new Rotate(-90, Rotate.Z_AXIS));
        xAxisGroup.getChildren().add(xAxis);

        // Green Y Axis
        Group yAxisGroup = new Group();
        Cylinder yAxis = new Cylinder(radius, effectiveLength);
        yAxis.setMaterial(makeMaterial(Color.GREEN));
        yAxis.getTransforms().add(new Translate(0, effectiveLength / 2, 0));
        yAxisGroup.getTransforms().add(new Rotate(0, Rotate.Z_AXIS)); // Default
        yAxisGroup.getChildren().add(yAxis);

        // Blue Z Axis
        Group zAxisGroup = new Group();
        Cylinder zAxis = new Cylinder(radius, effectiveLength);
        zAxis.setMaterial(makeMaterial(Color.BLUE));
        zAxis.getTransforms().add(new Translate(0, effectiveLength / 2, 0));
        zAxisGroup.getTransforms().add(new Rotate(-90, Rotate.X_AXIS));
        zAxisGroup.getChildren().add(zAxis);

        this.getChildren().addAll(xAxisGroup, yAxisGroup, zAxisGroup);
    }

    /**
     * Creates a PhongMaterial with given diffuse and specular color.
     */
    private PhongMaterial makeMaterial(Color color) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(Color.WHITE);
        return material;
    }
}
