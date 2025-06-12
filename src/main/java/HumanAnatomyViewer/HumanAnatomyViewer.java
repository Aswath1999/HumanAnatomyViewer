package HumanAnatomyViewer;


import HumanAnatomyViewer.model.Model;
import HumanAnatomyViewer.window.WindowPresenter;
import HumanAnatomyViewer.window.WindowView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HumanAnatomyViewer  extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        var view = new WindowView(); // loads Window.fxml
        var model = new Model();     // loads ANode tree
        var controller = view.getController();

        Scene scene = new Scene(view.getRoot(), 800, 600);


        // Bind size
        view.bindToScene(scene);
        WindowPresenter presenter = new WindowPresenter(stage, controller, model);/**/
        controller.initializePresenter(presenter); // ✅ Required to connect controller*/

        stage.setScene(scene);
        stage.setTitle("Human Anatomy Viewer");
        stage.show();
    }




    public static void main(String[] args) {
        launch(args);
    }

}
