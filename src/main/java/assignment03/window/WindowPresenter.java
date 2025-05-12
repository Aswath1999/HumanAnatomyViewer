package assignment03.window;
import assignment03.model.Model;
import javafx.stage.Stage;

public class WindowPresenter {

    public WindowPresenter(Stage stage, WindowController controller, Model model) {
        // Load tree into the UI
        TreeViewSetup.setup(controller.getAnatomyTreeView(), model.getPartOfRoot());
    }

}
