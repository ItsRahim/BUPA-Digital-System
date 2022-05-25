package com.controller;

import database.dbConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class editEmployeeController implements Initializable {

    Integer empID;

    @FXML
    public TextField firstNameField;

    @FXML
    public TextField lastNameField;

    @FXML
    public ComboBox<String> roleMenu;

    @FXML
    public TextField emailField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public PasswordField confirmPasswordField;

    @FXML
    public Button closeButton;

    ObservableList<String> roleList = FXCollections.observableArrayList("Manager", "Activity", "Receptionist");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleMenu.setItems(roleList);
    }

    public void setData(Integer ID, String fName, String lName, String email, String role) {
        empID = ID;
        firstNameField.setText(fName);
        lastNameField.setText(lName);
        emailField.setText(email);
        roleMenu.setValue(role);
    }

    public void updateButton() throws SQLException {
        if (!firstNameField.getText().isBlank() && !lastNameField.getText().isBlank()
                && !emailField.getText().isBlank() && roleMenu.getValue() != null
                && !passwordField.getText().isBlank() && !confirmPasswordField.getText().isBlank()
                && (passwordField.getText().equals(confirmPasswordField.getText()))) {
            String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(10));
            updateEmployee(hashedPassword);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Please check the details again");
            alert.showAndWait();
        }
    }

    private void updateEmployee(String hashedPassword) throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = dbConnector.getDbConn()
                    .prepareCall("{call updateEmployee (?, ?, ?, ?, ?, ?)}");
            stmt.setInt(1, empID);
            stmt.setString(2, firstNameField.getText());
            stmt.setString(3, lastNameField.getText());
            stmt.setString(4, roleMenu.getValue());
            stmt.setString(5, emailField.getText());
            stmt.setString(6, hashedPassword);
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