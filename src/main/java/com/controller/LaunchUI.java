package com.controller;

import database.dbConnector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class LaunchUI extends Application {
    private static Stage stage;

    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/login.fxml")));
            Scene scene = new Scene(root);
            stage = primaryStage;
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("BUPA Digital Systems");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeScreen(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
        stage.getScene().setRoot(pane);
    }

    public static void main(String[] args) {
        //initial db connection will be with the read only user
        File toDelete = new File("src/scripts/barcode_result.txt");
        toDelete.delete();
        dbConnector.Connect("login_only","$myLogin123");
        launch();
    }
}