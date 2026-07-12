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
            seedTestData();
            insertSlotsDirect();
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
        
        if (!tableExists("PATIENTS")) {
            stmt.execute("CREATE TABLE PATIENTS ("
                    + "PatientID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "Username VARCHAR(50) UNIQUE NOT NULL, "
                    + "ContactNumber VARCHAR(20), "
                    + "Address VARCHAR(200), "
                    + "FOREIGN KEY (Username) REFERENCES USERS(Username))");
            System.out.println("Created table: PATIENTS");
        }

        if (!tableExists("DOCTORS")) {
            stmt.execute("CREATE TABLE DOCTORS ("
                    + "DoctorID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "Username VARCHAR(50) UNIQUE NOT NULL, "
                    + "DoctorName VARCHAR(100) NOT NULL, "
                    + "Specialization VARCHAR(100), "
                    + "FOREIGN KEY (Username) REFERENCES USERS(Username))");
            System.out.println("Created table: DOCTORS");
        }

        if (!tableExists("DOCTOR_SCHEDULE")) {
            stmt.execute("CREATE TABLE DOCTOR_SCHEDULE ("
                    + "ScheduleID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "DoctorID INT NOT NULL, "
                    + "ScheduleDate VARCHAR(20) NOT NULL, "
                    + "TimeSlot VARCHAR(10) NOT NULL, "
                    + "IsAvailable BOOLEAN DEFAULT TRUE, "
                    + "FOREIGN KEY (DoctorID) REFERENCES DOCTORS(DoctorID))");
            System.out.println("Created table: DOCTOR_SCHEDULE");
        }

        if (!tableExists("APPOINTMENTS")) {
            stmt.execute("CREATE TABLE APPOINTMENTS ("
                    + "AppointmentID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "Username VARCHAR(50) NOT NULL, "
                    + "DoctorID INT NOT NULL, "
                    + "ApptDate VARCHAR(20) NOT NULL, "
                    + "ApptTime VARCHAR(10) NOT NULL, "
                    + "Status VARCHAR(20) DEFAULT 'UPCOMING', "
                    + "FOREIGN KEY (Username) REFERENCES USERS(Username), "
                    + "FOREIGN KEY (DoctorID) REFERENCES DOCTORS(DoctorID))");
            System.out.println("Created table: APPOINTMENTS");
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
    
    private static void seedTestData() throws Exception {
        Connection conn = DerbyConnection.getConnection();

        // Add test patient
        if (!userExists(conn, "patient1")) {
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)");
                 PreparedStatement insertPatient = conn.prepareStatement(
                    "INSERT INTO PATIENTS (Username, ContactNumber, Address) VALUES (?, ?, ?)")) {
            insert.setString(1, "patient1");
            insert.setString(2, hashPassword("patient123"));
            insert.setString(3, "PATIENT");
            insert.executeUpdate();

            insertPatient.setString(1, "patient1");
            insertPatient.setString(2, "0123456789");
            insertPatient.setString(3, "Kuala Lumpur");
            insertPatient.executeUpdate();
            }
            System.out.println("Seeded test patient: patient1 / patient123");
        }

        // Add test doctor
        if (!userExists(conn, "doctor1")) {
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)");
                 PreparedStatement insertDoctor = conn.prepareStatement(
                    "INSERT INTO DOCTORS (Username, DoctorName, Specialization) VALUES (?, ?, ?)");
                 PreparedStatement insertSlot = conn.prepareStatement(
                    "INSERT INTO DOCTOR_SCHEDULE (DoctorID, ScheduleDate, TimeSlot, IsAvailable) "
                    + "VALUES (1, ?, ?, true)")) {
            insert.setString(1, "doctor1");
            insert.setString(2, hashPassword("doctor123"));
            insert.setString(3, "DOCTOR");
            insert.executeUpdate();

            insertDoctor.setString(1, "doctor1");
            insertDoctor.setString(2, "Dr. Ahmad");
            insertDoctor.setString(3, "General Practitioner");
            insertDoctor.executeUpdate();
            System.out.println("Seeded test doctor: Dr. Ahmad");

            // Add available time slots for doctor1 (DoctorID = 1)
            String[] slots = {"09:00", "10:00", "11:00", "14:00", "15:00"};
            for (String slot : slots) {
                insertSlot.setString(1, "2026-02-10");
                insertSlot.setString(2, slot);
                insertSlot.executeUpdate();
            }
            }
            System.out.println("Seeded doctor schedule slots.");
        }
        
        // Seed doctor schedule slots if empty
        try (PreparedStatement checkSlots = conn.prepareStatement(
                "SELECT COUNT(*) FROM DOCTOR_SCHEDULE");
             ResultSet slotsRs = checkSlots.executeQuery()) {
            slotsRs.next();
            if (slotsRs.getInt(1) == 0) {
                try (PreparedStatement insertSlot = conn.prepareStatement(
                    "INSERT INTO DOCTOR_SCHEDULE (DoctorID, ScheduleDate, TimeSlot, IsAvailable) "
                    + "VALUES (1, ?, ?, true)")) {
                    String[] slots2 = {"09:00", "10:00", "11:00", "14:00", "15:00"};
                    for (String slot : slots2) {
                        insertSlot.setString(1, "2026-02-10");
                        insertSlot.setString(2, slot);
                        insertSlot.executeUpdate();
                    }
                }
                System.out.println("Re-seeded doctor schedule slots.");
            }
        }
    }

    private static boolean userExists(Connection conn, String username) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM USERS WHERE Username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
    
    
    private static void insertSlotsDirect() throws Exception {
    Connection conn = DerbyConnection.getConnection();
    
    // Get the actual DoctorID from DOCTORS table
    int actualDoctorId;
    try (PreparedStatement getDoctorId = conn.prepareStatement(
            "SELECT DoctorID FROM DOCTORS WHERE Username = 'doctor1'");
         ResultSet drRs = getDoctorId.executeQuery()) {
        if (!drRs.next()) {
            System.out.println("Doctor1 not found in DOCTORS table - skipping slot insert");
            return;
        }
        actualDoctorId = drRs.getInt("DoctorID");
    }
    System.out.println("Found doctor1 with DoctorID = " + actualDoctorId);
    
    // Check if slots already exist for this doctor
    int count;
    try (PreparedStatement check = conn.prepareStatement(
            "SELECT COUNT(*) FROM DOCTOR_SCHEDULE WHERE DoctorID = ? AND ScheduleDate = '2026-02-10'")) {
        check.setInt(1, actualDoctorId);
        try (ResultSet rs = check.executeQuery()) {
            rs.next();
            count = rs.getInt(1);
        }
    }
    
    if (count == 0) {
        String[] slots = {"09:00", "10:00", "11:00", "14:00", "15:00"};
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO DOCTOR_SCHEDULE (DoctorID, ScheduleDate, TimeSlot, IsAvailable) "
                + "VALUES (?, '2026-02-10', ?, true)")) {
            for (String slot : slots) {
                ps.setInt(1, actualDoctorId);
                ps.setString(2, slot);
                ps.executeUpdate();
            }
        }
        System.out.println("Slots inserted for DoctorID = " + actualDoctorId);
    } else {
        System.out.println("Slots already exist for DoctorID=" + actualDoctorId + " count=" + count);
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
