package assignment4.window;

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

    // method to draw the cladogram
    public static Group apply(ANode root, Map<ANode, Point2D> nodePointMap, double width, double height) {
        Collection<Point2D> points = nodePointMap.values();

        // Compute scaling function
        double labelMargin = 80;
        Function<Point2D, Point2D> scaleFn = setupScaleFunction(points, width-labelMargin, height);

        Group edgeGroup = new Group();
        Group nodeGroup = new Group();
        Group labelGroup = new Group();

        // loop through the nodePointMap and draw the edges
        for (Map.Entry<ANode, Point2D> entry : nodePointMap.entrySet()) {
            ANode parent = entry.getKey();
            // parent node
            Point2D parentScaled = scaleFn.apply(entry.getValue());

            // loop through the children of the parent node
            for (ANode child : parent.children()) {
                // check if the child node is in the nodePointMap
                if (!nodePointMap.containsKey(child)) continue;
                // child node
                Point2D childScaled = scaleFn.apply(nodePointMap.get(child));

                // draw the edge between the parent and child node
                double px = parentScaled.getX();
                double py = parentScaled.getY();
                double cx = childScaled.getX();
                double cy = childScaled.getY();

                // vertical line from parent (px, py) to (px, cy)
                Line vertical = new Line(px, py, px, cy);
                vertical.setStroke(Color.GRAY);

                // horizontal line from (px, cy) to child (cx, cy)
                Line horizontal = new Line(px, cy, cx, cy);
                horizontal.setStroke(Color.GRAY);

                // add the lines to the edge group
                edgeGroup.getChildren().addAll(vertical, horizontal);

            }
        }

        int leafCount = countLeaves(root);
        // Calculate font size based on the number of leaves
        double fontSize = Math.min(14, height / (leafCount + 1)); // Prevent overlap

        // loop through the nodePointMap and draw the nodes and labels
        for (Map.Entry<ANode, Point2D> entry : nodePointMap.entrySet()) {
            ANode node = entry.getKey();
            // node
            Point2D scaled = scaleFn.apply(entry.getValue());

            // draw the node
            Circle circle = new Circle(scaled.getX(), scaled.getY(), 2, Color.BLACK);
            nodeGroup.getChildren().add(circle);

            // if the node is a leaf node, draw the label
            if (node.children().isEmpty()) {
                Text label = new Text(node.name());
                label.setFont(new Font(fontSize));
                label.setX(scaled.getX() + 5);

                double labelHeight = label.getLayoutBounds().getHeight();
                label.setY(scaled.getY() + labelHeight / 4);

                labelGroup.getChildren().add(label);
            }
        }

        Group finalGroup = new Group(edgeGroup, nodeGroup, labelGroup);
        return finalGroup;
    }

    // method to set up the scaling function
    public static Function<Point2D, Point2D> setupScaleFunction(Collection<Point2D> points, double width, double height) {
        // find the min and max x and y values
        double xMin = points.stream().mapToDouble(Point2D::getX).min().orElse(0.0);
        double xMax = points.stream().mapToDouble(Point2D::getX).max().orElse(1.0);
        double yMin = points.stream().mapToDouble(Point2D::getY).min().orElse(0.0);
        double yMax = points.stream().mapToDouble(Point2D::getY).max().orElse(1.0);

        // return a function that scales the points to fit the width and height
        return (Point2D p) -> new Point2D(
                (p.getX() - xMin) / (xMax - xMin) * width,
                (p.getY() - yMin) / (yMax - yMin) * height
        );
    }

    // method to count the number of leaves in the cladogram
    private static int countLeaves(ANode node) {
        if (node.children().isEmpty()) return 1;
        return node.children().stream().mapToInt(DrawCladogram::countLeaves).sum();
    }
}


