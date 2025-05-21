package assignment4.view;

import assignment4.model.ANode;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.*;
import java.util.function.Function;

public class DrawCladogram {

    /**
     * Draws a cladogram tree using the provided layout and dimensions.
     *
     * @param root          The root node of the tree (not directly used in this method).
     * @param nodePointMap  A map containing layout positions (unscaled) for each node.
     * @param width         The desired width of the drawing area.
     * @param height        The desired height of the drawing area.
     * @return              A JavaFX Group containing all graphical components (edges, nodes, labels).
     */
    public static Group apply(ANode root, Map<ANode, Point2D> nodePointMap, double width, double height) {
        Collection<Point2D> points = nodePointMap.values();

        // Add margin for labels on the right
        double labelMargin = 80;

        // Create a function to scale all node positions to fit the desired canvas size
        Function<Point2D, Point2D> scale = setupScaleFunction(points, width - labelMargin, height);

        // Groups to hold different visual elements
        Group edgeGroup = new Group();   // For edges (lines)
        Group nodeGroup = new Group();   // For node dots
        Group labelGroup = new Group();  // For leaf labels

        // Determine font size based on number of leaves (to avoid overlap)
        int leafCount = (int) nodePointMap.keySet().stream()
                .filter(n -> n.children().isEmpty())
                .count();
        double fontSize = Math.min(12, height / (double) leafCount);

        /**
         * DRAW EDGES
         * For each parent-child relationship, draw L-shaped connectors:
         *   - vertical line from parent to child’s Y-level
         *   - horizontal line from parent’s X to child’s X at child’s Y
         */
        for (ANode parent : nodePointMap.keySet()) {
            Point2D p1 = scale.apply(nodePointMap.get(parent));  // Scaled position of parent

            for (ANode child : parent.children()) {
                Point2D p2 = scale.apply(nodePointMap.get(child));  // Scaled position of child

                // Vertical segment
                Line vertical = new Line(p1.getX(), p1.getY(), p1.getX(), p2.getY());

                // Horizontal segment
                Line horizontal = new Line(p1.getX(), p2.getY(), p2.getX(), p2.getY());

                // Styling
                vertical.setStroke(Color.BLACK);
                horizontal.setStroke(Color.BLACK);
                vertical.setStrokeWidth(0.75);
                horizontal.setStrokeWidth(0.75);

                // Add to edge group
                edgeGroup.getChildren().addAll(vertical, horizontal);
            }
        }

        /**
         * DRAW NODES AND LABELS
         * Each node is represented by a small dot.
         * If it's a leaf, display its label next to it.
         */
        for (ANode node : nodePointMap.keySet()) {
            Point2D point = scale.apply(nodePointMap.get(node));

            // Draw the node as a small black circle
            Circle dot = new Circle(point.getX(), point.getY(), 2.5);
            dot.setFill(Color.BLACK);
            nodeGroup.getChildren().add(dot);

            // Draw label for leaf nodes
            if (node.children().isEmpty()) {
                Text label = new Text(point.getX() + 4, point.getY(), node.label());
                label.setFont(Font.font("Monospaced", fontSize)); // Use monospace for better alignment
                label.setFill(Color.BLACK);
                labelGroup.getChildren().add(label);
            }
        }

        // Combine all visual elements into one group and return
        return new Group(edgeGroup, nodeGroup, labelGroup);
    }

    /**
     * Creates a scaling function to map raw node positions to pixel positions
     * that fit within the desired drawing width and height.
     *
     * @param points The raw layout points of the tree nodes.
     * @param width  The available drawing width (excluding label margin).
     * @param height The available drawing height.
     * @return       A function that converts raw coordinates to scaled coordinates.
     */
    public static Function<Point2D, Point2D> setupScaleFunction(Collection<Point2D> points, double width, double height) {
        // Determine min and max values to normalize coordinates
        double xMin = points.stream().mapToDouble(Point2D::getX).min().orElse(0.0);
        double xMax = points.stream().mapToDouble(Point2D::getX).max().orElse(1.0);
        double yMin = points.stream().mapToDouble(Point2D::getY).min().orElse(0.0);
        double yMax = points.stream().mapToDouble(Point2D::getY).max().orElse(1.0);

        // Return scaling function: normalizes and maps points to given width/height
        return (Point2D p) -> new Point2D(
                (p.getX() - xMin) / (xMax - xMin) * width,
                (p.getY() - yMin) / (yMax - yMin) * height
        );
    }
}
