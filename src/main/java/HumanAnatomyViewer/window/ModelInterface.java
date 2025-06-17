package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import HumanAnatomyViewer.model.ObjIO;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * ModelInterface handles loading, displaying, interacting with, and styling
 * 3D anatomical models based on selections in a TreeView.
 */
public class ModelInterface {

    // Parent node that contains all 3D models (used for rendering in the scene)
    private final Group innerGroup;

    // TreeView representing the anatomical hierarchy
    private final TreeView<ANode> treeView;

    // Maps file IDs to their loaded 3D Group (to avoid reloading)
    private final Map<String, Group> loadedModels = new HashMap<>();

    // Tracks currently selected file IDs (used for highlighting and coloring)
    private final Set<String> selectedFileIds = new HashSet<>();

    // Flag to avoid recursive selection feedback loops
    private boolean inSelectionUpdate = false;

    // Constructor: initializes with the main rendering group and the tree view
    public ModelInterface(Group innerGroup, TreeView<ANode> treeView) {
        this.innerGroup = innerGroup;
        this.treeView = treeView;
    }

    /**
     * Loads and displays models associated with selected tree nodes.
     * Clears previous models and redraws newly selected ones.
     */
    public void loadAndDisplayModels(List<TreeItem<ANode>> selectedItems) {
        innerGroup.getChildren().clear();   // Clear old models
        innerGroup.getTransforms().clear(); // Clear any old transforms (like rotations)

        for (TreeItem<ANode> item : selectedItems) {
            ANode node = item.getValue();
            if (node == null) continue;

            // For each file associated with this node
            for (String fileId : node.fileIds()) {
                // Load model if not already loaded
                Group modelGroup = loadModelIfAbsent(fileId);

                if (modelGroup != null && !innerGroup.getChildren().contains(modelGroup)) {
                    applyClickHandler(modelGroup, fileId); // Allow mouse clicks to select
                    innerGroup.getChildren().add(modelGroup);
                }
            }
        }

        applyDrawModeBasedOnSelection(); // Update draw mode based on current selection
    }

    /**
     * Hides models associated with selected tree nodes by removing them from the scene.
     */
    public void hideModels(List<TreeItem<ANode>> selectedItems) {
        for (TreeItem<ANode> item : selectedItems) {
            ANode node = item.getValue();
            if (node == null) continue;

            for (String fileId : node.fileIds()) {
                innerGroup.getChildren().remove(loadedModels.get(fileId));
            }
        }
    }

    /**
     * Loads a model from file only if it hasn't been loaded before.
     */
    private Group loadModelIfAbsent(String fileId) {
        return loadedModels.computeIfAbsent(fileId, id -> {
            try {
                // Locate the .obj file in resources
                URL url = getClass().getResource("/HumanAnatomy/BodyParts/" + id + ".obj");
                if (url != null) {
                    return ObjIO.openObjFile(new File(url.toURI()));
                }
            } catch (Exception e) {
                System.err.println("Error loading model: " + id);
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Recursively attaches click listeners to all 3D shapes in the model.
     */
    private void applyClickHandler(Group modelGroup, String fileId) {
        for (var node : modelGroup.getChildren()) {
            if (node instanceof Shape3D shape) {
                shape.setPickOnBounds(true); // Enables click detection
                shape.setUserData(fileId); //  Store file ID in userData
                shape.setOnMouseClicked(event -> {
                    handleModelClick(fileId, event);
                    event.consume(); // Prevent event from bubbling up
                });
            } else if (node instanceof Group subGroup) {
                applyClickHandler(subGroup, fileId); // Recurse into nested groups
            }
        }
    }

    /**
     * Handles mouse click on a model to update selection state.
     */
    private void handleModelClick(String fileId, MouseEvent event) {
        // If no modifier keys, clear previous selection
        if (!event.isShiftDown() && !event.isControlDown()) {
            selectedFileIds.clear();
        }

        // Toggle selection state
        if (selectedFileIds.contains(fileId)) {
            selectedFileIds.remove(fileId);
        } else {
            selectedFileIds.add(fileId);
        }

        syncTreeSelectionFromFileIds(); // Highlight nodes in TreeView
        applyDrawModeBasedOnSelection(); // Change draw mode for selected models
    }

    /**
     * Updates the TreeView selection based on currently selected file IDs.
     */
    private void syncTreeSelectionFromFileIds() {
        MultipleSelectionModel<TreeItem<ANode>> model = treeView.getSelectionModel();
        inSelectionUpdate = true;
        model.clearSelection();
        selectMatchingItems(treeView.getRoot(), model);
        inSelectionUpdate = false;
    }

    /**
     * Recursively selects all tree items whose file IDs match selected models.
     */
    private void selectMatchingItems(TreeItem<ANode> item, MultipleSelectionModel<TreeItem<ANode>> model) {
        if (item.getValue() != null &&
                item.getValue().fileIds().stream().anyMatch(selectedFileIds::contains)) {

            expandPathTo(item); // Expand the parent path so it's visible
            model.select(item); // Select the item

            // Scroll to the selected item (delayed to ensure UI updates correctly)
            Platform.runLater(() -> {
                int row = treeView.getRow(item);
                if (row >= 0) treeView.scrollTo(row);
            });
        }

        for (TreeItem<ANode> child : item.getChildren()) {
            selectMatchingItems(child, model);
        }
    }

    /**
     * Expands all parent nodes of the given item to ensure it's visible in the TreeView.
     */
    private void expandPathTo(TreeItem<ANode> item) {
        TreeItem<ANode> parent = item.getParent();
        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }
    }

    /**
     * Changes draw mode (FILL vs LINE) depending on whether each model is selected.
     */
    public void applyDrawModeBasedOnSelection() {
        for (Map.Entry<String, Group> entry : loadedModels.entrySet()) {
            String fileId = entry.getKey();
            Group group = entry.getValue();
            boolean isSelected = selectedFileIds.contains(fileId);

            setDrawModeRecursive(group, isSelected ? DrawMode.FILL : DrawMode.LINE);
        }
    }

    /**
     * Recursively sets the draw mode on all 3D shapes within a group.
     */
    private void setDrawModeRecursive(Group group, DrawMode mode) {
        for (var node : group.getChildren()) {
            if (node instanceof Shape3D shape) {
                shape.setDrawMode(mode);
            } else if (node instanceof Group subGroup) {
                setDrawModeRecursive(subGroup, mode);
            }
        }
    }

    /**
     * Applies a given color to all selected and filled 3D models.
     */
    public void applyColorToSelected(Color color) {
        for (String fileId : selectedFileIds) {
            Group group = loadedModels.get(fileId);
            if (group != null) {
                applyColorToFilledShapes(group, color);
            }
        }
    }

    /**
     * Recursively applies color to all shapes with FILL draw mode inside a group.
     */
    private void applyColorToFilledShapes(Group group, Color color) {
        for (var node : group.getChildren()) {
            if (node instanceof Shape3D shape && shape.getDrawMode() == DrawMode.FILL) {
                if (shape.getMaterial() instanceof PhongMaterial phong) {
                    phong.setDiffuseColor(color); // Change existing material color
                } else {
                    shape.setMaterial(new PhongMaterial(color)); // Assign new material
                }
            } else if (node instanceof Group subGroup) {
                applyColorToFilledShapes(subGroup, color); // Recurse
            }
        }
    }

    // === Getters ===

    public Set<String> getSelectedFileIds() {
        return selectedFileIds;
    }

    public boolean isInSelectionUpdate() {
        return inSelectionUpdate;
    }

    public Map<String, Group> getLoadedModels() {
        return loadedModels;
    }

    public Group getInnerGroup() {
        return innerGroup;
    }
}
