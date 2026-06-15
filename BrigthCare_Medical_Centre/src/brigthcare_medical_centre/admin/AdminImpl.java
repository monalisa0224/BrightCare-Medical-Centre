package brigthcare_medical_centre.admin;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import brigthcare_medical_centre.common.AdminInterface;
import brigthcare_medical_centre.database.DerbyConnection;

public class AdminImpl extends UnicastRemoteObject implements AdminInterface {

    public AdminImpl() throws RemoteException {
        super();
    }

    @Override
    public Admin getAdminById(int adminID) throws RemoteException {
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT UserID, Username, Role FROM USERS WHERE UserID = ? AND Role = 'ADMIN'");
            ps.setInt(1, adminID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin(rs.getInt("UserID"), rs.getString("Username"), rs.getString("Role"));
                rs.close();
                ps.close();
                return admin;
            }
            rs.close();
            ps.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to get admin: " + e.getMessage());
        }
    }

    @Override
    public List<String[]> viewLogs() throws RemoteException {
        List<String[]> logs = new ArrayList<>();
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT l.LogID, l.UserID, u.Username, l.Action, l.Timestamp, l.Details "
                    + "FROM LOGS l LEFT JOIN USERS u ON l.UserID = u.UserID "
                    + "ORDER BY l.Timestamp DESC");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] row = new String[6];
                row[0] = String.valueOf(rs.getInt("LogID"));
                row[1] = String.valueOf(rs.getInt("UserID"));
                row[2] = rs.getString("Username") != null ? rs.getString("Username") : "N/A";
                row[3] = rs.getString("Action");
                row[4] = rs.getTimestamp("Timestamp").toString();
                row[5] = rs.getString("Details") != null ? rs.getString("Details") : "";
                logs.add(row);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to view logs: " + e.getMessage());
        }
        return logs;
    }
}
