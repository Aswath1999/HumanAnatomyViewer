package assignment5.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;

public class WindowView extends Region {

    public WindowView() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/assignment5/window/Window.fxml"));

        Region view = (Region) loader.load();  // cast from Parent to Region
        getChildren().add(view);

        view.prefWidthProperty().bind(widthProperty());
        view.prefHeightProperty().bind(heightProperty());

        setPrefSize(600, 400);

        new WindowPresenter(loader.getController());
    }

}
