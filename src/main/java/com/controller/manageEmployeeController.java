package com.controller;

import database.dbConnector;
import menu.MenuChange;
import constructors.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class manageEmployeeController implements Initializable {

    @FXML
    public Button addUser;

    @FXML
    public Button refresh;

    @FXML
    public Button delete;

    @FXML
    public Button edit;

    @FXML
    public Button log;

    @FXML
    public TableView<Employee> employeeTable;

    @FXML
    public TableColumn<Employee, Integer> idColumn;

    @FXML
    public TableColumn<Employee, String> firstNameColumn;

    @FXML
    public TableColumn<Employee, String> lastNameColumn;

    @FXML
    public TableColumn<Employee, String> roleColumn;

    @FXML
    public TableColumn<Employee, String> emailColumn;

    @FXML
    public TableColumn<Employee, String> passwordColumn;

    ObservableList<Employee> EmployeeList = FXCollections.observableArrayList();

    Employee employee = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadData() throws SQLException {
        refreshClicked();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
    }

    @FXML
    public void addUserClicked() throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/addUser.fxml")));
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setTitle("BUPA Digital Systems");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    public void editClicked() throws IOException {
        Parent parent;
        employee = employeeTable.getSelectionModel().getSelectedItem();
        if(this.employee == null) {
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setHeaderText(null);
            warning.setContentText("Please select a record");
            warning.showAndWait();
        }
        else {
            Integer ID = employee.getId();
            String firstName = employee.getFirstName();
            String lastName = employee.getLastName();
            String email = employee.getEmail();
            String role = employee.getRole();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editUser.fxml"));
            parent = loader.load();

            editEmployeeController emp = loader.getController();
            emp.setData(ID, firstName, lastName, email, role);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("BUPA Digital Systems");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
    }

    @FXML
    public void refreshClicked() {
        try {
            EmployeeList.clear();
            CallableStatement stmt = dbConnector.getDbConn().prepareCall("{call getEmployeeInfo}");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                EmployeeList.add(new Employee(
                        rs.getInt("employee_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("work_role"),
                        rs.getString("email"),
                        rs.getString("e_password")));
                employeeTable.setItems(EmployeeList);
            }
            stmt.close();
        } catch (SQLException e) {
            Logger.getLogger(LaunchUI.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @FXML
    public void deleteClicked() throws SQLException {
        employee = employeeTable.getSelectionModel().getSelectedItem();
        if(this.employee == null){
            Alert warning = new Alert(Alert.AlertType.ERROR);
            warning.setHeaderText(null);
            warning.setContentText("Please select a record");
            warning.showAndWait();
        }else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Record Deletion Alert");
            alert.setContentText("Do you wish to delete " +
                    employee.getFirstName() + " " +
                    employee.getLastName() +
                    " from the record?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                deleteEmployee();
            }else if(result.get() == ButtonType.CANCEL) {
                alert.close();
            }
        }
    }

    private void deleteEmployee() throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = dbConnector.getDbConn().prepareCall("{call removeEmployee (?)}");
            stmt.setInt(1, employee.getId());
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

    @FXML
    public void userLogClicked() {
        try {
            CallableStatement st = dbConnector.getDbConn().prepareCall("{call createUserLog}");
            ResultSet rs = st.executeQuery();
            ExcelWriter.writeToExcel(rs, "User Log Information.csv");
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
    public void manageResidentClicked() {
        MenuChange.resident();
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