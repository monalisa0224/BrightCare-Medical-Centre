package brigthcare_medical_centre.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;

/**
 * Demonstrates the four ACID guarantees of the Derby database layer used by
 * the application: Atomicity, Consistency, Isolation and Durability.
 *
 * The test uses its own throwaway database (BrightCareDB_TestInt_ACID_*) so
 * the real clinic data is never touched, and the embedded engine is restarted
 * inside the Durability test to prove committed data survives a restart.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DbAcidTest {

    private static String dbBaseUrl;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        dbBaseUrl = "jdbc:derby:BrightCareDB_TestInt_ACID_" + System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(dbBaseUrl + ";create=true")) {
            createSchema(conn);
            seedBaseRows(conn);
            setLockWaitTimeout(conn);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try (Connection conn = openConn()) {
            conn.setAutoCommit(true);
        }
    }

    @Test
    public void test1Atomicity() throws Exception {
        int usersBefore = count("USERS");
        int doctorsBefore = count("DOCTORS");

        try (Connection conn = openConn()) {
            conn.setAutoCommit(false);
            try (PreparedStatement insertUser = conn.prepareStatement(
                    "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)")) {
                insertUser.setString(1, "doc_atomic_" + System.currentTimeMillis());
                insertUser.setString(2, DatabaseSetup.hashPassword("pass123"));
                insertUser.setString(3, "DOCTOR");
                insertUser.executeUpdate();
            }

            try (PreparedStatement bad = conn.prepareStatement(
                    "INSERT INTO APPOINTMENTS (Username, DoctorID, ApptDate) VALUES (?, ?, ?)")) {
                bad.setString(1, "no_such_patient");
                bad.setInt(2, 1);
                bad.setString(3, "2026-08-01");
                bad.executeUpdate();
                fail("Expected a foreign-key violation to abort the transaction");
            } catch (SQLException expected) {
            }
            conn.rollback();
        }

        assertEquals("Atomicity: the rolled-back user insert must not persist",
                usersBefore, count("USERS"));
        assertEquals("Atomicity: no partial doctor profile must persist",
                doctorsBefore, count("DOCTORS"));
    }

    @Test
    public void test2Consistency() throws Exception {
        int appointmentsBefore = count("APPOINTMENTS");
        try (Connection conn = openConn()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO APPOINTMENTS (Username, DoctorID, ApptDate) VALUES (?, ?, ?)")) {
                ps.setString(1, "ghost_patient");
                ps.setInt(2, 1);
                ps.setString(3, "2026-08-02");
                ps.executeUpdate();
                fail("Expected a referential-integrity violation for an orphan appointment");
            } catch (SQLException expected) {
            }
        }
        assertEquals("Consistency: an orphan appointment must be rejected",
                appointmentsBefore, count("APPOINTMENTS"));

        int usersBefore = count("USERS");
        try (Connection conn = openConn()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)")) {
                ps.setString(1, "admin");
                ps.setString(2, DatabaseSetup.hashPassword("x"));
                ps.setString(3, "ADMIN");
                ps.executeUpdate();
                fail("Expected a unique-constraint violation for a duplicate username");
            } catch (SQLException expected) {
            }
        }

        try (Connection conn = openConn()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)")) {
                ps.setString(1, "no_role_" + System.currentTimeMillis());
                ps.setString(2, DatabaseSetup.hashPassword("x"));
                ps.setString(3, null);
                ps.executeUpdate();
                fail("Expected a NOT NULL violation for a user without a role");
            } catch (SQLException expected) {
            }
        }
        assertEquals("Consistency: failed inserts must leave the table unchanged",
                usersBefore, count("USERS"));
    }

    @Test
    public void test3Isolation() throws Exception {
        String username = "iso_user_" + System.currentTimeMillis();
        boolean sawDirtyData = false;

        try (Connection tx = openConn()) {
            tx.setAutoCommit(false);
            try (PreparedStatement ps = tx.prepareStatement(
                    "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)")) {
                ps.setString(1, username);
                ps.setString(2, DatabaseSetup.hashPassword("iso123"));
                ps.setString(3, "PATIENT");
                ps.executeUpdate();
            }

            try (Connection reader = openConn();
                 PreparedStatement ps = reader.prepareStatement(
                         "SELECT COUNT(*) FROM USERS WHERE Username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) != 0) {
                        sawDirtyData = true;
                    }
                }
            } catch (SQLException lockWait) {
            }

            tx.commit();
        }

        assertFalse("Isolation: a concurrent reader must never see uncommitted data", sawDirtyData);

        try (Connection reader = openConn();
             PreparedStatement ps = reader.prepareStatement(
                     "SELECT COUNT(*) FROM USERS WHERE Username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                assertEquals("Isolation: committed data must become visible to new readers",
                        1, rs.getInt(1));
            }
        }
    }

    @Test
    public void test4Durability() throws Exception {
        String username = "durable_user_" + System.currentTimeMillis();
        try (Connection conn = openConn()) {
            conn.setAutoCommit(true);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)")) {
                ps.setString(1, username);
                ps.setString(2, DatabaseSetup.hashPassword("dur123"));
                ps.setString(3, "PATIENT");
                ps.executeUpdate();
            }
        }

        shutdownDatabase();

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM USERS WHERE Username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                assertEquals("Durability: committed data must survive an engine restart",
                        1, rs.getInt(1));
            }
        }
    }

    private static Connection openConn() throws SQLException {
        return DriverManager.getConnection(dbBaseUrl);
    }

    private static int count(String table) throws SQLException {
        try (Connection conn = openConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void setLockWaitTimeout(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.locks.waitTimeout', '2')")) {
            ps.execute();
        }
    }

    private static void shutdownDatabase() {
        try {
            DriverManager.getConnection(dbBaseUrl + ";shutdown=true");
            fail("Expected an exception from the embedded database shutdown");
        } catch (SQLException expected) {
        }
    }

    private static void createSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE USERS ("
                    + "UserID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "Username VARCHAR(50) UNIQUE NOT NULL, "
                    + "PasswordHash VARCHAR(256) NOT NULL, "
                    + "Role VARCHAR(20) NOT NULL, "
                    + "CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            st.execute("CREATE TABLE LOGS ("
                    + "LogID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "UserID INT, "
                    + "Action VARCHAR(100) NOT NULL, "
                    + "Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "Details VARCHAR(500), "
                    + "FOREIGN KEY (UserID) REFERENCES USERS(UserID))");
            st.execute("CREATE TABLE REPORTS ("
                    + "ReportID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "AdminID INT NOT NULL, "
                    + "ReportType VARCHAR(50) NOT NULL, "
                    + "GeneratedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "Parameters VARCHAR(200), "
                    + "ResultData CLOB, "
                    + "FOREIGN KEY (AdminID) REFERENCES USERS(UserID))");
            st.execute("CREATE TABLE PATIENTS ("
                    + "PatientID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "Username VARCHAR(50) UNIQUE NOT NULL, "
                    + "ContactNumber VARCHAR(20), "
                    + "Address VARCHAR(200), "
                    + "FOREIGN KEY (Username) REFERENCES USERS(Username))");
            st.execute("CREATE TABLE DOCTORS ("
                    + "DoctorID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "Username VARCHAR(50) UNIQUE NOT NULL, "
                    + "DoctorName VARCHAR(100) NOT NULL, "
                    + "Specialization VARCHAR(100), "
                    + "ContactNumber VARCHAR(20), "
                    + "FOREIGN KEY (Username) REFERENCES USERS(Username))");
            st.execute("CREATE TABLE RECEPTIONISTS ("
                    + "ReceptionistID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "Username VARCHAR(50) UNIQUE NOT NULL, "
                    + "FullName VARCHAR(100) NOT NULL, "
                    + "ContactNumber VARCHAR(20), "
                    + "FOREIGN KEY (Username) REFERENCES USERS(Username))");
            st.execute("CREATE TABLE DOCTOR_SCHEDULE ("
                    + "ScheduleID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "DoctorID INT NOT NULL, "
                    + "ScheduleDate VARCHAR(20) NOT NULL, "
                    + "TimeSlot VARCHAR(10) NOT NULL, "
                    + "IsAvailable BOOLEAN DEFAULT TRUE, "
                    + "FOREIGN KEY (DoctorID) REFERENCES DOCTORS(DoctorID))");
            st.execute("CREATE TABLE APPOINTMENTS ("
                    + "AppointmentID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "Username VARCHAR(50) NOT NULL, "
                    + "DoctorID INT NOT NULL, "
                    + "ApptDate VARCHAR(20) NOT NULL, "
                    + "ApptTime VARCHAR(10) NOT NULL, "
                    + "Status VARCHAR(20) DEFAULT 'PENDING', "
                    + "FOREIGN KEY (Username) REFERENCES USERS(Username), "
                    + "FOREIGN KEY (DoctorID) REFERENCES DOCTORS(DoctorID))");
            st.execute("CREATE TABLE CONSULTATION_NOTES ("
                    + "NoteID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "AppointmentID INT NOT NULL, "
                    + "DoctorID INT NOT NULL, "
                    + "PatientUsername VARCHAR(50) NOT NULL, "
                    + "ConsultationDate VARCHAR(20) NOT NULL, "
                    + "Diagnosis VARCHAR(500), "
                    + "Treatment VARCHAR(500), "
                    + "Prescription VARCHAR(500), "
                    + "Notes VARCHAR(1000), "
                    + "CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (AppointmentID) REFERENCES APPOINTMENTS(AppointmentID), "
                    + "FOREIGN KEY (DoctorID) REFERENCES DOCTORS(DoctorID), "
                    + "FOREIGN KEY (PatientUsername) REFERENCES USERS(Username))");
        }
    }

    private static void seedBaseRows(Connection conn) throws SQLException {
        try (PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)")) {
            insert.setString(1, "admin");
            insert.setString(2, DatabaseSetup.hashPassword("admin123"));
            insert.setString(3, "ADMIN");
            insert.executeUpdate();
            insert.setString(1, "doctor1");
            insert.setString(2, DatabaseSetup.hashPassword("doctor123"));
            insert.setString(3, "DOCTOR");
            insert.executeUpdate();
        }
        try (PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO DOCTORS (Username, DoctorName) VALUES (?, ?)")) {
            insert.setString(1, "doctor1");
            insert.setString(2, "Dr. Ahmad");
            insert.executeUpdate();
        }
    }
}
