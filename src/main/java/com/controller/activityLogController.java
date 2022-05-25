package com.controller;

import database.dbConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;

public class activityLogController implements Initializable {

    @FXML
    public Label addLogLabel;

    @FXML
    public DatePicker dateMenu;

    @FXML
    public TextField activityField;

    @FXML
    public TextField descriptionField;

    @FXML
    public ComboBox<String> interactionBox;

    @FXML
    public ComboBox<String> engagementBox;

    @FXML
    public ComboBox<String> moodBox;

    @FXML
    public TextArea evidenceField;

    @FXML
    public RadioButton yesRadio;

    @FXML
    public RadioButton noRadio;

    @FXML
    public Button closeButton;

    ToggleGroup tg = new ToggleGroup();

    Integer rID;
    Integer eID;
    String rName = "";

    //populating drop boxes with data
    ObservableList<String> interactionTypes = FXCollections.observableArrayList("High", "Neutral", "Low");
    ObservableList<String> engagementTypes = FXCollections.observableArrayList("High", "Neutral", "Low");
    ObservableList<String> moodTypes = FXCollections.observableArrayList("\uD83D\uDE01", "\uD83D\uDE10", "\uD83D\uDE1E");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tg.getToggles().addAll(yesRadio, noRadio);
        interactionBox.setItems(interactionTypes);
        engagementBox.setItems(engagementTypes);
        moodBox.setItems(moodTypes);
    }

    public void setData(Integer residentID, Integer employeeID, String name) {
        rID = residentID;
        eID = employeeID;
        rName = name;
        addLogLabel.setText("Activity Log for " + rName);
    }

    public void saveButton() throws SQLException {
        if (checkIfFilled() && yesRadio.isSelected()) {
            insertData();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Please check the details again");
            alert.showAndWait();
        }
    }

    private boolean checkIfFilled() {
        //checking to see if the form has been filled correctly
        return dateMenu.getValue() != null && !activityField.getText().isBlank()
                && !descriptionField.getText().isBlank() && interactionBox.getValue() != null
                && engagementBox.getValue() != null && moodBox.getValue() != null
                && !evidenceField.getText().isBlank() || tg.getSelectedToggle() == null;
    }

    private void insertData() throws SQLException {
        CallableStatement stmt = null;
        //generating random id
        String activityID = UUID.randomUUID().toString();
        try {
            stmt = dbConnector.getDbConn()
                    .prepareCall("{call addActivityRecord (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            stmt.setInt(1, rID);
            stmt.setInt(2, eID);
            stmt.setString(3, String.valueOf(dateMenu.getValue()));
            stmt.setString(4, activityField.getText());
            stmt.setString(5, descriptionField.getText());
            stmt.setString(6, interactionBox.getValue());
            stmt.setString(7, engagementBox.getValue());
            stmt.setString(8, moodBox.getValue());
            stmt.setString(9, evidenceField.getText());
            stmt.setString(10, activityID);
            stmt.executeUpdate();
            dbConnector.getDbConn().commit();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(null);
            alert.setContentText("New Log Added for " + rName);
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
