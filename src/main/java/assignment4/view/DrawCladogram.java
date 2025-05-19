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

    public static Group apply(ANode root, Map<ANode, Point2D> nodePointMap, double width, double height) {
        Function<Point2D, Point2D> scale = setupScaleFunction(nodePointMap.values(), width, height);

        Group edgeGroup = new Group();
        Group nodeGroup = new Group();
        Group labelGroup = new Group();

        int leafCount = (int) nodePointMap.keySet().stream().filter(n -> n.children().isEmpty()).count();
        double fontSize = Math.min(12, height / (double) leafCount);

        // Draw L-shaped edges
        for (ANode parent : nodePointMap.keySet()) {
            Point2D p1 = scale.apply(nodePointMap.get(parent));
            for (ANode child : parent.children()) {
                Point2D p2 = scale.apply(nodePointMap.get(child));

                Line vertical = new Line(p1.getX(), p1.getY(), p1.getX(), p2.getY());
                Line horizontal = new Line(p1.getX(), p2.getY(), p2.getX(), p2.getY());

                vertical.setStroke(Color.BLACK);
                horizontal.setStroke(Color.BLACK);
                vertical.setStrokeWidth(0.75);
                horizontal.setStrokeWidth(0.75);

                edgeGroup.getChildren().addAll(vertical, horizontal);
            }
        }

        // Draw all node dots
        for (ANode node : nodePointMap.keySet()) {
            Point2D point = scale.apply(nodePointMap.get(node));
            Circle dot = new Circle(point.getX(), point.getY(), 2.5);
            dot.setFill(Color.BLACK);
            nodeGroup.getChildren().add(dot);

            // Label only for leaves
            if (node.children().isEmpty()) {
                Text label = new Text(point.getX() + 4, point.getY(), node.label());
                label.setFont(Font.font("Monospaced", fontSize));
                label.setFill(Color.BLACK);
                labelGroup.getChildren().add(label);
            }
        }

        return new Group(edgeGroup, nodeGroup, labelGroup);
    }

    public static Function<Point2D, Point2D> setupScaleFunction(Collection<Point2D> points, double width, double height) {
        double xMin = points.stream().mapToDouble(Point2D::getX).min().orElse(0.0);
        double xMax = points.stream().mapToDouble(Point2D::getX).max().orElse(1.0);
        double yMin = points.stream().mapToDouble(Point2D::getY).min().orElse(0.0);
        double yMax = points.stream().mapToDouble(Point2D::getY).max().orElse(1.0);

        double xRange = (xMax - xMin == 0) ? 1 : (xMax - xMin);
        double yRange = (yMax - yMin == 0) ? 1 : (yMax - yMin);

        return (Point2D p) -> new Point2D(
                (p.getX() - xMin) / xRange * width,
                (p.getY() - yMin) / yRange * height
        );
    }
}
