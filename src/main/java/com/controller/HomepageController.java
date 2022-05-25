package com.controller;

import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import menu.MenuChange;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.controller.LoginController.loginRole;

public class HomepageController implements Initializable {
    LaunchUI main = new LaunchUI();

    public HBox userButton;

    public HBox settingButton;

    public HBox visitorButton;

    public HBox bookingButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //disabling menu options depending on roles
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

    public void residentButtonClicked() {
        try {
            main.changeScreen("/manageResident.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void visitorButtonClicked() {
        try {
            main.changeScreen("/manageVisitor.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void userButtonClicked() {
        try {
            main.changeScreen("/manageUser.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bookingButtonClicked() {
        try {
            main.changeScreen("/manageBooking.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insightButtonClicked() {
        try {
            main.changeScreen("/insight.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void settingButtonClicked() {
        try {
            main.changeScreen("/setting.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void signOutButtonClicked() throws SQLException {
        MenuChange.signout();
    }
}