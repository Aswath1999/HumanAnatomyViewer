package assignment02.anatomy;

import java.nio.file.Files;
import java.nio.file.Path;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.net.URL;




public class AnatomyDataExplorer extends Application {

    @Override
    public void start(Stage stage) {
        TreeView<ANode> treeView = new TreeView<>();
        ListView<String> listView = new ListView<>();

        // Buttons
        Button expandBtn = new Button("Expand");
        Button collapseBtn = new Button("Collapse");
        Button byeBtn = new Button("Bye");

        ToolBar toolBar = new ToolBar(expandBtn, collapseBtn, byeBtn);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(treeView, listView);
        splitPane.setDividerPositions(0.4);

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(splitPane);

        try {
            System.out.println("File URL: " + getClass().getResource("/partof_parts_list_e.txt"));

            String partsPath = getClass().getResource("/partof_parts_list_e.txt").getPath();
            String elementsPath = getClass().getResource("/partof_element_parts.txt").getPath();
            String relationsPath = getClass().getResource("/partof_inclusion_relation_list.txt").getPath();

            ANode rootNode = TreeLoader.load(partsPath, elementsPath, relationsPath);



            TreeItem<ANode> rootItem = createTreeItemsRec(rootNode);
            treeView.setRoot(rootItem);
            treeView.setShowRoot(true);

            //  Expand All button
            expandBtn.setOnAction(e -> expandAll(rootItem));

            //  Collapse All button
            collapseBtn.setOnAction(e -> {
                collapseAll(rootItem);
                rootItem.setExpanded(true); // Keep root expanded (or not, up to you)
            });

            //  Bye button
            byeBtn.setOnAction(e -> {
                stage.close(); // or Platform.exit();
            });

            // Monitor tree expansion state to toggle Collapse button
            treeView.expandedItemCountProperty().addListener((obs, oldVal, newVal) -> {
                collapseBtn.setDisable(!hasExpandedDescendants(treeView.getRoot()));
            });
            collapseBtn.setDisable(!hasExpandedDescendants(treeView.getRoot()));




        } catch (IOException  e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading anatomy tree: " + e.getMessage()).showAndWait();
        }

        // 🎯 Tree selection listener
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            listView.getItems().clear();
            if (newItem != null && newItem.getValue() != null) {
                listView.getItems().addAll(newItem.getValue().fileIds());
            }
        });

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Anatomy Data Explorer");
        stage.show();
    }


    // Recursively create TreeItems from ANode
    static TreeItem<ANode> createTreeItemsRec(ANode node) {
        TreeItem<ANode> item = new TreeItem<>(node);
        for (ANode child : node.children()) {
            item.getChildren().add(createTreeItemsRec(child));
        }
        return item;
    }
    // Recursively expand all nodes
    private void expandAll(TreeItem<?> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<?> child : item.getChildren()) {
                expandAll(child);
            }
        }
    }

    // Recursively collapse all nodes
    private void collapseAll(TreeItem<?> item) {
        for (TreeItem<?> child : item.getChildren()) {
            collapseAll(child);
        }
        item.setExpanded(false);
    }

    private boolean hasExpandedDescendants(TreeItem<?> root) {
        for (TreeItem<?> child : root.getChildren()) {
            if (child.isExpanded() || hasExpandedDescendants(child)) {
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        launch();
    }
}
