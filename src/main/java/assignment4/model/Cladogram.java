package assignment4.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Point2D;
import java.util.function.Consumer;

public class Cladogram {

    /**
     * Layout algorithm that assigns equal depth (X-coordinate) to all leaves.
     * Internal nodes are positioned based on their children's coordinates.
     *
     * @param root the root of the tree
     * @return a map from each node to its (X, Y) coordinate
     */
    public static Map<ANode, Point2D> layoutEqualLeafDepth(ANode root) {
        Map<ANode, Point2D> layout = new HashMap<>();
        // Mutable counter for number of leaves
        int[] leafCounter = {0};

        // Compute max depth first
        int maxDepth = computeMaxDepth(root);

        // Recursive post-order traversal
        layoutEqualLeafDepthRecursive(root, layout, leafCounter, maxDepth);
        return layout;
    }

    // Recursive helper to assign coordinates
    private static void layoutEqualLeafDepthRecursive(ANode node, Map<ANode, Point2D> layout, int[] leafCounter, int maxDepth) {
        Collection<ANode> children = node.children();

        if (children.isEmpty()) {
            // Leaf node: x = maxDepth (all leaves aligned here), y = leaf order
            layout.put(node, new Point2D(maxDepth, leafCounter[0]));
            leafCounter[0]++;
        } else {
            for (ANode child : children) {
                layoutEqualLeafDepthRecursive(child, layout, leafCounter, maxDepth);
            }

            // x = min child's x - 1 (go leftwards)
            double minX = children.stream()
                    .mapToDouble(c -> layout.get(c).getX())
                    .min().orElse(maxDepth);
            double avgY = children.stream()
                    .mapToDouble(c -> layout.get(c).getY())
                    .average().orElse(0);

            // update current node's coordinates
            layout.put(node, new Point2D(minX - 1, avgY));
        }
    }

    /**
     * Layout algorithm that enforces a uniform edge length between parent and child nodes.
     * Horizontal position (X) increases with depth, and vertical position (Y) is same as equalLeafDepth.
     *
     * @param root the root of the tree
     * @return a map from each node to its (X, Y) coordinate
     */
    // Method to layout the cladogram with uniform edge lengths
    public static Map<ANode, Point2D> layoutUniformEdgeLength(ANode root) {
        Map<ANode, Point2D> layout = new HashMap<>();
        int[] leafCounter = {0};

        // Compute max depth first
        int maxDepth = computeMaxDepth(root);

        // Post-order traversal for y-coordinates
        layoutEqualLeafDepthRecursive(root, layout, leafCounter, maxDepth); // same as before

        // Pre-order traversal for x-coordinates, starting from root at x=0
        layoutUniformEdgeXRecursive(root, layout, 0);

        return layout;
    }

    // Recursive helper to assign x-coordinates
    private static void layoutUniformEdgeXRecursive(ANode node, Map<ANode, Point2D> layout, int x) {
        Point2D old = layout.get(node);
        layout.put(node, new Point2D(x, old.getY())); // Keep y, update x

        for (ANode child : node.children()) {
            layoutUniformEdgeXRecursive(child, layout, x + 1); // Child x = parent x + 1
        }
    }
    /**
     * Recursive helper function to set X-coordinates using pre-order traversal.
     *
     * @param node current node
     * @param x depth level (starts at 0 and increments for children)
     * @param coordinates map of node positions
     */
    private static void preOrder(ANode node, int x, Map<ANode, Point2D> coordinates) {
        Point2D old = coordinates.get(node);
        // Update point with correct X (depth), retain previous Y
        coordinates.put(node, new Point2D(x, old.getY()));
        for (ANode child : node.children()) {
            preOrder(child, x + 1, coordinates); // Recurse into children, increasing depth
        }
    }

    /**
     * Recursive post-order traversal (children first, then parent)
     * Applies a given function (visitor) to each node after its children are visited.
     *
     * @param node the current node
     * @param visitor a function to apply to each node
     */
    private static void postOrder(ANode node, Consumer<ANode> visitor) {
        for (ANode child : node.children()) {
            postOrder(child, visitor); // Recurse into children
        }
        visitor.accept(node); // Apply function after all children
    }

    // method to compute the maximum depth of the tree
    private static int computeMaxDepth(ANode node) {
        if (node.children().isEmpty()) return 0;
        return 1 + node.children().stream().mapToInt(Cladogram::computeMaxDepth).max().orElse(0);
    }
}
