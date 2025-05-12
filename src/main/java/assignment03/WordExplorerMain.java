package assignment03;

import assignment03.model.Model;
import assignment03.window.WindowPresenter;
import assignment03.window.WindowView;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;



public class WordExplorerMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var view = new WindowView(); // loads Window.fxml
        var model = new Model();     // loads ANode tree
        var presenter = new WindowPresenter(stage, view.getController(), model);

        stage.setScene(new Scene(view.getRoot(), 800, 600));
        stage.setTitle("Part-of Anatomy Tree Explorer");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
