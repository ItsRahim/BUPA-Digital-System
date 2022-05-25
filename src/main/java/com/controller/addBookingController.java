package com.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import database.dbConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;

public class addBookingController implements Initializable {

    @FXML
    public ComboBox<String> visitorBox;

    @FXML
    public ComboBox<String> residentBox;

    @FXML
    public DatePicker dateMenu;

    @FXML
    public TextField purposeTextField;

    @FXML
    public Label bookingIDLabel;

    @FXML
    public Button closeButton;

    ObservableList<String> visitorNames = FXCollections.observableArrayList();
    ObservableList<String> residentNames = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Code from: https://stackoverflow.com/questions/48238855/how-to-disable-past-dates-in-datepicker-of-javafx-scene-builder
        dateMenu.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                setDisable(empty || date.compareTo(today) < 0 );
            }
        });
        try {
            fillVisitorBox();
            fillResidentBox();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //populating drop boxes with resident and visitor names
        visitorBox.setItems(visitorNames);
        residentBox.setItems(residentNames);
    }

    private void fillVisitorBox() throws SQLException {
        try {
            CallableStatement stmt = dbConnector.getDbConn().prepareCall("{call getVisitorNames}");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                visitorNames.add(rs.getString(1));
            }
            stmt.close();
        } catch (SQLException e) {
            Logger.getLogger(LaunchUI.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void fillResidentBox() throws SQLException {
        try {
            CallableStatement stmt = dbConnector.getDbConn().prepareCall("{call getResidentNames}");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                residentNames.add(rs.getString(1));
            }
            stmt.close();
        } catch (SQLException e) {
            Logger.getLogger(LaunchUI.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void saveButton() throws IOException, WriterException, SQLException {
        if(visitorBox.getValue() == null || residentBox.getValue() == null
            || dateMenu.getValue() == null || purposeTextField.getText() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Please check the details again");
            alert.showAndWait();
        }else {
            generateBookingID();
        }
    }

    private void generateBookingID() throws WriterException, IOException, SQLException {

        //Code from https://www.youtube.com/watch?v=238LfSBwvbs
        String bookingID = UUID.randomUUID().toString();
        String path = "src/main/resources/QRCode/" + bookingID + ".jpg";

        //converting id to a qr code
        BitMatrix matrix = new MultiFormatWriter().encode(bookingID, BarcodeFormat.QR_CODE, 500, 500);
        MatrixToImageWriter.writeToPath(matrix, "jpg", Paths.get(path));
        insertBooking(bookingID);
    }

    private void insertBooking(String bookingID) throws SQLException {
        CallableStatement stmt = null;
        int [] idToAdd = getIDs();
        try {
            bookingIDLabel.setText(bookingID);
            stmt = dbConnector.getDbConn()
                    .prepareCall("{call addNewBooking (?, ?, ?, ?, ?)}");
            stmt.setString(1, bookingID);
            stmt.setInt(2, idToAdd[0]);
            stmt.setInt(3, idToAdd[1]);
            stmt.setDate(4, Date.valueOf(dateMenu.getValue()));
            stmt.setString(5, purposeTextField.getText());
            stmt.executeUpdate();
            dbConnector.getDbConn().commit();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("New Booking Added");
            alert.setContentText("New Booking has been added");
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
            getInfo(bookingID);
            cancelAction();
        }
    }

    private void getInfo(String bookingID) {
        /*
        Method to get booking information after qr code is scanned successfully
         */
        String date = "";
        String residentName = "";
        int roomNumber = 0;
        String visitorName = "";
        String email = "";

        CallableStatement stmt;
        try {
            stmt = dbConnector.getDbConn().prepareCall("{call sendEmail (?)}");
            stmt.setString(1, bookingID);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                date = rs.getString(1);
                residentName = rs.getString(2);
                roomNumber = rs.getInt(3);
                visitorName = rs.getString(4);
                email = rs.getString(5);
            }
            stmt.close();
            rs.close();
            sendEmail(date, residentName, roomNumber, visitorName, email, bookingID);
            
        } catch (SQLException e) {
            Logger.getLogger(LaunchUI.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private void sendEmail(String date, String residentName, int roomNumber,
                           String visitorName, String email, String bookingID) throws SQLException {
        final String sender = "adbb041@gmail.com";
        final String password = "uzwwbabildjyvtzx";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "*");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });
        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(sender));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            msg.setSubject("BUPA Visitor Booking Information - " + bookingID);

            Multipart emailContent = new MimeMultipart();

            MimeBodyPart message = new MimeBodyPart();
            message.setText("Dear " + visitorName + ",\n\nThis is an email confirmation of your booking to Collingwood " +
                    "Court Care Homes.\n\n" +
                    "Visitation Rules:\n" +
                    "•All visitors should avoid the use of public transport when travelling to the site\n" +
                    "•All visitors will be required to comply with any direction given by BUPA/Richmond " +
                    "Village while on site\n" +
                    "•All visitors will be required to have their temperature checked on arrival to their " +
                    "scheduled visit. Any visitors presenting with a temperature over 37.8 Celsius will " +
                    "be refused access.\n" +
                    "•All visitors will be required to wear PPE, namely gloves and a face mask, and will " +
                    "be required to use hand sanitiser on arrival and departure. Any visitors refusing to " +
                    "wear PPE will be refused access.\n" +
                    "•All visitors consent to Bupa holding a copy of this declaration for a period of up to " +
                    "4-year following the date of provisionally booking their visit.\n\n" +
                    "Below are details of your booking along with a QR code to check into the home:\n\n" +
                    "Date of Visit: " + date +
                    "\nResident Name: " + residentName +
                    "\nRoom Number: " + roomNumber +
                    "\n\nPlease arrive 5 minutes early to conduct a lateral flow test and put on PPE\n\n" +
                    "\nWe hope to see you soon,\nCollingwood Court");

            MimeBodyPart QRCode = new MimeBodyPart();
            QRCode.attachFile("src/main/resources/QRCode/"+bookingID+".jpg");

            emailContent.addBodyPart(message);
            emailContent.addBodyPart(QRCode);

            msg.setContent(emailContent);

            Transport.send(msg);
            File toDelete = new File("src/main/resources/QRCode/"+bookingID+".jpg");
            toDelete.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] getIDs() {
        CallableStatement stmt;
        int[] id = new int[0];
        try {
            stmt = dbConnector.getDbConn().prepareCall("{call getID (?, ?)}");
            stmt.setString(1, visitorBox.getValue());
            stmt.setString(2, residentBox.getValue());
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                id = new int[]{rs.getInt(1), rs.getInt(2)};
            }
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger(LaunchUI.class.getName()).log(Level.SEVERE, null, e);
        }
        return id;
    }

    public void cancelAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
