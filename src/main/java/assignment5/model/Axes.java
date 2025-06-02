package assignment5.model;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Axes class that creates 3 cylinders representing coordinate axes.
 * The cylinders are aligned so that their bases meet at the origin,
 * effectively "gluing" their ends together.
 */
public class Axes extends Group {

    // Ratio of the axis radius to the length of the axis
    private static final double AXIS_RADIUS_RATIO = 0.05;

    /**
     * Constructor to create axes with specified length.
     * @param length Length of each axis cylinder.
     */
    public Axes(double length) {
        double radius = length * AXIS_RADIUS_RATIO;

        // Create red X axis group
        Group xAxisGroup = new Group();
        Cylinder xAxis = new Cylinder(radius, length);
        xAxis.setMaterial(makeMaterial(Color.RED));
        // Move cylinder so base is at origin: translate by length/2 along y in local coords
        xAxis.getTransforms().add(new Translate(0, length / 2, 0));
        // Rotate group to align cylinder along z axis
        xAxisGroup.getTransforms().add(new Rotate(-90, Rotate.Z_AXIS));
        xAxisGroup.getChildren().add(xAxis);

        // Create green Y axis group
        Group yAxisGroup = new Group();
        Cylinder yAxis = new Cylinder(radius, length);
        yAxis.setMaterial(makeMaterial(Color.GREEN));
        // Move cylinder so base is at origin
        yAxis.getTransforms().add(new Translate(0, length / 2, 0));
        // Y axis is default orientation
        yAxisGroup.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        yAxisGroup.getChildren().add(yAxis);

        // Create blue Z axis group
        Group zAxisGroup = new Group();
        Cylinder zAxis = new Cylinder(radius, length);
        zAxis.setMaterial(makeMaterial(Color.BLUE));
        // Move cylinder so base is at origin
        zAxis.getTransforms().add(new Translate(0, length / 2, 0));
        // Rotate group to align cylinder along Z axis
        zAxisGroup.getTransforms().addAll(
                new Rotate(-90, Rotate.X_AXIS),
                new Rotate(-180, Rotate.Z_AXIS));
        zAxisGroup.getChildren().add(zAxis);

        // Add axis groups to the main group
        this.getChildren().addAll(xAxisGroup, yAxisGroup, zAxisGroup);


    }

    /**
     * Helper method to create a PhongMaterial with given color.
     */
    private PhongMaterial makeMaterial(Color color) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(Color.WHITE);
        return material;
    }
}

