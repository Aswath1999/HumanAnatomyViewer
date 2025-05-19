package assignment4.window;

import assignment4.model.ANode;
import assignment4.model.NewickExporter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WindowPresenter {

    private final Stage stage;

    public WindowPresenter(Stage stage) {
        this.stage = stage;
    }

    public void saveAsNewick(ANode root) {
        if (root == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Tree as Newick");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Newick files", "*.nwk"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                String newick = NewickExporter.toNewick(root); // Implement this method
                writer.write(newick);
            } catch (IOException e) {
                e.printStackTrace(); // Or show alert
            }
        }
    }
}
