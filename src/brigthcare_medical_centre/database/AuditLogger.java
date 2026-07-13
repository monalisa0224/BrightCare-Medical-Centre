package brigthcare_medical_centre.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class AuditLogger {

    public static void log(int userID, String action, String details) {
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO LOGS (UserID, Action, Timestamp, Details) VALUES (?, ?, ?, ?)");
            if (userID > 0) {
                ps.setInt(1, userID);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, action);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, details);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
