package brigthcare_medical_centre.doctor;

import brigthcare_medical_centre.database.DatabaseSetup;
import brigthcare_medical_centre.database.DerbyConnection;
import brigthcare_medical_centre.database.DoctorDB;
import brigthcare_medical_centre.database.PatientDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class DoctorTransactionalACIDTest {

    private static DoctorDB doctorDB;
    private static PatientDB patientDB;
    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    @BeforeClass
    public static void setUpClass() {
        DatabaseSetup.initialize();
        doctorDB = new DoctorDB();
        patientDB = new PatientDB();
    }

    @AfterClass
    public static void tearDownClass() {
        POOL.shutdownNow();
    }

    // =================================================================
    //  ATOMICITY
    // =================================================================

    @Test
    public void testAtomicity_RollbackDiscardsAllStatements() throws Exception {
        String user = "acid_rb_" + System.currentTimeMillis();
        String date = futureDate(7);
        try (Connection c = DerbyConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                insertUser(c, user, "DOCTOR");
                insertDoctor(c, user, "Dr Rollback");
                insertSchedule(c, user, date, "09:00", true);
                c.rollback();
            } finally {
                c.setAutoCommit(true);
            }
        }
        assertEquals(0, queryInt("SELECT COUNT(*) FROM USERS WHERE Username = ?", user));
        assertEquals(0, queryInt("SELECT COUNT(*) FROM DOCTORS WHERE Username = ?", user));
        assertEquals(0, queryInt(
                "SELECT COUNT(*) FROM DOCTOR_SCHEDULE s "
                + "JOIN DOCTORS d ON s.DoctorID = d.DoctorID "
                + "WHERE d.Username = ?", user));
    }

    @Test
    public void testAtomicity_CommitPersistsAllStatements() throws Exception {
        String user = "acid_cm_" + System.currentTimeMillis();
        String date = futureDate(8);
        try (Connection c = DerbyConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                insertUser(c, user, "DOCTOR");
                insertDoctor(c, user, "Dr Commit");
                insertSchedule(c, user, date, "09:00", true);
                c.commit();
            } finally {
                c.setAutoCommit(true);
            }
        }
        assertEquals(1, queryInt("SELECT COUNT(*) FROM USERS WHERE Username = ?", user));
        assertEquals(1, queryInt("SELECT COUNT(*) FROM DOCTORS WHERE Username = ?", user));
        assertEquals(1, queryInt(
                "SELECT COUNT(*) FROM DOCTOR_SCHEDULE s "
                + "JOIN DOCTORS d ON s.DoctorID = d.DoctorID "
                + "WHERE d.Username = ?", user));
        cleanupUser(user);
    }

    @Test
    public void testAtomicity_ConstraintFailureRollsBackWholeTransaction() throws Exception {
        String user = "acid_fc_" + System.currentTimeMillis();
        try (Connection c = DerbyConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                insertUser(c, user, "DOCTOR");
                boolean threw = false;
                try {
                    insertRaw(c,
                            "INSERT INTO APPOINTMENTS "
                            + "(Username, DoctorID, ApptDate, ApptTime, Status) "
                            + "VALUES ('patient1', 999999, ?, '09:00', 'PENDING')",
                            futureDate(9));
                } catch (SQLException expected) {
                    threw = true;
                }
                assertTrue("FK-violating statement must throw", threw);
                c.rollback();
            } finally {
                c.setAutoCommit(true);
            }
        }
        assertEquals(0, queryInt("SELECT COUNT(*) FROM USERS WHERE Username = ?", user));
    }

    @Test
    public void testAtomicity_FailedRescheduleLeavesNoPartialState() throws Exception {
        String date = futureDate(10);
        cleanupDate(1, date);
        String patient2 = "acid_p2_" + System.currentTimeMillis();
        insertUser(patient2, "PATIENT");
        insertPatient(patient2);
        try {
            assertTrue(doctorDB.updateDoctorSchedule(1, date, "09:00", true));
            assertTrue(doctorDB.updateDoctorSchedule(1, date, "10:00", true));
            assertTrue(patientDB.bookAppointment("patient1", 1, date, "09:00"));
            assertTrue(patientDB.bookAppointment(patient2, 1, date, "10:00"));

            int appt = findAppointmentId("patient1", 1, date, "09:00");
            assertFalse("reschedule to occupied slot must fail",
                    doctorDB.rescheduleAppointment(appt, date, "10:00"));
            assertEquals("09:00", getAppointmentTime(appt));
            assertEquals("PENDING", getAppointmentStatus(appt));
            assertFalse("old slot stays reserved", isSlotAvailable(1, date, "09:00"));
            assertFalse("target slot stays occupied", isSlotAvailable(1, date, "10:00"));
        } finally {
            cleanupUser(patient2);
        }
    }

    @Test
    public void testAtomicity_RejectCommitsStatusAndSlotReleaseTogether() throws Exception {
        String date = futureDate(11);
        cleanupDate(1, date);
        assertTrue(doctorDB.updateDoctorSchedule(1, date, "13:00", true));
        assertTrue(patientDB.bookAppointment("patient1", 1, date, "13:00"));
        int id = findAppointmentId("patient1", 1, date, "13:00");
        assertFalse("slot reserved while pending", isSlotAvailable(1, date, "13:00"));

        assertTrue(doctorDB.rejectAppointment(id));
        assertEquals("REJECTED", getAppointmentStatus(id));
        assertTrue("slot released after reject", isSlotAvailable(1, date, "13:00"));
    }

    // =================================================================
    //  CONSISTENCY
    // =================================================================

    @Test
    public void testConsistency_FK_Appointments_NonExistentDoctor() throws Exception {
        try {
            try (Connection c = DerbyConnection.getConnection()) {
                insertRaw(c,
                        "INSERT INTO APPOINTMENTS "
                        + "(Username, DoctorID, ApptDate, ApptTime, Status) "
                        + "VALUES ('patient1', 999999, ?, '09:00', 'PENDING')",
                        futureDate(12));
            }
            fail("expected FK constraint violation");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void testConsistency_FK_Appointments_NonExistentPatient() throws Exception {
        try {
            try (Connection c = DerbyConnection.getConnection()) {
                insertRaw(c,
                        "INSERT INTO APPOINTMENTS "
                        + "(Username, DoctorID, ApptDate, ApptTime, Status) "
                        + "VALUES ('ghost_patient', 1, ?, '09:00', 'PENDING')",
                        futureDate(12));
            }
            fail("expected FK constraint violation");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void testConsistency_FK_DoctorProfile_NoUserRow() throws Exception {
        try {
            try (Connection c = DerbyConnection.getConnection()) {
                insertRaw(c,
                        "INSERT INTO DOCTORS (Username, DoctorName, Specialization) "
                        + "VALUES ('ghost_doctor', 'Dr Ghost', 'X')");
            }
            fail("expected FK constraint violation");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void testConsistency_FK_Schedule_NoDoctorRow() throws Exception {
        try {
            try (Connection c = DerbyConnection.getConnection()) {
                insertRaw(c,
                        "INSERT INTO DOCTOR_SCHEDULE "
                        + "(DoctorID, ScheduleDate, TimeSlot, IsAvailable) "
                        + "VALUES (999999, ?, '09:00', true)",
                        futureDate(13));
            }
            fail("expected FK constraint violation");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void testConsistency_FK_ConsultationNotes_NoAppointment() throws Exception {
        try {
            try (Connection c = DerbyConnection.getConnection()) {
                insertRaw(c,
                        "INSERT INTO CONSULTATION_NOTES "
                        + "(AppointmentID, DoctorID, PatientUsername, ConsultationDate, Diagnosis) "
                        + "VALUES (999999, 1, 'patient1', ?, 'D')",
                        futureDate(13));
            }
            fail("expected FK constraint violation");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void testConsistency_Unique_Users() throws Exception {
        String user = "acid_du_" + System.currentTimeMillis();
        insertUser(user, "DOCTOR");
        try {
            insertUser(user, "DOCTOR");
            fail("expected unique constraint violation");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        } finally {
            cleanupUser(user);
        }
    }

    @Test
    public void testConsistency_Unique_Doctors() throws Exception {
        String user = "acid_dd_" + System.currentTimeMillis();
        insertUser(user, "DOCTOR");
        insertDoctor(user, "Dr First");
        try {
            insertDoctor(user, "Dr Second");
            fail("expected unique constraint violation");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        } finally {
            cleanupUser(user);
        }
    }

    @Test
    public void testConsistency_Unique_Patients() throws Exception {
        String user = "acid_dp_" + System.currentTimeMillis();
        insertUser(user, "PATIENT");
        insertPatient(user);
        try {
            insertPatient(user);
            fail("expected unique constraint violation");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        } finally {
            cleanupUser(user);
        }
    }

    @Test
    public void testConsistency_DefaultStatusIsPending() throws Exception {
        String date = futureDate(14);
        cleanupDate(1, date);
        assertTrue(doctorDB.updateDoctorSchedule(1, date, "09:00", true));
        assertTrue(patientDB.bookAppointment("patient1", 1, date, "09:00"));
        int id = findAppointmentId("patient1", 1, date, "09:00");
        assertEquals("PENDING", getAppointmentStatus(id));
    }

    @Test
    public void testConsistency_StatusMachine() throws Exception {
        String date = futureDate(15);
        cleanupDate(1, date);
        assertTrue(doctorDB.updateDoctorSchedule(1, date, "10:00", true));
        assertTrue(patientDB.bookAppointment("patient1", 1, date, "10:00"));
        int id = findAppointmentId("patient1", 1, date, "10:00");

        assertTrue(doctorDB.acceptAppointment(id));
        assertFalse("cannot accept twice", doctorDB.acceptAppointment(id));
        assertFalse("cannot reject accepted", doctorDB.rejectAppointment(id));
        assertEquals("ACCEPTED", getAppointmentStatus(id));

        assertTrue(doctorDB.cancelAppointmentByDoctor(id));
        assertEquals("CANCELLED", getAppointmentStatus(id));
        assertFalse("cannot cancel again", doctorDB.cancelAppointmentByDoctor(id));
    }

    @Test
    public void testConsistency_SlotAndStatusStayInSync() throws Exception {
        String date = futureDate(16);
        cleanupDate(1, date);
        assertTrue(doctorDB.updateDoctorSchedule(1, date, "11:00", true));
        assertTrue(patientDB.bookAppointment("patient1", 1, date, "11:00"));
        assertFalse("slot reserved when booked", isSlotAvailable(1, date, "11:00"));

        int id = findAppointmentId("patient1", 1, date, "11:00");
        assertTrue(doctorDB.acceptAppointment(id));
        assertFalse("slot stays reserved when accepted", isSlotAvailable(1, date, "11:00"));

        assertTrue(doctorDB.cancelAppointmentByDoctor(id));
        assertTrue("slot released when cancelled", isSlotAvailable(1, date, "11:00"));
    }

    // =================================================================
    //  ISOLATION
    // =================================================================

    @Test
    public void testIsolation_DirtyReadPrevented() throws Exception {
        String date = futureDate(17);
        cleanupDate(1, date);
        assertTrue(doctorDB.updateDoctorSchedule(1, date, "09:00", true));

        CountDownLatch updated = new CountDownLatch(1);
        CountDownLatch releaseCommit = new CountDownLatch(1);
        Future<?> writer = POOL.submit(() -> {
            try (Connection c = DerbyConnection.getConnection()) {
                c.setAutoCommit(false);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE DOCTOR_SCHEDULE SET IsAvailable = false "
                        + "WHERE DoctorID = 1 AND ScheduleDate = ? AND TimeSlot = '09:00'")) {
                    ps.setString(1, date);
                    ps.executeUpdate();
                }
                updated.countDown();
                releaseCommit.await(15, TimeUnit.SECONDS);
                c.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        updated.await(15, TimeUnit.SECONDS);

        CountDownLatch readDone = new CountDownLatch(1);
        AtomicBoolean readerSawValue = new AtomicBoolean(true);
        AtomicBoolean readerCompleted = new AtomicBoolean(false);
        POOL.submit(() -> {
            try {
                readerSawValue.set(isSlotAvailable(1, date, "09:00"));
            } catch (Exception ignored) {
            } finally {
                readerCompleted.set(true);
                readDone.countDown();
            }
        });

        Thread.sleep(1000);
        assertFalse("reader must be blocked while writer holds uncommitted lock",
                readerCompleted.get());

        releaseCommit.countDown();
        writer.get(15, TimeUnit.SECONDS);
        readDone.await(15, TimeUnit.SECONDS);

        assertTrue("reader completes after commit", readerCompleted.get());
        assertFalse("reader sees committed false", readerSawValue.get());
        doctorDB.updateDoctorSchedule(1, date, "09:00", true);
    }

    @Test
    public void testIsolation_ConcurrentBookingExactlyOneWins() throws Exception {
        String date = futureDate(18);
        cleanupDate(1, date);
        assertTrue(doctorDB.updateDoctorSchedule(1, date, "14:00", true));

        CyclicBarrier gate = new CyclicBarrier(2);
        CountDownLatch done = new CountDownLatch(2);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        Runnable booker = () -> {
            try {
                gate.await(15, TimeUnit.SECONDS);
                if (patientDB.bookAppointment("patient1", 1, date, "14:00")) {
                    succeeded.incrementAndGet();
                } else {
                    failed.incrementAndGet();
                }
            } catch (Exception e) {
                failed.incrementAndGet();
            } finally {
                done.countDown();
            }
        };

        POOL.submit(booker);
        POOL.submit(booker);
        done.await(60, TimeUnit.SECONDS);

        assertEquals("exactly one booking must succeed", 1, succeeded.get());
        assertEquals("exactly one booking must fail", 1, failed.get());
        assertEquals("no double-booking", 1,
                queryInt("SELECT COUNT(*) FROM APPOINTMENTS "
                        + "WHERE DoctorID = 1 AND ApptDate = ? "
                        + "AND ApptTime = '14:00' AND Status IN ('PENDING','ACCEPTED')",
                        date));
        assertFalse("slot must be reserved", isSlotAvailable(1, date, "14:00"));
    }

    @Test
    public void testIsolation_ConcurrentAcceptExactlyOneWins() throws Exception {
        String date = futureDate(19);
        cleanupDate(1, date);
        assertTrue(doctorDB.updateDoctorSchedule(1, date, "10:00", true));
        assertTrue(patientDB.bookAppointment("patient1", 1, date, "10:00"));
        int id = findAppointmentId("patient1", 1, date, "10:00");

        CyclicBarrier gate = new CyclicBarrier(2);
        CountDownLatch done = new CountDownLatch(2);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();

        Runnable accepter = () -> {
            try {
                gate.await(15, TimeUnit.SECONDS);
                if (doctorDB.acceptAppointment(id)) {
                    accepted.incrementAndGet();
                } else {
                    skipped.incrementAndGet();
                }
            } catch (Exception e) {
                skipped.incrementAndGet();
            } finally {
                done.countDown();
            }
        };

        POOL.submit(accepter);
        POOL.submit(accepter);
        done.await(60, TimeUnit.SECONDS);

        assertEquals("exactly one accept must succeed", 1, accepted.get());
        assertEquals("exactly one accept must fail", 1, skipped.get());
        assertEquals("ACCEPTED", getAppointmentStatus(id));
    }

    // =================================================================
    //  DURABILITY
    // =================================================================

    @Test
    public void testDurability_CommittedDataSurvivesConnectionClose() throws Exception {
        String user = "acid_dc_" + System.currentTimeMillis();
        String date = futureDate(20);
        try (Connection c = DerbyConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                insertUser(c, user, "DOCTOR");
                insertDoctor(c, user, "Dr Durable");
                insertSchedule(c, user, date, "09:00", true);
                c.commit();
            } finally {
                c.setAutoCommit(true);
            }
        }
        assertEquals(1, queryInt("SELECT COUNT(*) FROM USERS WHERE Username = ?", user));
        assertEquals(1, queryInt("SELECT COUNT(*) FROM DOCTORS WHERE Username = ?", user));
        assertEquals(1, queryInt(
                "SELECT COUNT(*) FROM DOCTOR_SCHEDULE s "
                + "JOIN DOCTORS d ON s.DoctorID = d.DoctorID "
                + "WHERE d.Username = ?", user));
        cleanupUser(user);
    }

    @Test
    public void testDurability_CommittedDataSurvivesDbShutdownAndRestart() throws Exception {
        String user = "acid_ds_" + System.currentTimeMillis();
        String date = futureDate(21);
        insertUser(user, "DOCTOR");
        insertDoctor(user, "Dr Restart");
        doctorDB.updateDoctorSchedule(getDoctorId(user), date, "09:00", true);

        assertEquals(1, queryInt("SELECT COUNT(*) FROM USERS WHERE Username = ?", user));

        shutdownEmbeddedDb();

        assertEquals(1, queryInt("SELECT COUNT(*) FROM USERS WHERE Username = ?", user));
        assertEquals(1, queryInt("SELECT COUNT(*) FROM DOCTORS WHERE Username = ?", user));
        assertEquals(1, queryInt(
                "SELECT COUNT(*) FROM DOCTOR_SCHEDULE s "
                + "JOIN DOCTORS d ON s.DoctorID = d.DoctorID "
                + "WHERE d.Username = ?", user));
        cleanupUser(user);
    }

    // =================================================================
    //  Helpers
    // =================================================================

    private static String futureDate(int offsetDays) {
        return LocalDate.now().plusDays(offsetDays).toString();
    }

    private static int queryInt(String sql, Object... params) throws SQLException {
        try (Connection c = DerbyConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static String queryString(String sql, Object... params) throws SQLException {
        try (Connection c = DerbyConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static boolean isSlotAvailable(int doctorId, String date, String time)
            throws SQLException {
        try (Connection c = DerbyConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT IsAvailable FROM DOCTOR_SCHEDULE "
                     + "WHERE DoctorID = ? AND ScheduleDate = ? AND TimeSlot = ?")) {
            ps.setInt(1, doctorId);
            ps.setString(2, date);
            ps.setString(3, time);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private static int findAppointmentId(String username, int doctorId,
            String date, String time) throws SQLException {
        try (Connection c = DerbyConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT AppointmentID FROM APPOINTMENTS "
                     + "WHERE Username = ? AND DoctorID = ? "
                     + "AND ApptDate = ? AND ApptTime = ? "
                     + "ORDER BY AppointmentID DESC")) {
            ps.setString(1, username);
            ps.setInt(2, doctorId);
            ps.setString(3, date);
            ps.setString(4, time);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new AssertionError("No appointment for " + username
                + " at " + date + " " + time);
    }

    private static String getAppointmentStatus(int appointmentId) throws SQLException {
        String s = queryString(
                "SELECT Status FROM APPOINTMENTS WHERE AppointmentID = ?",
                appointmentId);
        if (s == null) {
            throw new AssertionError("No appointment " + appointmentId);
        }
        return s;
    }

    private static String getAppointmentTime(int appointmentId) throws SQLException {
        String t = queryString(
                "SELECT ApptTime FROM APPOINTMENTS WHERE AppointmentID = ?",
                appointmentId);
        if (t == null) {
            throw new AssertionError("No appointment " + appointmentId);
        }
        return t;
    }

    private static int getDoctorId(String username) throws SQLException {
        Integer id = queryIntNullable(
                "SELECT DoctorID FROM DOCTORS WHERE Username = ?", username);
        if (id == null) {
            throw new AssertionError("No doctor for " + username);
        }
        return id;
    }

    private static Integer queryIntNullable(String sql, Object... params)
            throws SQLException {
        try (Connection c = DerbyConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private static void bind(PreparedStatement ps, Object... params)
            throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private static void insertUser(Connection c, String username, String role)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO USERS (Username, PasswordHash, Role) "
                + "VALUES (?, 'test-hash', ?)")) {
            ps.setString(1, username);
            ps.setString(2, role);
            ps.executeUpdate();
        }
    }

    private static void insertUser(String username, String role) throws SQLException {
        try (Connection c = DerbyConnection.getConnection()) {
            insertUser(c, username, role);
        }
    }

    private static void insertPatient(String username) throws SQLException {
        try (Connection c = DerbyConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO PATIENTS (Username, ContactNumber, Address) "
                     + "VALUES (?, '0000000000', 'Test')")) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    private static void insertDoctor(Connection c, String username, String name)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO DOCTORS (Username, DoctorName, Specialization) "
                + "VALUES (?, ?, 'General Practice')")) {
            ps.setString(1, username);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    private static void insertDoctor(String username, String name) throws SQLException {
        try (Connection c = DerbyConnection.getConnection()) {
            insertDoctor(c, username, name);
        }
    }

    private static void insertSchedule(Connection c, String username,
            String date, String time, boolean available) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO DOCTOR_SCHEDULE "
                + "(DoctorID, ScheduleDate, TimeSlot, IsAvailable) "
                + "VALUES ((SELECT DoctorID FROM DOCTORS WHERE Username = ?), "
                + "?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, date);
            ps.setString(3, time);
            ps.setBoolean(4, available);
            ps.executeUpdate();
        }
    }

    private static void insertRaw(Connection c, String sql, Object... params)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            ps.executeUpdate();
        }
    }

    private static void cleanupUser(String username) throws SQLException {
        try (Connection c = DerbyConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM APPOINTMENTS WHERE Username = ? "
                    + "OR DoctorID IN (SELECT DoctorID FROM DOCTORS WHERE Username = ?)")) {
                ps.setString(1, username);
                ps.setString(2, username);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM CONSULTATION_NOTES WHERE PatientUsername = ? "
                    + "OR DoctorID IN (SELECT DoctorID FROM DOCTORS WHERE Username = ?)")) {
                ps.setString(1, username);
                ps.setString(2, username);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM DOCTOR_SCHEDULE WHERE DoctorID IN "
                    + "(SELECT DoctorID FROM DOCTORS WHERE Username = ?)")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM DOCTORS WHERE Username = ?")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM PATIENTS WHERE Username = ?")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM USERS WHERE Username = ?")) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
        }
    }

    private static void shutdownEmbeddedDb() throws SQLException {
        try {
            java.sql.DriverManager.getConnection(
                    "jdbc:derby:BrightCareDB;shutdown=true");
        } catch (SQLException expected) {
        }
    }

    private static void cleanupDate(int doctorId, String date) throws SQLException {
        try (Connection c = DerbyConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM CONSULTATION_NOTES WHERE AppointmentID IN "
                    + "(SELECT AppointmentID FROM APPOINTMENTS "
                    + "WHERE DoctorID = ? AND ApptDate = ?)")) {
                ps.setInt(1, doctorId);
                ps.setString(2, date);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM APPOINTMENTS WHERE DoctorID = ? AND ApptDate = ?")) {
                ps.setInt(1, doctorId);
                ps.setString(2, date);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM DOCTOR_SCHEDULE WHERE DoctorID = ? AND ScheduleDate = ?")) {
                ps.setInt(1, doctorId);
                ps.setString(2, date);
                ps.executeUpdate();
            }
        }
    }
}
