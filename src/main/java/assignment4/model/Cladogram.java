package assignment4.model;

import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Point2D;
import java.util.function.Consumer;


public class Cladogram {
    public static Map<ANode, Point2D> layoutEqualLeafDepth(ANode root) {
        Map<ANode, Point2D> coordinates = new HashMap<>();
        int [] leafIndex = {0};

        postOrder(root, node -> {
            if(node.children().isEmpty()){
                coordinates.put(node, new Point2D(0, leafIndex[0]++));
            }else{
                double minX = Double.MAX_VALUE;
                double sumY = 0;
                for(ANode child: node.children()){
                    Point2D childPoint = coordinates.get(child);
                    if(childPoint != null){
                        minX = Math.min(minX, childPoint.getX());
                        sumY += childPoint.getY();
                    }
                    double avgY = sumY / node.children().size();
                    coordinates.put(node, new Point2D(minX, avgY));
                }
            }
        } );
        return coordinates;
    }

    // Uniform Edge Length layout
    public static Map<ANode, Point2D> layoutUniformEdgeLength(ANode root) {
        Map<ANode, Point2D> coordinates = new HashMap<>();
        int[] leafIndex = {0};

        // Step 1: Post-order traversal for Y (same as equal leaf depth)
        postOrder(root, node -> {
            if (node.children().isEmpty()) {
                coordinates.put(node, new Point2D(0, leafIndex[0]++));
            } else {
                double sumY = 0;
                for (ANode child : node.children()) {
                    sumY += coordinates.get(child).getY();
                }
                double avgY = sumY / node.children().size();
                coordinates.put(node, new Point2D(0, avgY));
            }
        });

        // Step 2: Pre-order traversal to assign X
        preOrder(root, 0, coordinates);

        return coordinates;
    }


    // Pre-order traversal helper to set x-coordinates
    private static void preOrder(ANode node, int x, Map<ANode, Point2D> coordinates) {
        Point2D old = coordinates.get(node);
        coordinates.put(node, new Point2D(x, old.getY()));
        for (ANode child : node.children()) {
            preOrder(child, x + 1, coordinates);
        }
    }

    private static void postOrder(ANode node, java.util.function.Consumer<ANode> visitor){
        for(ANode child : node.children())
        {
        postOrder(child, visitor);
        }
        visitor.accept(node);
    }

}
