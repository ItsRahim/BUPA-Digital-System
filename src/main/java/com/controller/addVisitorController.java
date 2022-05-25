package com.controller;

import database.dbConnector;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import java.util.Objects;
import java.util.ResourceBundle;

public class addVisitorController implements Initializable {

    @FXML
    public TextField firstNameField;

    @FXML
    public TextField lastNameField;

    @FXML
    public DatePicker dobMenu;

    @FXML
    public TextField emailField;

    @FXML
    public TextField phoneField;

    @FXML
    public Button closeButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void saveButton() throws SQLException {
        if(firstNameField.getText().isBlank() || lastNameField.getText().isBlank()
                || dobMenu.getValue() == null || emailField.getText().isBlank()
                || phoneField.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Please check the details again");
            alert.showAndWait();
        }else {
            insertVisitor();
        }
    }

    private boolean checkDetails() {
        try{
            CallableStatement stmt = dbConnector.getDbConn().prepareCall("{call checkVisitorDetail(?, ?)}");
            stmt.setString(1, emailField.getText());
            stmt.setString(2, phoneField.getText());
            ResultSet rs = stmt.executeQuery();
            return !rs.isBeforeFirst();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void insertVisitor() throws SQLException {
        CallableStatement stmt = null;
        if(checkDetails())
        {
            try {
                stmt = dbConnector.getDbConn()
                        .prepareCall("{call addNewVisitor (?, ?, ?, ?, ?)}");
                stmt.setString(1, firstNameField.getText());
                stmt.setString(2, lastNameField.getText());
                stmt.setDate(3, Date.valueOf(dobMenu.getValue()));
                stmt.setString(4, emailField.getText());
                stmt.setString(5, phoneField.getText());
                stmt.executeUpdate();
                dbConnector.getDbConn().commit();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("New Visitor Added");
                alert.setContentText("New Visitor has been added");
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
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Visitor with email or phone exists");
            alert.showAndWait();
        }

    }

    public void cancelAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
