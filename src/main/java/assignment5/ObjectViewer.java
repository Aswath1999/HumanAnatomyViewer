package assignment5;
import assignment5.window.WindowView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ObjectViewer extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        var root = new WindowView();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
