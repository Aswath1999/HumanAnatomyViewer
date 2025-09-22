module Advanced_Java_for_Bioinformatics {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    
    opens HumanAnatomyViewer.window to javafx.fxml;
    exports HumanAnatomyViewer;
    opens HumanAnatomyViewer.model to javafx.fxml;


}
