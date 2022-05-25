package database;

import java.sql.*;
import java.util.Objects;

public class dbConnector {
    private static Connection dbConn;

    private static final String url = "jdbc:mysql://localhost:3306/dbName";

    public static Connection Connect(String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            //connecting to MySQL database
            dbConn = DriverManager.getConnection(url, username, password);
            if (dbConn != null) {
                Objects.requireNonNull(dbConn).setAutoCommit(false);
                dbConn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                return getDbConn();
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("DB Connection Unsuccessful");
        }
        return null;
    }

    public static Connection getDbConn() {
        return dbConn;
    }

    public static void closeConn() throws SQLException {
        dbConn.close();
    }
}
