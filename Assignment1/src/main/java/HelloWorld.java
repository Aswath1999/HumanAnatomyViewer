import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class HelloWorld extends Application {
    public void start(Stage primaryStage) {
        var root = new FlowPane();
        root.getChildren().add(new Label("Hello World"));
        var scene = new Scene(root, 300, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
