package assignment4.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

/**
 * WindowView is responsible for loading the JavaFX window defined in the FXML file.
 * It links the FXML layout with its controller and provides access to both.
 */
public class WindowView {

    // Reference to the controller associated with the FXML file
    private final WindowController controller;

    // The root node of the scene graph (loaded from the FXML)
    private final Parent root;

    /**
     * Constructor loads the FXML layout and initializes the controller and root node.
     *
     * @throws IOException if the FXML file cannot be found or loaded
     */
    public WindowView() throws IOException {
        URL url = getClass().getResource("/assignment4/window/window.fxml");
        System.out.println("FXML URL = " + url);  // Should NOT be null

        if (url == null) {
            throw new IllegalStateException("Could not find FXML file at /assignment4/window/window.fxml");
        }


        // Create an FXMLLoader for the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/assignment4/window/window.fxml"));

        // Load the FXML file and store the root UI element
        root = loader.load();

        // Get the controller that was specified in the FXML file
        controller = loader.getController();
    }

    /**
     * Returns the controller instance associated with the FXML file.
     *
     * @return the WindowController used to interact with the view
     */
    public WindowController getController() {
        return controller;
    }

    /**
     * Returns the root node of the UI hierarchy (used when setting the scene).
     *
     * @return the top-level Parent node from the FXML
     */
    public Parent getRoot() {
        return root;
    }
}
