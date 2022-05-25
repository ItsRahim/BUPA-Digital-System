package menu;

import com.mysql.cj.jdbc.CallableStatement;
import database.dbConnector;
import com.controller.LaunchUI;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import static com.controller.LoginController.uuid;

public class MenuChange {

    static LaunchUI main = new LaunchUI();

    public static void home() throws IOException {
        main.changeScreen("/homepage.fxml");
    }

    public static void visitor() {
        try {
            main.changeScreen("/manageVisitor.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resident() {
        try{
            main.changeScreen("/manageResident.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void booking() {
        try{
            main.changeScreen("/manageBooking.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void user() {
        try{
            main.changeScreen("/manageUser.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insight() {
        try{
            main.changeScreen("/insight.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setting() {
        try{
            main.changeScreen("/setting.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void signout() throws SQLException {
        java.sql.Timestamp out = new java.sql.Timestamp(new java.util.Date().getTime());
        CallableStatement stmt = null;
        try {
            stmt = (CallableStatement) dbConnector.getDbConn()
                    .prepareCall("{call updateLog (?, ?)}");
            stmt.setString(1, String.valueOf(uuid));
            stmt.setString(2, String.valueOf(out));
            stmt.executeUpdate();
            dbConnector.getDbConn().commit();
            dbConnector.closeConn();
            dbConnector.Connect("login_only", "$myLogin123");
            main.changeScreen("/login.fxml");
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
            uuid = null;
        }
    }
}
