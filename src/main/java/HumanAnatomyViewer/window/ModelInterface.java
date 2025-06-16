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

public class ModelInterface {

    private final Group innerGroup;
    private final TreeView<ANode> treeView;
    private final Map<String, Group> loadedModels = new HashMap<>();
    private final Set<String> selectedFileIds = new HashSet<>();
    private boolean inSelectionUpdate = false;

    public ModelInterface(Group innerGroup, TreeView<ANode> treeView) {
        this.innerGroup = innerGroup;
        this.treeView = treeView;
    }

    public void loadAndDisplayModels(List<TreeItem<ANode>> selectedItems) {
        innerGroup.getChildren().clear();
        innerGroup.getTransforms().clear();

        for (TreeItem<ANode> item : selectedItems) {
            ANode node = item.getValue();
            if (node == null) continue;

            for (String fileId : node.fileIds()) {
                Group modelGroup = loadModelIfAbsent(fileId);
                if (modelGroup != null && !innerGroup.getChildren().contains(modelGroup)) {
                    applyClickHandler(modelGroup, fileId);
                    innerGroup.getChildren().add(modelGroup);
                }
            }
        }

        applyDrawModeBasedOnSelection();
    }

    public void hideModels(List<TreeItem<ANode>> selectedItems) {
        for (TreeItem<ANode> item : selectedItems) {
            ANode node = item.getValue();
            if (node == null) continue;
            for (String fileId : node.fileIds()) {
                innerGroup.getChildren().remove(loadedModels.get(fileId));
            }
        }
    }

    private Group loadModelIfAbsent(String fileId) {
        return loadedModels.computeIfAbsent(fileId, id -> {
            try {
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

    private void applyClickHandler(Group modelGroup, String fileId) {
        for (var node : modelGroup.getChildren()) {
            if (node instanceof Shape3D shape) {
                shape.setPickOnBounds(true);
                shape.setOnMouseClicked(event -> {
                    handleModelClick(fileId, event);
                    event.consume();
                });
            } else if (node instanceof Group subGroup) {
                applyClickHandler(subGroup, fileId);
            }
        }
    }

    private void handleModelClick(String fileId, MouseEvent event) {
        if (!event.isShiftDown() && !event.isControlDown()) {
            selectedFileIds.clear();
        }

        if (selectedFileIds.contains(fileId)) {
            selectedFileIds.remove(fileId);
        } else {
            selectedFileIds.add(fileId);
        }

        syncTreeSelectionFromFileIds();
        applyDrawModeBasedOnSelection();
    }

    private void syncTreeSelectionFromFileIds() {
        MultipleSelectionModel<TreeItem<ANode>> model = treeView.getSelectionModel();
        inSelectionUpdate = true;
        model.clearSelection();
        selectMatchingItems(treeView.getRoot(), model);
        inSelectionUpdate = false;
    }

    private void selectMatchingItems(TreeItem<ANode> item, MultipleSelectionModel<TreeItem<ANode>> model) {
        if (item.getValue() != null &&
                item.getValue().fileIds().stream().anyMatch(selectedFileIds::contains)) {
            expandPathTo(item);
            model.select(item);
            Platform.runLater(() -> {
                int row = treeView.getRow(item);
                if (row >= 0) treeView.scrollTo(row);
            });
        }

        for (TreeItem<ANode> child : item.getChildren()) {
            selectMatchingItems(child, model);
        }
    }

    private void expandPathTo(TreeItem<ANode> item) {
        TreeItem<ANode> parent = item.getParent();
        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }
    }

    public void applyDrawModeBasedOnSelection() {
        for (Map.Entry<String, Group> entry : loadedModels.entrySet()) {
            String fileId = entry.getKey();
            Group group = entry.getValue();
            boolean isSelected = selectedFileIds.contains(fileId);
            setDrawModeRecursive(group, isSelected ? DrawMode.FILL : DrawMode.LINE);
        }
    }

    private void setDrawModeRecursive(Group group, DrawMode mode) {
        for (var node : group.getChildren()) {
            if (node instanceof Shape3D shape) {
                shape.setDrawMode(mode);
            } else if (node instanceof Group subGroup) {
                setDrawModeRecursive(subGroup, mode);
            }
        }
    }

    public void applyColorToSelected(Color color) {
        for (String fileId : selectedFileIds) {
            Group group = loadedModels.get(fileId);
            if (group != null) {
                applyColorToFilledShapes(group, color);
            }
        }
    }

    private void applyColorToFilledShapes(Group group, Color color) {
        for (var node : group.getChildren()) {
            if (node instanceof Shape3D shape && shape.getDrawMode() == DrawMode.FILL) {
                if (shape.getMaterial() instanceof PhongMaterial phong) {
                    phong.setDiffuseColor(color);
                } else {
                    shape.setMaterial(new PhongMaterial(color));
                }
            } else if (node instanceof Group subGroup) {
                applyColorToFilledShapes(subGroup, color);
            }
        }
    }

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
