package HumanAnatomyViewer.window;

import HumanAnatomyViewer.model.ANode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays an interactive pie chart showing volume distribution
 * of selected anatomical parts.
 */
public class VolumeChartHelper {

    /**
     * Shows a pie chart of volumes for selected models.
     *
     * @param ownerStage      The parent window that owns this pie chart window.
     * @param selectedFileIds Set of selected model file IDs.
     * @param modelMap        Map of fileId → Group (loaded 3D model).
     * @param fileIdToName    Map of fileId → display name from ANode.
     */
    public static void showSelectedPartsVolumeChart(Stage ownerStage,
                                                    Iterable<String> selectedFileIds,
                                                    Map<String, Group> modelMap,
                                                    Map<String, String> fileIdToName) {

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (String fileId : selectedFileIds) {
            Group model = modelMap.get(fileId);
            if (model == null) continue;

            double volume = calculateVolume(model);
            if (volume <= 0) continue;

            String label = fileIdToName.getOrDefault(fileId, fileId);
            PieChart.Data slice = new PieChart.Data(label, volume);
            pieData.add(slice);
        }

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Volume Distribution of Selected Parts");
        pieChart.setLabelsVisible(true);
        pieChart.setClockwise(true);
        pieChart.setLegendVisible(false);

        // === Optional Interactivity ===
        // Hover Tooltip for Volume
        for (PieChart.Data slice : pieChart.getData()) {
            Tooltip tooltip = new Tooltip(String.format("%s: %.2f mm³", slice.getName(), slice.getPieValue()));
            Tooltip.install(slice.getNode(), tooltip);
        }

        // Scroll Zoom (mouse scroll up/down to zoom)
        pieChart.setOnScroll(event -> {
            double zoomFactor = (event.getDeltaY() > 0) ? 1.05 : 0.95;
            pieChart.setScaleX(pieChart.getScaleX() * zoomFactor);
            pieChart.setScaleY(pieChart.getScaleY() * zoomFactor);
        });

        // Label to show selected part (even if it's small)
        Label selectedLabel = new Label("Click a slice to show its name");
        selectedLabel.setStyle("-fx-font-size: 16px;");

        // Original labels for restoring
        Map<PieChart.Data, String> originalLabels = new HashMap<>();
        for (PieChart.Data slice : pieData) {
            originalLabels.put(slice, slice.getName());
        }

        // Highlight on Click & update label
        for (PieChart.Data slice : pieChart.getData()) {
            slice.getNode().setOnMouseClicked(e -> {
                for (PieChart.Data other : pieChart.getData()) {
                    other.getNode().setStyle("");
                    other.setName("");
                }
                slice.getNode().setStyle("-fx-effect: dropshadow(gaussian, #333, 10, 0.6, 0, 0);");
                slice.setName(originalLabels.get(slice));
                selectedLabel.setText("Selected: " + originalLabels.get(slice));
                e.consume();
            });
        }

        // === Layout and Stage Setup ===
        VBox vbox = new VBox(10, pieChart, selectedLabel);
        vbox.setPadding(new Insets(20));
        VBox.setVgrow(pieChart, Priority.ALWAYS);
        pieChart.setMinSize(600, 600);

        Scene scene = new Scene(vbox, 800, 700);

        // Global click to restore all labels
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
            for (PieChart.Data slice : pieChart.getData()) {
                slice.setName(originalLabels.get(slice));
                slice.getNode().setStyle("");
            }
            selectedLabel.setText("Click a slice to show its name");
        });

        Stage chartStage = new Stage();
        chartStage.setTitle("Volume Pie Chart");
        chartStage.setScene(scene);
        chartStage.setResizable(true);
        chartStage.initOwner(ownerStage);
        chartStage.show();
    }

    /**
     * Calculates the total volume of a 3D model Group.
     */
    private static double calculateVolume(Group group) {
        double volume = 0.0;
        for (Node node : group.getChildren()) {
            if (node instanceof MeshView meshView && meshView.getMesh() instanceof TriangleMesh mesh) {
                volume += computeMeshVolume(mesh);
            }
        }
        return volume;
    }

    /**
     * Computes the volume of a TriangleMesh using signed tetrahedrons.
     */
    private static double computeMeshVolume(TriangleMesh mesh) {
        ObservableFloatArray pointArray = mesh.getPoints();
        float[] points = new float[pointArray.size()];
        pointArray.toArray(points);

        ObservableIntegerArray faceArray = mesh.getFaces();
        int[] faces = new int[faceArray.size()];
        faceArray.toArray(faces);

        double volume = 0.0;
        for (int i = 0; i < faces.length; i += 6) {
            int p0 = faces[i] * 3;
            int p1 = faces[i + 2] * 3;
            int p2 = faces[i + 4] * 3;

            Point3D a = new Point3D(points[p0], points[p0 + 1], points[p0 + 2]);
            Point3D b = new Point3D(points[p1], points[p1 + 1], points[p1 + 2]);
            Point3D c = new Point3D(points[p2], points[p2 + 1], points[p2 + 2]);

            volume += a.dotProduct(b.crossProduct(c)) / 6.0;
        }

        return Math.abs(volume);
    }
}