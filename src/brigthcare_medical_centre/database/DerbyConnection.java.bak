package brigthcare_medical_centre.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import brigthcare_medical_centre.util.Constants;

public class DerbyConnection {
    private static volatile boolean driverLoaded;

    public static Connection getConnection() throws SQLException {
        ensureDriverLoaded();
        return DriverManager.getConnection(Constants.DB_URL);
    }

    private static synchronized void ensureDriverLoaded() throws SQLException {
        if (!driverLoaded) {
            try {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException("Derby driver not found: " + e.getMessage());
            }
        }
    }

    public static synchronized void closeConnection() {
        // Connections are short-lived per call and closed by try-with-resources.
    }
}
