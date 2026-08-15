package brigthcare_medical_centre.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import brigthcare_medical_centre.util.Constants;

public class DerbyConnection {
    private static Connection connection;

public static synchronized Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
        try {
            String dbUrl = Constants.DB_URL;
            if (dbUrl != null && dbUrl.startsWith("jdbc:derby://")) {
                Class.forName("org.apache.derby.jdbc.ClientDriver");   // network DB
            } else {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver"); // local embedded DB
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Derby driver not found: " + e.getMessage(), e);
        }
        connection = DriverManager.getConnection(Constants.DB_URL);
    }
    return connection;
}

    public static synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
