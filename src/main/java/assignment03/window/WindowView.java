
package assignment03.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;


public class WindowView {
    private final WindowController controller;
    private final Parent root;

    public WindowView() throws IOException {
        URL url = getClass().getResource("/assignment03/window/Window.fxml");
        System.out.println("URL = " + url);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/assignment03/window/window.fxml"));

        root = loader.load();
        controller = loader.getController();
    }

    public WindowController getController() {
        return controller;
    }

    public Parent getRoot() {
        return root;
    }
}
