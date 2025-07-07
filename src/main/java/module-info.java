module Advanced_Java_for_Bioinformatics {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    exports assignment1.anatomy;
    exports assignment02.anatomy;
    opens assignment02.anatomy;

    opens assignment03.window to javafx.fxml;
    exports assignment03;

    opens assignment4.window to javafx.fxml;
    exports assignment4;

    opens assignment5.window to javafx.fxml;
    exports assignment5;

    opens assignment6.window to javafx.fxml;
    exports assignment6;

    opens HumanAnatomyViewer.window to javafx.fxml;
    exports HumanAnatomyViewer;
    opens HumanAnatomyViewer.model to javafx.fxml;


}
