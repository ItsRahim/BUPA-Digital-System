package com.controller;

import database.dbConnector;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.uuid.Generators;

public class LoginController {

    public static UUID uuid;
    public static int loginID =0;
    public static String loginRole = "";

    @FXML
    public Label invalidLoginLabel;

    @FXML
    public Button loginButton;

    @FXML
    public Button quitButton;


    @FXML
    public TextField usernameTextField;

    @FXML
    public PasswordField passwordTextField;

    public void loginButtonAction() {
        if (!usernameTextField.getText().isBlank() && !passwordTextField.getText().isBlank()) {
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();
            validateLogin(username, password);
        } else {
            displayInvalidLogin();
        }
    }

    private void validateLogin(String username, String password) {
        LaunchUI main = new LaunchUI();

        //TODO: Add new roles later
        HashMap<String, String> loginData = new HashMap<>();
        loginData.put("login_only","NONE" );
        loginData.put("manager", "NONE");
        loginData.put("activity", "NONE");
        loginData.put("receptionist", "NONE");

        try {
            String query = "SELECT employee_id, email, e_password, work_role FROM Employee WHERE email = ?";
            PreparedStatement statement = Objects.requireNonNull(
                    dbConnector.Connect("login_only", "$myLogin123"))
                    .prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                //decrypting password and verifying it is valid
                if(BCrypt.checkpw(password, rs.getString("e_password"))){
                    String user = rs.getString(4).toLowerCase(Locale.ROOT);
                    int userID = rs.getInt(1);
                    loginRole = rs.getString(4);
                    loginID = userID;

                    //changing db connection
                    dbConnector.closeConn();
                    dbConnector.Connect(user, loginData.get(user));

                    addUserLog(userID);
                    invalidBookings();
                    generateActivitySheetsUnit();
                    
                    main.changeScreen("/homepage.fxml");
                } else {
                    displayInvalidLogin();
                }
            }else {
                rs.close();
                displayInvalidLogin();
            }
            dbConnector.getDbConn().commit();
            statement.close();
            rs.close();
        } catch (Exception e) {
            try {
                Objects.requireNonNull(dbConnector.Connect("login_only", "$myLogin123")).rollback();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void generateActivitySheetsUnit() throws IOException {
        try {
            CallableStatement st = dbConnector.getDbConn().prepareCall("{call activityPerUnit}");
            ResultSet rs = st.executeQuery();
            ExcelWriter.writeToExcel(rs, "Activity per Unit.csv");
            st.close();
            rs.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally{
            generateActivitySheetsResident();
        }
    }

    private void generateActivitySheetsResident() throws IOException {
        ArrayList<String> units = new ArrayList<>(Arrays.asList("Willow", "Rose", "Hazel"));
        for (String unit : units) {
            try {
                CallableStatement st = dbConnector.getDbConn().prepareCall("{call activityPerResident(?)}");
                st.setString(1, unit);
                ResultSet rs = st.executeQuery();
                ExcelWriter.writeToExcel(rs, "Activity per Resident in " + unit+ ".csv");
                st.close();
                rs.close();
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
        InsightController.runScript();
    }

    private void invalidBookings() throws SQLException {
        LocalDate today = LocalDate.now();
        CallableStatement stmt = null;
        try {
            stmt = dbConnector.getDbConn()
                    .prepareCall("{call invalidateBooking (?)}");
            stmt.setDate(1, Date.valueOf(today));
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
        }
    }

    private void addUserLog(int employeeID) throws SQLException {
        java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());
        uuid = Generators.timeBasedGenerator().generate();

        CallableStatement stmt = null;
        try {
            stmt = dbConnector.getDbConn()
                    .prepareCall("{call addToLog (?, ?, ?)}");
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, employeeID);
            stmt.setString(3, String.valueOf(now));
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
        }
    }

    private void displayInvalidLogin() {
        invalidLoginLabel.setText("Please enter a valid username or password");
    }

    public void quitButtonAction() throws SQLException {
        dbConnector.closeConn();
        Stage stage = (Stage) quitButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void openLink() throws IOException {
        String url = "www.bupa.co.uk";
        java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
    }
}
