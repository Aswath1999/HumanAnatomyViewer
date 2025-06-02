package assignment4.window;

import assignment4.model.ANode;
import javafx.geometry.Point2D;

import java.util.Map;

public class TreeViewSetup {
    // Controller to manage the window
    private final WindowController controller;

    // Constructor to initialize the controller
    public TreeViewSetup(WindowController controller) {
        this.controller = controller;
    }

    // Method to render the tree view
    public void render(Map<ANode, Point2D> layout) {
        var edgeGroup = controller.getEdgeGroup();
        var nodeGroup = controller.getNodeGroup();
        var labelGroup = controller.getLabelGroup();

        edgeGroup.getChildren().clear();
        nodeGroup.getChildren().clear();
        labelGroup.getChildren().clear();

        // Count nodes, edges, and leaves
        int nodeCount = layout.size();
        int edgeCount = countEdges(layout);
        int leafCount = countLeaves(layout);
        // Update the HBox with the counts
        controller.getLabelHBox().setText("Nodes: " + nodeCount + ", Edges: " + edgeCount + ", Leaves: " + leafCount);
    }

    // Method to count edges based on the layout
    private int countEdges(Map<ANode, Point2D> layout) {
        int edgeCount = 0;
        for (ANode node : layout.keySet()) {
            edgeCount += node.children().size(); // Each child represents an edge
        }
        return edgeCount;
    }
    // Method to count leaves based on the layout
    private int countLeaves(Map<ANode, Point2D> layout) {
        int leafCount = 0;
        for (ANode node : layout.keySet()) {
            if (node.children().isEmpty()) {
                leafCount++; // Count if the node has no children
            }
        }
        return leafCount;
    }
}


