package assignment03.window;

import assignment03.model.ANode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Utility class for converting a tree of ANode objects
 * into a JavaFX TreeView structure using TreeItem nodes.
 */
public class TreeViewSetup {

    /**
     * Sets up a TreeView to display a hierarchy of ANode objects.
     *
     * @param treeView The JavaFX TreeView to populate.
     * @param rootNode The root of the ANode tree (from your model).
     */
    public static void setup(TreeView<ANode> treeView, ANode rootNode) {
        // Convert ANode tree to TreeItem tree (used by TreeView)
        TreeItem<ANode> rootItem = buildTree(rootNode);

        // Expand root node so its children are visible by default
        rootItem.setExpanded(true);

        // Attach the built TreeItem hierarchy to the TreeView UI
        treeView.setRoot(rootItem);
    }

    /**
     * Recursively builds a TreeItem hierarchy from an ANode tree.
     *
     * @param node The ANode model node to convert.
     * @return A TreeItem that wraps the node and includes its children.
     */
    private static TreeItem<ANode> buildTree(ANode node) {
        // Create a TreeItem that wraps the current ANode
        TreeItem<ANode> item = new TreeItem<>(node);

        // Recursively convert and attach all children
        for (ANode child : node.children()) {
            TreeItem<ANode> childItem = buildTree(child); // Recursive step
            item.getChildren().add(childItem);
        }

        // Return the complete TreeItem (with children attached)
        return item;
    }
}
