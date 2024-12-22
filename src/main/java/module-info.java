module com.example.focus {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.focus to javafx.fxml;
    exports com.example.focus;
}