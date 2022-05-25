package com.controller;

import database.dbConnector;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class editVisitorController implements Initializable {

    Integer visitorID;

    @FXML
    public TextField firstNameField;

    @FXML
    public TextField lastNameField;

    @FXML
    public TextField emailField;

    @FXML
    public TextField phoneField;

    @FXML
    public Button closeButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setData(Integer ID, String fName, String lName, String email, String phone) {
        visitorID = ID;
        firstNameField.setText(fName);
        lastNameField.setText(lName);
        emailField.setText(email);
        phoneField.setText(phone);
    }

    public void updateButton() throws SQLException {
        if(firstNameField.getText().isBlank() || lastNameField.getText().isBlank()
                || emailField.getText().isBlank() || phoneField.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Please check the details again");
            alert.showAndWait();
        }else {
            updateVisitor();
        }
    }

    private void updateVisitor() throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = dbConnector.getDbConn()
                    .prepareCall("{call updateVisitor (?, ?, ?, ?, ?)}");
            stmt.setInt(1, visitorID);
            stmt.setString(2, firstNameField.getText());
            stmt.setString(3, lastNameField.getText());
            stmt.setString(4, emailField.getText());
            stmt.setString(5, phoneField.getText());
            stmt.executeUpdate();
            dbConnector.getDbConn().commit();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Record Update");
            alert.setContentText("Record has been Updated");
            alert.showAndWait();
        } catch (Exception e) {
            try {
                Objects.requireNonNull(dbConnector.getDbConn()).rollback();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            assert stmt != null;
            stmt.close();
            cancelAction();
        }
    }

    public void cancelAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}