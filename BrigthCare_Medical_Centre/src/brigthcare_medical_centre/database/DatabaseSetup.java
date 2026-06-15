package brigthcare_medical_centre.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseSetup {

    public static void initialize() {
        try {
            createTables();
            seedAdmin();
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTables() throws Exception {
        Connection conn = DerbyConnection.getConnection();
        Statement stmt = conn.createStatement();

        if (!tableExists("USERS")) {
            stmt.execute("CREATE TABLE USERS ("
                    + "UserID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "Username VARCHAR(50) UNIQUE NOT NULL, "
                    + "PasswordHash VARCHAR(256) NOT NULL, "
                    + "Role VARCHAR(20) NOT NULL, "
                    + "CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            System.out.println("Created table: USERS");
        }

        if (!tableExists("LOGS")) {
            stmt.execute("CREATE TABLE LOGS ("
                    + "LogID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "UserID INT, "
                    + "Action VARCHAR(100) NOT NULL, "
                    + "Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "Details VARCHAR(500), "
                    + "FOREIGN KEY (UserID) REFERENCES USERS(UserID))");
            System.out.println("Created table: LOGS");
        }

        if (!tableExists("REPORTS")) {
            stmt.execute("CREATE TABLE REPORTS ("
                    + "ReportID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "AdminID INT NOT NULL, "
                    + "ReportType VARCHAR(50) NOT NULL, "
                    + "GeneratedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "Parameters VARCHAR(200), "
                    + "ResultData CLOB, "
                    + "FOREIGN KEY (AdminID) REFERENCES USERS(UserID))");
            System.out.println("Created table: REPORTS");
        }

        stmt.close();
    }

    private static boolean tableExists(String tableName) throws Exception {
        Connection conn = DerbyConnection.getConnection();
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), null);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }

    private static void seedAdmin() throws Exception {
        Connection conn = DerbyConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USERS WHERE Username = ?");
        ps.setString(1, "admin");
        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        ps.close();

        if (count == 0) {
            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)");
            insert.setString(1, "admin");
            insert.setString(2, hashPassword("admin123"));
            insert.setString(3, "ADMIN");
            insert.executeUpdate();
            insert.close();
            System.out.println("Seeded default admin account.");
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }
}
