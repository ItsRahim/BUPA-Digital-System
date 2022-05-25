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
import java.sql.*;
import java.util.Objects;
import java.util.ResourceBundle;

public class addEmployeeController implements Initializable {

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
    public DatePicker dobMenu;

    @FXML
    public RadioButton maleRadio;

    @FXML
    public RadioButton femaleRadio;

    @FXML
    public RadioButton otherRadio;

    @FXML
    public Button closeButton;

    ToggleGroup tg = new ToggleGroup();

    ObservableList<String> roleList = FXCollections.observableArrayList("Manager", "Activity", "Receptionist");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleMenu.setItems(roleList);
        tg.getToggles().addAll(maleRadio, femaleRadio, otherRadio);
    }

    public void saveButton() throws SQLException {
        //checking to see if all fields have been filled before saving
        if (!firstNameField.getText().isBlank() && !lastNameField.getText().isBlank()
                && !emailField.getText().isBlank() && roleMenu.getValue() != null
                && !passwordField.getText().isBlank() && !confirmPasswordField.getText().isBlank()
                && (passwordField.getText().equals(confirmPasswordField.getText()))
                && dobMenu.getValue()!= null && (maleRadio.isSelected() ||
                femaleRadio.isSelected() || otherRadio.isSelected())) {
            RadioButton selectedRadioButton = (RadioButton) tg.getSelectedToggle();
            String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(10));
            String gender = selectedRadioButton.getText();
            insertEmployee(hashedPassword, gender);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Please check the details again");
            alert.showAndWait();
        }
    }

    private void insertEmployee(String hashedPassword, String gender) throws SQLException {
        CallableStatement stmt = null;
        if(mailExists()) {
            try {
                //calling a stored procedure to add employee to system
                stmt = dbConnector.getDbConn()
                        .prepareCall("{call addNewEmployee (?, ?, ?, ?, ?, ?, ?)}");
                stmt.setString(1, firstNameField.getText());
                stmt.setString(2, lastNameField.getText());
                stmt.setString(3, roleMenu.getValue());
                stmt.setString(4, emailField.getText());
                stmt.setString(5, hashedPassword);
                stmt.setDate(6, Date.valueOf(dobMenu.getValue()));
                stmt.setString(7, gender);
                stmt.executeUpdate();
                dbConnector.getDbConn().commit();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("New User Added");
                alert.setContentText("New User has been added");
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
            //warning shown if existing email is on database
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Email Exists");
            alert.showAndWait();
        }

    }

    //method to check if email exists on employee database before adding
    private boolean mailExists() {
        ResultSet rs;
        try {
            String query = "SELECT email FROM Employee WHERE email = ?";
            PreparedStatement statement = dbConnector.getDbConn().prepareStatement(query);
            statement.setString(1, emailField.getText());
            rs = statement.executeQuery();
            if (!rs.isBeforeFirst()) {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

        public void cancelAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}