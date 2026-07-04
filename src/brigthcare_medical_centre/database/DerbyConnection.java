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
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Derby driver not found: " + e.getMessage());
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
