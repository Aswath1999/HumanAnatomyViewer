module Advanced_Java_for_Bioinformatics {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    exports assignment1.anatomy;
    exports assignment02.anatomy;
    opens assignment02.anatomy;

    opens assignment03.window to javafx.fxml;
    exports assignment03;

    opens assignment4.window to javafx.fxml;
    exports assignment4;

}
