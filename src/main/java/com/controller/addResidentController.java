package com.controller;

import database.dbConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import java.util.Objects;
import java.util.ResourceBundle;

public class addResidentController implements Initializable {

    @FXML
    public TextField firstNameField;

    @FXML
    public TextField lastNameField;

    @FXML
    public Button closeButton;

    @FXML
    public DatePicker dobMenu;

    @FXML
    public ComboBox<Integer> roomMenu;

    @FXML
    public ComboBox<String> unitMenu;

    @FXML
    public ComboBox<Integer> bandMenu;

    @FXML
    public ComboBox<String> fundingMenu;

    ObservableList<Integer> availableRooms = FXCollections.observableArrayList();
    ObservableList<String> units = FXCollections.observableArrayList("Rose", "Hazel", "Willow");
    ObservableList<Integer> bands = FXCollections.observableArrayList(1, 2, 3, 4, 5);
    ObservableList<String> funding = FXCollections.observableArrayList("Self Funder",
            "Lambeth", "CCL London", "Southwark", "Westminster", "Other");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            checkAvailableRooms();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        unitMenu.setItems(units);
        bandMenu.setItems(bands);
        fundingMenu.setItems(funding);
    }

    private void checkAvailableRooms() throws SQLException {
        /*
        Method to remove all occupied rooms
         */
        for (int i = 1; i < 81; i++) {
            availableRooms.add(i);
        }
        Statement stmt = dbConnector.getDbConn().createStatement();
        String query = "SELECT room_number FROM Resident";
        ResultSet rs = stmt.executeQuery(query);

        //removing already occupied rooms from list of available rooms
        while (rs.next()){
            availableRooms.removeIf(room -> {
                try {
                    return Objects.equals(room, rs.getInt(1));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return false;
            });
        }
        roomMenu.setItems(availableRooms);
        rs.close();
        stmt.close();
    }

    public void saveButton() throws SQLException {
        if(firstNameField.getText().isBlank() || lastNameField.getText().isBlank()
            || dobMenu.getValue() == null || roomMenu.getValue()==null
            || unitMenu.getValue() == null || bandMenu.getValue() == null
            || fundingMenu.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Please check the details again");
            alert.showAndWait();
        }else {
            insertResident();
        }
    }

    private void insertResident() throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = dbConnector.getDbConn()
                    .prepareCall("{call addNewResident (?, ?, ?, ?, ?, ?, ?)}");
            stmt.setString(1, firstNameField.getText());
            stmt.setString(2, lastNameField.getText());
            stmt.setDate(3, Date.valueOf(dobMenu.getValue()));
            stmt.setInt(4, roomMenu.getValue());
            stmt.setString(5, unitMenu.getValue());
            stmt.setInt(6, bandMenu.getValue());
            stmt.setString(7, fundingMenu.getValue());
            stmt.executeUpdate();
            dbConnector.getDbConn().commit();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("New Resident Added");
            alert.setContentText("New Resident has been added");
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
