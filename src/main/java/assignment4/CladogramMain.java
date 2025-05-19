package assignment4;

import assignment4.model.Model;
import assignment4.window.WindowPresenter;
import assignment4.window.WindowView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CladogramMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the view (FXML + controller)
        var view = new WindowView();
        var controller = view.getController();

        // Load the model (tree data)
        var model = new Model(); // loads partof_parts_list_e.txt etc.



        // Connect the controller to the model's root
        controller.setRoot(model.getPartOfRoot());

        // Set up presenter with access to the stage and model
        var presenter = new WindowPresenter(stage);
        controller.setPresenter(presenter); // Connect presenter

        // Set up the main scene
        stage.setScene(new Scene(view.getRoot(), 800, 600));
        stage.setTitle("Part-of Anatomy Tree Explorer");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
