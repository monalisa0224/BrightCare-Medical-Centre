package brigthcare_medical_centre.admin;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import brigthcare_medical_centre.common.AdminInterface;
import brigthcare_medical_centre.auth.UserRole;
import brigthcare_medical_centre.database.AuditLogger;
import brigthcare_medical_centre.database.AdminProvisioningDefaults;
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
        try (Connection conn = DerbyConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (usernameExists(conn, username)) {
                    conn.rollback();
                    return false;
                }

                String passwordHash = DatabaseSetup.hashPassword(password);
                try (PreparedStatement insertPs = conn.prepareStatement(
                        "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)")) {
                    insertPs.setString(1, username);
                    insertPs.setString(2, passwordHash);
                    insertPs.setString(3, role.name());
                    if (insertPs.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                insertRoleProfile(conn, username, role);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            AuditLogger.log(0, "USER_REGISTERED",
                    "Admin registered new user: " + username + " with role: " + role.name());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to register user: " + e.getMessage());
        }
    }

    @Override
    public boolean updateUserRole(int userID, UserRole newRole) throws RemoteException {
        try (Connection conn = DerbyConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                UserSnapshot snapshot = getUserSnapshot(conn, userID);
                if (snapshot == null) {
                    conn.rollback();
                    return false;
                }

                if ("ADMIN".equals(snapshot.role)) {
                    conn.rollback();
                    throw new RemoteException("Cannot change the role of admin users");
                }

                if (snapshot.role.equals(newRole.name())) {
                    conn.rollback();
                    return true;
                }

                validateRoleChange(conn, snapshot, newRole);
                deleteRoleProfile(conn, snapshot.username, snapshot.role, false);

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE USERS SET Role = ? WHERE UserID = ?")) {
                    ps.setString(1, newRole.name());
                    ps.setInt(2, userID);
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                insertRoleProfile(conn, snapshot.username, newRole);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            AuditLogger.log(0, "ROLE_UPDATED",
                    "Admin updated user ID " + userID + " role to " + newRole.name());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to update user role: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteUser(int userID) throws RemoteException {
        try (Connection conn = DerbyConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                UserSnapshot snapshot = getUserSnapshot(conn, userID);
                if (snapshot == null) {
                    conn.rollback();
                    return false;
                }

                if ("ADMIN".equals(snapshot.role)) {
                    conn.rollback();
                    return false;
                }

                ensureUserDeletionAllowed(conn, snapshot);
                deleteRoleProfile(conn, snapshot.username, snapshot.role, true);

                try (PreparedStatement logPs = conn.prepareStatement("DELETE FROM LOGS WHERE UserID = ?")) {
                    logPs.setInt(1, userID);
                    logPs.executeUpdate();
                }

                try (PreparedStatement reportPs = conn.prepareStatement("DELETE FROM REPORTS WHERE AdminID = ?")) {
                    reportPs.setInt(1, userID);
                    reportPs.executeUpdate();
                }

                try (PreparedStatement userPs = conn.prepareStatement("DELETE FROM USERS WHERE UserID = ?")) {
                    userPs.setInt(1, userID);
                    if (userPs.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            AuditLogger.log(0, "USER_DELETED", "Admin deleted user ID: " + userID);
            return true;
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

    private boolean usernameExists(Connection conn, String username) throws SQLException {
        try (PreparedStatement checkPs = conn.prepareStatement("SELECT 1 FROM USERS WHERE Username = ?")) {
            checkPs.setString(1, username);
            try (ResultSet rs = checkPs.executeQuery()) {
                return rs.next();
            }
        }
    }

    private UserSnapshot getUserSnapshot(Connection conn, int userID) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT UserID, Username, Role FROM USERS WHERE UserID = ?")) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UserSnapshot(rs.getInt("UserID"), rs.getString("Username"), rs.getString("Role"));
            }
        }
    }

    private void insertRoleProfile(Connection conn, String username, UserRole role) throws SQLException {
        switch (role) {
            case PATIENT:
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO PATIENTS (Username, ContactNumber, Address) VALUES (?, ?, ?)")) {
                    ps.setString(1, username);
                    ps.setString(2, null);
                    ps.setString(3, null);
                    ps.executeUpdate();
                }
                break;
            case DOCTOR:
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO DOCTORS (Username, DoctorName, Specialization, ContactNumber) "
                        + "VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, username);
                    ps.setString(2, AdminProvisioningDefaults.doctorName(username));
                    ps.setString(3, null);
                    ps.setString(4, null);
                    ps.executeUpdate();
                }
                break;
            case RECEPTIONIST:
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO RECEPTIONISTS (Username, FullName, ContactNumber) VALUES (?, ?, ?)")) {
                    ps.setString(1, username);
                    ps.setString(2, AdminProvisioningDefaults.receptionistName(username));
                    ps.setString(3, null);
                    ps.executeUpdate();
                }
                break;
            case ADMIN:
                break;
            default:
                throw new SQLException("Unsupported role: " + role.name());
        }
    }

    private void validateRoleChange(Connection conn, UserSnapshot snapshot, UserRole newRole) throws SQLException {
        if ("DOCTOR".equals(snapshot.role)) {
            Integer doctorId = getDoctorIdByUsername(conn, snapshot.username);
            if (doctorId != null && doctorHasDependencies(conn, doctorId)) {
                throw new SQLException("Doctor accounts with schedules or appointments cannot be re-assigned.");
            }
        }

        if ("ADMIN".equals(newRole.name())) {
            throw new SQLException("Promoting users to ADMIN is not supported from this panel.");
        }
    }

    private void ensureUserDeletionAllowed(Connection conn, UserSnapshot snapshot) throws SQLException {
        if ("PATIENT".equals(snapshot.role) && userHasPatientHistory(conn, snapshot.username)) {
            throw new SQLException("Cannot delete a patient user with appointment or consultation history.");
        }

        if ("DOCTOR".equals(snapshot.role)) {
            Integer doctorId = getDoctorIdByUsername(conn, snapshot.username);
            if (doctorId != null && doctorHasDependencies(conn, doctorId)) {
                throw new SQLException("Cannot delete a doctor user with schedules or appointments.");
            }
        }
    }

    private void deleteRoleProfile(Connection conn, String username, String role, boolean deletingUser)
            throws SQLException {
        if ("PATIENT".equals(role)) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM PATIENTS WHERE Username = ?")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
            return;
        }

        if ("RECEPTIONIST".equals(role)) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM RECEPTIONISTS WHERE Username = ?")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
            return;
        }

        if ("DOCTOR".equals(role)) {
            Integer doctorId = getDoctorIdByUsername(conn, username);
            if (doctorId == null) {
                return;
            }

            if (doctorHasDependencies(conn, doctorId)) {
                throw new SQLException(deletingUser
                        ? "Cannot delete a doctor user with schedules or appointments."
                        : "Doctor accounts with schedules or appointments cannot be re-assigned.");
            }

            try (PreparedStatement schedulePs = conn.prepareStatement(
                    "DELETE FROM DOCTOR_SCHEDULE WHERE DoctorID = ?")) {
                schedulePs.setInt(1, doctorId);
                schedulePs.executeUpdate();
            }
            try (PreparedStatement doctorPs = conn.prepareStatement("DELETE FROM DOCTORS WHERE Username = ?")) {
                doctorPs.setString(1, username);
                doctorPs.executeUpdate();
            }
        }
    }

    private Integer getDoctorIdByUsername(Connection conn, String username) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT DoctorID FROM DOCTORS WHERE Username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("DoctorID") : null;
            }
        }
    }

    private boolean doctorHasDependencies(Connection conn, int doctorId) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM APPOINTMENTS WHERE DoctorID = ?", doctorId) > 0
                || count(conn, "SELECT COUNT(*) FROM CONSULTATION_NOTES WHERE DoctorID = ?", doctorId) > 0
                || count(conn, "SELECT COUNT(*) FROM DOCTOR_SCHEDULE WHERE DoctorID = ?", doctorId) > 0;
    }

    private boolean userHasPatientHistory(Connection conn, String username) throws SQLException {
        return count(conn, "SELECT COUNT(*) FROM APPOINTMENTS WHERE Username = ?", username) > 0
                || count(conn, "SELECT COUNT(*) FROM CONSULTATION_NOTES WHERE PatientUsername = ?", username) > 0;
    }

    private int count(Connection conn, String sql, Object value) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static final class UserSnapshot {
        private final int userId;
        private final String username;
        private final String role;

        private UserSnapshot(int userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }
    }
}
