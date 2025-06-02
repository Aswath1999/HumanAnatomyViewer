package assignment4;

import assignment4.model.*;
import assignment4.window.*;

import java.io.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class TreeDrawer extends Application {

    private static ANode rootNode;

    @Override
    public void start(Stage stage) throws Exception {
        // Load the tree from files
        InputStream partsStream = getClass().getResourceAsStream("/partof_parts_list_e.txt");
        InputStream elementsStream = getClass().getResourceAsStream("/partof_element_parts.txt");
        InputStream relationsStream = getClass().getResourceAsStream("/partof_inclusion_relation_list.txt");

        Model model = new Model(partsStream, elementsStream, relationsStream);
        ANode root = model.getRoot();

        // Setup view and presenter
        var view = new WindowView();
        var presenter = new WindowPresenter(stage, view.getController(), root);

        stage.setScene(new Scene(view.getRoot(), 800, 600));
        stage.setTitle("Tree Drawer");
        stage.show();
    }

    // main
    public static void main(String[] args) throws IOException {

        launch(args); // Launch JavaFX app
    }


}
