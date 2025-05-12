package assignment03.window;


import assignment03.model.ANode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class TreeViewSetup {

    public static void setup(TreeView<ANode> treeView, ANode rootNode) {
        TreeItem<ANode> rootItem = buildTree(rootNode);
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
    }

    private static TreeItem<ANode> buildTree(ANode node) {
        TreeItem<ANode> item = new TreeItem<>(node);
        for (ANode child : node.children()) {
            item.getChildren().add(buildTree(child));
        }
        return item;
    }
}
