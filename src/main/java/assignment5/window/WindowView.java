package assignment5.window;

import assignment5.window.WindowController;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class WindowView extends Parent {

    public WindowView() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/assignment5/window/Window.fxml"));
        Parent view = loader.load();
        getChildren().add(view);

        new WindowPresenter(loader.getController());
    }

}
