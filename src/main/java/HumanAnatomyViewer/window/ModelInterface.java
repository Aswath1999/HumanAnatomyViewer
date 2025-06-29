package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import HumanAnatomyViewer.model.ObjIO;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
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
import java.util.stream.Collectors;

/**
 * ModelInterface handles loading, displaying, interacting with, and styling
 * 3D anatomical models based on selections in a TreeView.
 */
public class ModelInterface {

    // === FIELDS ===

    // Parent container for all 3D models in the JavaFX scene
    private final Group innerGroup;

    // The TreeView that displays the anatomical hierarchy
    private final TreeView<ANode> treeView;

    // Cache: maps file IDs (e.g. bone_001) to their corresponding loaded 3D Group
    private final Map<String, Group> loadedModels = new HashMap<>();

    // Tracks currently selected model IDs to apply highlighting or interaction
    private final Set<String> selectedFileIds = new HashSet<>();

    // Prevents feedback loops during programmatic selection updates
    private boolean inSelectionUpdate = false;

    /**
     * Constructor for ModelInterface.
     * @param innerGroup The 3D group that will hold model meshes.
     * @param treeView The tree representing the anatomical structure.
     */
    public ModelInterface(Group innerGroup, TreeView<ANode> treeView) {
        this.innerGroup = innerGroup;
        this.treeView = treeView;
    }

    // === CORE METHODS ===

    /**
     * Loads and displays all 3D models corresponding to the selected TreeView nodes.
     * Clears previous models and replaces them with the newly selected ones.
     * @param selectedItems Selected nodes in the TreeView
     */
    public void loadAndDisplayModels(List<TreeItem<ANode>> selectedItems) {
        innerGroup.getChildren().clear();   // Remove all currently displayed models
        innerGroup.getTransforms().clear(); // Reset any applied transforms (e.g. rotations)

        for (TreeItem<ANode> item : selectedItems) {
            ANode node = item.getValue();
            if (node == null) continue;

            // For each file ID (model) associated with this node
            for (String fileId : node.fileIds()) {
                // Try to load model if not already loaded
                Group modelGroup = loadModelIfAbsent(fileId);

                // If valid and not already displayed, add it to the group
                if (modelGroup != null && !innerGroup.getChildren().contains(modelGroup)) {
                    applyClickHandler(modelGroup, fileId); // Add click interactivity
                    innerGroup.getChildren().add(modelGroup);
                }
            }
        }
        selectedFileIds.clear();
        selectedFileIds.addAll(selectedItems.stream()
                .filter(Objects::nonNull)
                .map(TreeItem::getValue)
                .filter(Objects::nonNull)
                .flatMap(anode -> anode.fileIds().stream())
                .toList());
        // Visually indicate selection with draw mode changes
        applyDrawModeBasedOnSelection();
    }

    public void loadAndDisplayModelsByFileIds(Collection<String> fileIds) {


        // Step 1: Clear previous display
        innerGroup.getChildren().clear();
        innerGroup.getTransforms().clear();


        // Step 2: Update selected file IDs
        selectedFileIds.clear();
        selectedFileIds.addAll(fileIds);


        // Step 3: Load and display each model
        for (String fileId : fileIds) {


            Group modelGroup = loadModelIfAbsent(fileId);
            if (modelGroup != null) {
                // Remove just in case it was already in (paranoia check)
                if (innerGroup.getChildren().contains(modelGroup)) {

                    innerGroup.getChildren().remove(modelGroup);
                }

                applyClickHandler(modelGroup, fileId);
                innerGroup.getChildren().add(modelGroup);
                modelGroup.setUserData(fileId); // ✅ Attach file ID to top-level group

            } else {
                System.out.println("❌ Could not load model: " + fileId);
            }
        }

        // Step 4: Apply visual highlighting
        applyDrawModeBasedOnSelection();

    }



    public Set<String> getCurrentlyVisibleFileIds() {
        Set<String> ids = innerGroup.getChildren().stream()
                .map(Node::getUserData)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toSet());
        System.out.println("Visible File IDs: " + ids);
        return ids;
    }



    /**
     * Attempts to load a 3D model from file if not already loaded.
     * @param fileId The ID of the file/model (e.g., "lung_left")
     * @return Loaded Group containing the model, or null if not found or failed
     */
    private Group loadModelIfAbsent(String fileId) {
        return loadedModels.computeIfAbsent(fileId, id -> {
            try {
                // Construct path to .obj file in resources
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

    // === INTERACTION METHODS ===

    /**
     * Applies a mouse click handler to all Shape3D nodes inside the model group.
     * Allows user to click models in the 3D scene to select them.
     * @param modelGroup The loaded 3D model
     * @param fileId The associated file ID
     */
    private void applyClickHandler(Group modelGroup, String fileId) {
        for (var node : modelGroup.getChildren()) {
            if (node instanceof Shape3D shape) {
                shape.setPickOnBounds(true);           // Enable click detection
                shape.setUserData(fileId);             // Store ID in metadata
                shape.setOnMouseClicked(event -> {
                    handleModelClick(fileId, event);   // Handle selection
                    event.consume();                   // Prevent propagation
                });
            } else if (node instanceof Group subGroup) {
                applyClickHandler(subGroup, fileId);   // Recursively apply to children
            }
        }
    }

    /**
     * Handles model selection when user clicks on a shape in the 3D view.
     * @param fileId The file ID of the clicked model
     * @param event The mouse event
     */
    private void handleModelClick(String fileId, MouseEvent event) {
        // If not holding shift/ctrl, start new selection
        if (!event.isShiftDown() && !event.isControlDown()) {
            selectedFileIds.clear();
        }

        // Toggle selection state
        if (selectedFileIds.contains(fileId)) {
            selectedFileIds.remove(fileId);
        } else {
            selectedFileIds.add(fileId);
        }

        // Sync tree selection and draw mode after interaction
        syncTreeSelectionFromFileIds();
        applyDrawModeBasedOnSelection();
    }

    /**
     * Updates the TreeView selection to match currently selected file IDs.
     */
    public void syncTreeSelectionFromFileIds() {
        MultipleSelectionModel<TreeItem<ANode>> model = treeView.getSelectionModel();
        inSelectionUpdate = true;
        model.clearSelection();
        selectMatchingItems(treeView.getRoot(), model);
        inSelectionUpdate = false;
    }

    /**
     * Recursively selects all tree items whose ANode contains a selected file ID.
     * @param item Tree node to test
     * @param model Selection model of the TreeView
     */
    private void selectMatchingItems(TreeItem<ANode> item, MultipleSelectionModel<TreeItem<ANode>> model) {
        if (item.getValue() != null &&
                item.getValue().fileIds().stream().anyMatch(selectedFileIds::contains)) {

            expandPathTo(item); // Expand parents so item is visible
            model.select(item); // Programmatically select it

            // Scroll to selected item in the TreeView (UI-safe)
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
     * Expands all parents of a given TreeItem to make it visible.
     * @param item The target tree item
     */
    private void expandPathTo(TreeItem<ANode> item) {
        TreeItem<ANode> parent = item.getParent();
        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }
    }

    // === DRAW MODE & COLOR METHODS ===

    /**
     * Applies draw modes to models: selected = FILL, unselected = LINE.
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
     * Recursively sets the draw mode for all Shape3D nodes inside a Group.
     * @param group Parent group
     * @param mode The draw mode to apply
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
     * Applies a color to all selected and filled 3D models.
     * @param color The JavaFX Color to apply
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
     * Recursively applies a color to all Shape3D nodes with FILL mode inside a group.
     * @param group The model group
     * @param color The color to apply
     */
    private void applyColorToFilledShapes(Group group, Color color) {
        for (var node : group.getChildren()) {
            if (node instanceof Shape3D shape && shape.getDrawMode() == DrawMode.FILL) {
                if (shape.getMaterial() instanceof PhongMaterial phong) {
                    phong.setDiffuseColor(color); // Modify existing material
                } else {
                    shape.setMaterial(new PhongMaterial(color)); // Assign new one
                }
            } else if (node instanceof Group subGroup) {
                applyColorToFilledShapes(subGroup, color);
            }
        }
    }


    public Color getFirstSelectedColor() {
        for (String fileId : selectedFileIds) {
            Group group = loadedModels.get(fileId);
            if (group != null) {
                for (var node : group.getChildren()) {
                    if (node instanceof Shape3D shape && shape.getDrawMode() == DrawMode.FILL) {
                        if (shape.getMaterial() instanceof PhongMaterial phong) {
                            return phong.getDiffuseColor();
                        }
                    }
                }
            }
        }
        return Color.GRAY; // default fallback if none found
    }

    public Map<String, Color> getCurrentColorsForSelected() {
        Map<String, Color> colorMap = new HashMap<>();

        for (String fileId : selectedFileIds) {
            Group group = loadedModels.get(fileId);
            if (group != null) {
                for (var node : group.getChildren()) {
                    if (node instanceof Shape3D shape && shape.getDrawMode() == DrawMode.FILL) {
                        if (shape.getMaterial() instanceof PhongMaterial phong) {
                            colorMap.put(fileId, phong.getDiffuseColor());
                            break; // Only need one representative color
                        }
                    }
                }
            }
        }
        return colorMap;
    }

    public void applyColorsFromMap(Map<String, Color> colorMap) {
        for (Map.Entry<String, Color> entry : colorMap.entrySet()) {
            String fileId = entry.getKey();
            Color color = entry.getValue();

            Group group = loadedModels.get(fileId);
            if (group != null) {
                applyColorToFilledShapes(group, color);
            }
        }
    }



    // === GETTERS ===

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


    public void hideModelsByFileIds(Collection<String> fileIds) {
        for (String fileId : fileIds) {
            innerGroup.getChildren().remove(loadedModels.get(fileId));
        }
    }

}
