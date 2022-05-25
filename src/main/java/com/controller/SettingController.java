package com.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import menu.MenuChange;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static com.controller.LoginController.loginRole;

public class SettingController implements Initializable {
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

    @FXML
    public void homeButtonClicked() throws IOException {
        MenuChange.home();
    }

    @FXML
    public void visitorButtonClicked(){
        MenuChange.visitor();
    }

    public void residentButtonClicked() {
        MenuChange.resident();
    }

    @FXML
    public void userButtonClicked() {
        MenuChange.user();
    }

    @FXML
    public void bookingButtonClicked() {
        MenuChange.booking();
    }

    @FXML
    public void insightButtonClicked() {
        MenuChange.insight();
    }

    @FXML
    public void signOutButtonClicked() throws SQLException {
        MenuChange.signout();
    }
}
