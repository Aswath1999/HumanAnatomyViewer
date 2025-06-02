package assignment4.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.io.InputStream;

public class WindowView {

    // FXML elements
    private final WindowController controller;
    private final Parent root;


    // Loads the FXML file and initializes the controller
    public WindowView() throws IOException {
        try (InputStream ins = getClass().getResource("/assignment4/window/Window.fxml").openStream()) {
            FXMLLoader loader = new FXMLLoader();
            root = loader.load(ins);
            controller = loader.getController();
        }
    }

    // methods to get the root
    public Parent getRoot() {
        return root;
    }

    // method to get the controller
    public WindowController getController() {
        return controller;
    }
}

