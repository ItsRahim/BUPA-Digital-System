package com.controller;

import database.dbConnector;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class readQRController implements Initializable {

    public String currentStatus = "";

    @FXML
    public CheckBox check1;

    @FXML
    public CheckBox check2;

    @FXML
    public CheckBox check3;

    @FXML
    public CheckBox check4;

    @FXML
    public Label displayBookingID;

    @FXML
    public Label dateField;

    @FXML
    public Label visitorField;

    @FXML
    public Label residentField;

    @FXML
    public Label roomField;

    @FXML
    public Label purposeField;

    @FXML
    public Button checkInButton;

    @FXML
    public Button checkOutButton;

    @FXML
    public Button closeButton;

    @FXML
    public GridPane confirmPane;

    @FXML
    public VBox box;

    public static Runtime rt = Runtime.getRuntime();
    public static Process pr;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        confirmPane.setVisible(false);
        confirmPane.managedProperty().bind(confirmPane.visibleProperty());

        checkInButton.setVisible(false);
        checkInButton.managedProperty().bind(checkInButton.visibleProperty());

        checkOutButton.setVisible(false);
        checkOutButton.managedProperty().bind(checkOutButton.visibleProperty());

        getBookingID();
    }

    public static void runScript() throws IOException {
        pr = rt.exec("cmd /c start \"\" script.vbs");
    }

    private void getBookingID() {
        try {
            File myObj = new File("src/scripts/barcode_result.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                displayBookingID.setText(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        addDetails();
    }

    private void addDetails() {
        LocalDate today = LocalDate.now();
        try {
            CallableStatement stmt = dbConnector.getDbConn().prepareCall("{call getQRDetails(?)}");
            stmt.setString(1, displayBookingID.getText());
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            if (!rs.isBeforeFirst()) {
                Alert warning = new Alert(Alert.AlertType.INFORMATION);
                warning.setHeaderText(null);
                warning.setContentText("Booking Does not Exist");
                warning.showAndWait();
            } else {
                while (rs.next()) {
                    dateField.setText(rs.getString(1));
                    visitorField.setText(rs.getString(2));
                    residentField.setText(rs.getString(3));
                    roomField.setText(rs.getString(4));
                    currentStatus = rs.getString(6);
                    purposeField.setText(rs.getString(7));
                }
                if(currentStatus.equals("Pending") && dateField.getText().equals(String.valueOf(today))) {
                    confirmPane.setVisible(true);
                    confirmPane.managedProperty().bind(confirmPane.visibleProperty());
                    checkInButton.setVisible(true);
                    checkInButton.managedProperty().bind(checkInButton.visibleProperty());
                } else if(currentStatus.equals("Active")){
                    checkOutButton.setVisible(true);
                    checkOutButton.managedProperty().bind(checkOutButton.visibleProperty());
                } else if(currentStatus.equals("Invalid")) {
                    Alert warning = new Alert(Alert.AlertType.INFORMATION);
                    warning.setHeaderText(null);
                    warning.setContentText("Booking is Invalid");
                    warning.showAndWait();
                    pr.destroy();
                } else if(!Objects.equals(dateField.getText(), String.valueOf(today))) {
                    Alert warning = new Alert(Alert.AlertType.INFORMATION);
                    warning.setHeaderText(null);
                    warning.setContentText("Booking is for a future date");
                    warning.showAndWait();
                    pr.destroy();
                }
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(LaunchUI.class.getName()).log(Level.SEVERE, null, e);
        }
        finally {
            File toDelete = new File("src/scripts/barcode_result.txt");
            toDelete.delete();
        }
    }

    public void checkInClicked() throws SQLException {
        CallableStatement stmt = null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.now();
        if(check1.isSelected() && check2.isSelected() && check3.isSelected() && check4.isSelected()) {
            try {
                stmt = dbConnector.getDbConn()
                        .prepareCall("{call checkInProcedure (?, ?, ?)}");
                stmt.setString(1, displayBookingID.getText());
                stmt.setString(2, dtf.format(localTime));
                stmt.setString(3, "Active");
                stmt.executeUpdate();
                dbConnector.getDbConn().commit();
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
                Alert warning = new Alert(Alert.AlertType.INFORMATION);
                warning.setHeaderText(null);
                warning.setContentText(visitorField.getText() +
                        " has been checked in");
                warning.showAndWait();
                cancelAction();
            }
        } else {
            Alert warning = new Alert(Alert.AlertType.INFORMATION);
            warning.setHeaderText(null);
            warning.setContentText("Please check the boxes");
            warning.showAndWait();
        }

    }

    public void checkOutClicked() throws SQLException {
        CallableStatement stmt = null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.now();
        try {
            stmt = dbConnector.getDbConn()
                    .prepareCall("{call checkOutProcedure (?, ?, ?)}");
            stmt.setString(1, displayBookingID.getText());
            stmt.setString(2, dtf.format(localTime));
            stmt.setString(3, "Complete");
            stmt.executeUpdate();
            dbConnector.getDbConn().commit();
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
            Alert warning = new Alert(Alert.AlertType.INFORMATION);
            warning.setHeaderText(null);
            warning.setContentText(visitorField.getText() +
                    " has been checked out");
            warning.showAndWait();
            cancelAction();
        }
    }

    public void cancelAction() {
        Stage stage = (Stage) displayBookingID.getScene().getWindow();
        stage.close();
    }
}
