package HumanAnatomyViewer.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

import java.io.IOException;

/**
 * WindowView is responsible for loading the JavaFX window defined in the FXML file.
 * It links the FXML layout with its controller and provides access to both.
 * This version ensures that the UI resizes with the scene.
 */
public class WindowView {

    private final WindowController controller;
    private final Region root; // Changed from Parent to Region for size binding

    public WindowView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/HumanAnatomy/Window/window.fxml"));
        root = loader.load(); // Assumes root of FXML is a Region (e.g., AnchorPane, BorderPane)
        controller = loader.getController();
    }

    public WindowController getController() {
        return controller;
    }

    public Region getRoot() {
        return root;
    }

    /**
     * Binds the root's preferred size to the scene dimensions.
     * Call this after setting the scene to ensure resizing works.
     */
    public void bindToScene(javafx.scene.Scene scene) {
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
    }
}
