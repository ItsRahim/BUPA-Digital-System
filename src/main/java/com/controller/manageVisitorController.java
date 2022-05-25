package com.controller;

import constructors.Visitor;
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

public class manageVisitorController implements Initializable {

    @FXML
    public TableView<Visitor> visitorTable;

    @FXML
    public TableColumn<Visitor, Integer> idColumn;

    @FXML
    public TableColumn<Visitor, String> firstNameColumn;

    @FXML
    public TableColumn<Visitor, String> lastNameColumn;

    @FXML
    public TableColumn<Visitor, String> dobColumn;

    @FXML
    public TableColumn<Visitor, String> emailColumn;

    @FXML
    public TableColumn<Visitor, String> phoneColumn;

    @FXML
    public HBox userButton;

    @FXML
    public HBox settingButton;

    ObservableList<Visitor> VisitorList = FXCollections.observableArrayList();

    Visitor visitor = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (loginRole.equals("Manager") || loginRole.equals("Admin")) {
            userButton.setDisable(false);
            settingButton.setDisable(false);
        } else {
            userButton.setDisable(true);
            settingButton.setDisable(true);
        }
        loadData();
    }

    private void loadData() {
        refreshClicked();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("visitorID"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("date_of_birth"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
    }

    public void refreshClicked() {
        try {
            VisitorList.clear();
            CallableStatement stmt = dbConnector.getDbConn().prepareCall("{call getVisitorInfo}");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                VisitorList.add(new Visitor(
                        rs.getInt("visitor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("dob"),
                        rs.getString("email"),
                        rs.getString("phone_number")));
                visitorTable.setItems(VisitorList);
            }
            stmt.close();
        } catch (SQLException e) {
            Logger.getLogger(LaunchUI.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void deleteVisitorClicked() throws SQLException {
        visitor = visitorTable.getSelectionModel().getSelectedItem();
        if(this.visitor == null){
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setHeaderText(null);
            warning.setContentText("Please select a record");
            warning.showAndWait();
        }else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Record Deletion Alert");
            alert.setContentText("Do you wish to delete " +
                    visitor.getFirstName() + " " +
                    visitor.getLastName() +
                    " from the record?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                deleteVisitor();
            }else if(result.get() == ButtonType.CANCEL) {
                alert.close();
            }
        }
    }

    private void deleteVisitor() throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = dbConnector.getDbConn().prepareCall("{call removeVisitor (?)}");
            stmt.setInt(1, visitor.getVisitorID());
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

    public void addVisitorClicked() throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/addVisitor.fxml")));
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setTitle("BUPA Digital Systems");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void editVisitorClicked() throws IOException {
        Parent parent;
        visitor = visitorTable.getSelectionModel().getSelectedItem();
        if(this.visitor == null) {
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setHeaderText(null);
            warning.setContentText("Please select a record");
            warning.showAndWait();
        }
        else {
            Integer ID = visitor.getVisitorID();
            String firstName = visitor.getFirstName();
            String lastName = visitor.getLastName();
            String email = visitor.getEmail();
            String phone = visitor.getPhoneNumber();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editVisitor.fxml"));
            parent = loader.load();

            editVisitorController vis = loader.getController();
            vis.setData(ID, firstName, lastName, email, phone);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("BUPA Digital Systems");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
    }
    @FXML
    public void homeButtonClicked() throws IOException {
        MenuChange.home();
    }

    @FXML
    public void residentButtonClicked (){
        MenuChange.resident();
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
