module com.example.serialread {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;
    requires java.desktop;
    requires javafx.swing;
    requires opencsv;
    requires MaterialFX;


    opens com.example.serialread to javafx.fxml;
    exports com.example.serialread;
}