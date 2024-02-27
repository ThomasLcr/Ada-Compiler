module eu.telecomnancy.application {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens eu.telecomnancy.application to javafx.fxml;
    exports eu.telecomnancy.application;
}