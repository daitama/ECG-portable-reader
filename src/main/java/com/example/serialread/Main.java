package com.example.serialread;

import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.io.IOException;

public class Main extends Application {

    /*Di awal program berjalan, akan di load file fxml Index atau halaman awal beserta style sheet cssnya.
    * Tiap-tiap FXML terhubung dengan controller class yang berisi fungsi-fungsi yang berjalan diatas FXML yang tertampil
    *  e.x : Index.fxml memiliki controller class bernama IndexController.*/
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Index.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
        scene.getStylesheets().add(getClass().getResource("IndexStyle.css").toExternalForm());
        Image image = new Image("ecg.png");
        stage.getIcons().add(image);
        stage.setTitle("ECG Reader");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}