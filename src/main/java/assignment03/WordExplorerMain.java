package assignment03;

import assignment03.model.Model;
import assignment03.window.WindowPresenter;
import assignment03.window.WindowView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WordExplorerMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var view = new WindowView(); // loads Window.fxml
        var model = new Model();     // loads ANode tree
        var controller = view.getController();

        WindowPresenter presenter = new WindowPresenter(stage, controller, model);
        controller.initializePresenter(presenter); // ✅ Required to connect controller

        stage.setScene(new Scene(view.getRoot(), 800, 600));
        stage.setTitle("Part-of Anatomy Tree Explorer");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
