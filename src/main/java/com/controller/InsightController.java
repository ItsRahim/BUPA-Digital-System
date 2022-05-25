package com.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import menu.MenuChange;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.controller.LoginController.loginRole;

public class InsightController implements Initializable {

    @FXML
    public ImageView imageSetter;

    @FXML
    public HBox userButton;

    @FXML
    public HBox settingButton;

    @FXML
    public HBox visitorButton;

    @FXML
    public HBox bookingButton;

    public static Runtime rt = Runtime.getRuntime();
    public static Process pr;

    Image image;
    String rose = "src/scripts/rose.png";
    String hazel = "src/scripts/hazel.png";
    String willow = "src/scripts/willow.png";
    String unit = "src/scripts/unit.png";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (loginRole.equals("Manager") || loginRole.equals("Admin")) {
            userButton.setDisable(false);
            settingButton.setDisable(false);
            visitorButton.setDisable(false);
        } else {
            userButton.setDisable(true);
            settingButton.setDisable(true);
        } if (loginRole.equals("Activity")) {
            visitorButton.setDisable(true);
            bookingButton.setDisable(true);
        }
    }

    public void roseClicked() {
        try {
            image = new Image(new FileInputStream(rose));
            imageSetter.setImage(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void willowClicked() {
        try {
            image = new Image(new FileInputStream(willow));
            imageSetter.setImage(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void hazelClicked() {
        try {
            image = new Image(new FileInputStream(hazel));
            imageSetter.setImage(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void unitClicked() {
        try {
            image = new Image(new FileInputStream(unit));
            imageSetter.setImage(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void runScript() throws IOException {
        pr = rt.exec("cmd /c start \"\" graphRun.vbs");
    }

    @FXML
    public void homeButtonClicked() throws IOException {
        MenuChange.home();
    }

    @FXML
    public void residentButtonClicked() {
        MenuChange.resident();
    }

    @FXML
    public void visitorButtonClicked (){
        MenuChange.visitor();
    }

    @FXML
    public void manageUserClicked() {
        MenuChange.user();
    }

    @FXML
    public void manageBookingClicked() {
        MenuChange.booking();
    }

    @FXML
    public void settingsClicked() {
        MenuChange.setting();
    }

    @FXML
    public void signoutClicked() throws SQLException {
        MenuChange.signout();
    }
}
