package com.controller;

import constructors.Resident;
import database.dbConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import menu.MenuChange;
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

public class manageResidentController implements Initializable {

    @FXML
    public TableView<Resident> residentTable;

    @FXML
    public TableColumn<Resident, Integer> idColumn;

    @FXML
    public TableColumn<Resident, String> firstNameColumn;

    @FXML
    public TableColumn<Resident, String> lastNameColumn;

    @FXML
    public TableColumn<Resident, String> unitColumn;

    @FXML
    public TableColumn<Resident, String> dobColumn;

    @FXML
    public TableColumn<Resident, String> roomColumn;

    @FXML
    public HBox userButton;

    @FXML
    public HBox settingButton;

    @FXML
    public HBox visitorButton;

    @FXML
    public HBox bookingButton;

    ObservableList<Resident> ResidentList = FXCollections.observableArrayList();

    Resident resident = null;

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
            bookingButton.setDisable(true);
        }
        loadData();
    }

    private void loadData() {
        refreshClicked();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("date_of_birth"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
    }

    public void addResidentClicked() throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/addResident.fxml")));
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setTitle("BUPA Digital Systems");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void editResidentClicked() throws IOException {
        Parent parent;
        resident = residentTable.getSelectionModel().getSelectedItem();
        if(this.resident == null) {
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setHeaderText(null);
            warning.setContentText("Please select a record");
            warning.showAndWait();
        }
        else {
            Integer ID = resident.getId();
            String firstName = resident.getFirstName();
            String lastName = resident.getLastName();
            int roomNumber = resident.getRoomNumber();
            String unit = resident.getUnit();
            int band = resident.getBand();
            String funding = resident.getFundingType();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editResident.fxml"));
            parent = loader.load();

            editResidentController res = loader.getController();
            res.setData(ID, firstName, lastName, roomNumber, unit, band, funding);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("BUPA Digital Systems");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
    }

    public void deleteResidentClicked() throws SQLException {
        resident = residentTable.getSelectionModel().getSelectedItem();
        if(this.resident == null){
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setHeaderText(null);
            warning.setContentText("Please select a record");
            warning.showAndWait();
        }else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Record Deletion Alert");
            alert.setContentText("Do you wish to delete " +
                    resident.getFirstName() + " " +
                    resident.getLastName() +
                    " from the record?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                deleteResident();
            }else if(result.get() == ButtonType.CANCEL) {
                alert.close();
            }
        }
    }

    private void deleteResident() throws SQLException{
        CallableStatement stmt = null;
        try {
            stmt = dbConnector.getDbConn().prepareCall("{call removeResident (?)}");
            stmt.setInt(1, resident.getId());
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
            ResidentList.clear();
            CallableStatement stmt = dbConnector.getDbConn().prepareCall("{call getResidentInfo}");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                ResidentList.add(new Resident(
                        rs.getInt("resident_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("date_of_birth"),
                        rs.getInt("room_number"),
                        rs.getString("unit"),
                        rs.getInt("band"),
                        rs.getString("funding_type")));
                residentTable.setItems(ResidentList);
            }
            stmt.close();
        } catch (SQLException e) {
            Logger.getLogger(LaunchUI.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    //TODO: MVP
    public void addLogClicked() throws IOException {
        Parent parent;
        resident = residentTable.getSelectionModel().getSelectedItem();
        if (this.resident == null) {
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setHeaderText(null);
            warning.setContentText("Please select a resident");
            warning.showAndWait();
        } else {
            Integer residentID = resident.getId();
            Integer employeeID = LoginController.loginID;
            String name = resident.getFirstName() + " " + resident.getLastName();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addActivityLog.fxml"));
            parent = loader.load();

            activityLogController activity = loader.getController();
            activity.setData(residentID, employeeID, name);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("BUPA Digital Systems");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
    }

    public void exportToCSV() {
        resident = residentTable.getSelectionModel().getSelectedItem();
        if (this.resident == null) {
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setHeaderText(null);
            warning.setContentText("Please select a resident");
            warning.showAndWait();
        } else {
            try {
                CallableStatement st = dbConnector.getDbConn().prepareCall("{call generateResidentLog (?)}");
                st.setInt(1, resident.getId());
                ResultSet rs = st.executeQuery();
                ExcelWriter.writeToExcel(rs, "Activity Log for " + resident.getFirstName()
                                + " " + resident.getLastName()+ ".csv");
                st.close();
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
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
    public void manageBookingClicked() {
        MenuChange.booking();
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
