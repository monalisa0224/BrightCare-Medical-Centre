package brigthcare_medical_centre.auth;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import brigthcare_medical_centre.common.AuthenticationInterface;
import brigthcare_medical_centre.database.AuditLogger;
import brigthcare_medical_centre.database.DatabaseSetup;
import brigthcare_medical_centre.database.DerbyConnection;

public class AuthenticationImpl extends UnicastRemoteObject implements AuthenticationInterface {

    public AuthenticationImpl() throws RemoteException {
        super();
    }

    @Override
    public User login(String username, String password) throws RemoteException {
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT UserID, Username, PasswordHash, Role FROM USERS WHERE Username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("PasswordHash");
                String inputHash = DatabaseSetup.hashPassword(password);

                if (storedHash.equals(inputHash)) {
                    User user = new User();
                    user.setUserID(rs.getInt("UserID"));
                    user.setUsername(rs.getString("Username"));
                    user.setRole(UserRole.valueOf(rs.getString("Role")));
                    rs.close();
                    ps.close();

                    AuditLogger.log(user.getUserID(), "LOGIN", "User " + username + " logged in successfully");
                    return user;
                }
            }
            rs.close();
            ps.close();
            AuditLogger.log(0, "LOGIN_FAILED", "Failed login attempt for username: " + username);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Login failed: " + e.getMessage());
        }
    }
}
