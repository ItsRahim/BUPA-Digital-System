package com.controller;

import constructors.Booking;
import database.dbConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import menu.MenuChange;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.controller.LoginController.loginRole;

public class manageBookingController implements Initializable {

    @FXML
    public TextField searchTextField;

    @FXML
    public TableView<Booking> bookingTable;

    @FXML
    public TableColumn<Booking, Integer> idColumn;

    @FXML
    public TableColumn<Booking, String> visitorColumn;

    @FXML
    public TableColumn<Booking, String> residentColumn;

    @FXML
    public TableColumn<Booking, Integer> roomColumn;

    @FXML
    public TableColumn<Booking, String> dateColumn;

    @FXML
    public TableColumn<Booking, Integer> statusColumn;

    @FXML
    public HBox userButton;

    @FXML
    public HBox settingButton;

    @FXML
    public HBox visitorButton;

    ObservableList<Booking> BookingList = FXCollections.observableArrayList();

    Booking booking = null;

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
        }
        loadData();
    }

    private void loadData() {
        refreshClicked();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingID"));
        visitorColumn.setCellValueFactory(new PropertyValueFactory<>("visitorName"));
        residentColumn.setCellValueFactory(new PropertyValueFactory<>("residentName"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_of_visit"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        /*
        Code below us method for search function
        Reused from: https://www.youtube.com/watch?v=2M0L6w3tMOY
         */
        FilteredList<Booking> filteredData = new FilteredList<>(BookingList, b -> true);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(booking -> {
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            String toFind = newValue.toLowerCase();

            if (booking.getVisitorName().toLowerCase().contains(toFind)) {
                return true;
            } else
            if (booking.getResidentName().toLowerCase().contains(toFind)) {
                return true;
            }
            else return booking.getBookingID().toLowerCase().contains(toFind);
        }));
        SortedList<Booking> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(bookingTable.comparatorProperty());
        bookingTable.setItems(sortedData);
    }

    public void addBookingClicked() throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/addBooking.fxml")));
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setTitle("BUPA Digital Systems");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void deleteBookingClicked() throws SQLException {
        booking = bookingTable.getSelectionModel().getSelectedItem();
        if(this.booking == null){
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setHeaderText(null);
            warning.setContentText("Please select a record");
            warning.showAndWait();
        }else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Record Deletion Alert");
            alert.setContentText("Do you wish to delete booking for " +
                    booking.getVisitorName() +
                    " from the record?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                deleteBooking();
            }else if(result.get() == ButtonType.CANCEL) {
                alert.close();
            }
        }
    }

    private void deleteBooking() throws SQLException{
        CallableStatement stmt = null;
        try {
            stmt = dbConnector.getDbConn().prepareCall("{call removeBooking (?)}");
            stmt.setString(1, booking.getBookingID());
            stmt.executeUpdate();
            dbConnector.getDbConn().commit();
        } catch (Exception ex) {
            try {
                Objects.requireNonNull(dbConnector.getDbConn()).rollback();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            ex.printStackTrace();
        } finally {
            assert stmt != null;
            stmt.close();
            refreshClicked();
        }
    }

    public void refreshClicked() {
        try {
            BookingList.clear();
            CallableStatement stmt = dbConnector.getDbConn().prepareCall("{call viewBookingInfo}");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                BookingList.add(new Booking(
                        rs.getString("booking_id"),
                        rs.getString("Resident_Name"),
                        rs.getInt("room_number"),
                        rs.getString("Visitor_Name"),
                        rs.getString("date_of_visit"),
                        rs.getString("validity")));
                bookingTable.setItems(BookingList);
            }
            stmt.close();
        } catch (SQLException e) {
            Logger.getLogger(LaunchUI.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void scanQRClicked() throws IOException {
        readQRController.runScript();
        File f = new File("src/scripts/barcode_result.txt");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking");
        alert.setContentText("Scan QR Code");
        alert.showAndWait();
        if((f.exists() && !f.isDirectory())) {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/readQRCode.fxml")));
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("BUPA Digital Systems");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }else {
            Alert warning = new Alert(Alert.AlertType.INFORMATION);
            warning.setHeaderText(null);
            warning.setContentText("Booking Does not Exist");
            warning.showAndWait();
        }
    }

    public void generateCompletedBookings() {
        try {
            CallableStatement st = dbConnector.getDbConn().prepareCall("{call generateCompletedBookings}");
            ResultSet rs = st.executeQuery();
            ExcelWriter.writeToExcel(rs, "Completed Bookings.csv");
            st.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void homeButtonClicked() throws IOException {
        MenuChange.home();
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
    public void residentButtonClicked() {
        MenuChange.resident();
    }

    @FXML
    public void insightsClicked() {
        MenuChange.insight();
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
