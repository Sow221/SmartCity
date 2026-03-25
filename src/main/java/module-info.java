module com.example.smartcity1 {
    //requires javafx.controls;
    requires javafx.fxml;

   // requires org.controlsfx.controls;
    //requires org.kordamp.ikonli.javafx;

    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires java.sql;

    // Exporte ton package principal pour que JavaFX puisse accéder à MainApp
    opens com.smartcity.app to javafx.graphics, javafx.fxml;

    // Exporte aussi tes contrôleurs si tu utilises FXML
    opens com.smartcity.controller to javafx.fxml;

    // Exporte tes modèles si tu les utilises dans FXML
    opens com.smartcity.model to javafx.base;



    opens com.example.smartcity1 to javafx.fxml;
    exports com.example.smartcity1;
}