package brigthcare_medical_centre.admin;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import brigthcare_medical_centre.common.AdminInterface;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.auth.UserRole;
import brigthcare_medical_centre.database.AuditLogger;
import brigthcare_medical_centre.database.DatabaseSetup;
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

    @Override
    public boolean registerUser(String username, String password, UserRole role) throws RemoteException {
        try {
            Connection conn = DerbyConnection.getConnection();
            
            // Check if username already exists
            PreparedStatement checkPs = conn.prepareStatement("SELECT UserID FROM USERS WHERE Username = ?");
            checkPs.setString(1, username);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next()) {
                rs.close();
                checkPs.close();
                return false; // Username already exists
            }
            rs.close();
            checkPs.close();

            // Insert new user
            String passwordHash = DatabaseSetup.hashPassword(password);
            PreparedStatement insertPs = conn.prepareStatement(
                    "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            insertPs.setString(1, username);
            insertPs.setString(2, passwordHash);
            insertPs.setString(3, role.name());
            int affectedRows = insertPs.executeUpdate();
            
            int newUserId = -1;
            ResultSet generatedKeys = insertPs.getGeneratedKeys();
            if (generatedKeys.next()) {
                newUserId = generatedKeys.getInt(1);
            }
            generatedKeys.close();
            insertPs.close();
            
            if (affectedRows > 0) {
                AuditLogger.log(0, "USER_REGISTERED", "Admin registered new user: " + username + " with role: " + role.name());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to register user: " + e.getMessage());
        }
    }

    @Override
    public boolean updateUserRole(int userID, UserRole newRole) throws RemoteException {
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE USERS SET Role = ? WHERE UserID = ?");
            ps.setString(1, newRole.name());
            ps.setInt(2, userID);
            int affectedRows = ps.executeUpdate();
            ps.close();
            
            if (affectedRows > 0) {
                AuditLogger.log(0, "ROLE_UPDATED", "Admin updated user ID " + userID + " role to " + newRole.name());
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to update user role: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteUser(int userID) throws RemoteException {
        try {
            Connection conn = DerbyConnection.getConnection();
            
            // Prevent deleting admin users (optional safety check)
            PreparedStatement checkPs = conn.prepareStatement("SELECT Role FROM USERS WHERE UserID = ?");
            checkPs.setInt(1, userID);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && "ADMIN".equals(rs.getString("Role"))) {
                rs.close();
                checkPs.close();
                throw new RemoteException("Cannot delete admin users");
            }
            rs.close();
            checkPs.close();

            PreparedStatement ps = conn.prepareStatement("DELETE FROM USERS WHERE UserID = ?");
            ps.setInt(1, userID);
            int affectedRows = ps.executeUpdate();
            ps.close();
            
            if (affectedRows > 0) {
                AuditLogger.log(0, "USER_DELETED", "Admin deleted user ID: " + userID);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to delete user: " + e.getMessage());
        }
    }

    @Override
    public List<String[]> getAllUsers() throws RemoteException {
        List<String[]> users = new ArrayList<>();
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT u.UserID, u.Username, u.Role, u.CreatedDate, " +
                    "COUNT(l.LogID) as ActivityCount " +
                    "FROM USERS u " +
                    "LEFT JOIN LOGS l ON u.UserID = l.UserID " +
                    "GROUP BY u.UserID, u.Username, u.Role, u.CreatedDate " +
                    "ORDER BY u.CreatedDate DESC");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] row = new String[5];
                row[0] = String.valueOf(rs.getInt("UserID"));
                row[1] = rs.getString("Username");
                row[2] = rs.getString("Role");
                row[3] = rs.getTimestamp("CreatedDate") != null ? rs.getTimestamp("CreatedDate").toString() : "N/A";
                row[4] = String.valueOf(rs.getInt("ActivityCount"));
                users.add(row);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to get users: " + e.getMessage());
        }
        return users;
    }
}
