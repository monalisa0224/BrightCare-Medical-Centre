package brigthcare_medical_centre.tests;

import brigthcare_medical_centre.admin.AdminImpl;
import brigthcare_medical_centre.auth.UserRole;
import brigthcare_medical_centre.database.DatabaseSetup;
import brigthcare_medical_centre.database.DerbyConnection;
import brigthcare_medical_centre.database.DoctorDB;
import brigthcare_medical_centre.database.PatientDB;
import brigthcare_medical_centre.report.ReportGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class RegressionSmokeTests {

    public static void main(String[] args) throws Exception {
        System.setProperty("brightcare.db.url",
                "jdbc:derby:BrightCareDB_Test_" + System.currentTimeMillis() + ";create=true");
        DatabaseSetup.initialize();

        run("booking integrity", RegressionSmokeTests::testBookingIntegrity);
        run("patient cancellation ownership", RegressionSmokeTests::testPatientCancellationOwnershipAndRestore);
        run("doctor reschedule and slot guardrails", RegressionSmokeTests::testDoctorRescheduleAndScheduleGuards);
        run("admin role provisioning", RegressionSmokeTests::testAdminProvisioningAndCleanup);
        run("report schema compatibility", RegressionSmokeTests::testReportQueriesAgainstActualSchema);

        System.out.println("All regression smoke tests passed.");
        System.exit(0);
    }

    private static void testBookingIntegrity() throws Exception {
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        String date = "2026-03-03";
        String time = "09:00";

        assertTrue(doctorDB.updateDoctorSchedule(1, date, time, true),
                "Expected the doctor slot to be created.");
        assertTrue(patientDB.bookAppointment("patient1", 1, date, time),
                "Expected the first booking to succeed.");
        assertFalse(patientDB.bookAppointment("patient1", 1, date, time),
                "Expected a duplicate booking for the same slot to fail.");
        assertFalse(patientDB.getDoctorAvailability(1, date).contains(time),
                "Expected the booked slot to disappear from availability.");
    }

    private static void testPatientCancellationOwnershipAndRestore() throws Exception {
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        String date = "2026-03-03";
        String time = "10:00";

        assertTrue(doctorDB.updateDoctorSchedule(1, date, time, true),
                "Expected a fresh slot for the cancellation test.");
        assertTrue(patientDB.bookAppointment("patient1", 1, date, time),
                "Expected the appointment to be booked before cancellation.");

        int appointmentId = findAppointmentId("patient1", 1, date, time);
        assertFalse(patientDB.cancelAppointment("intruder", appointmentId),
                "Expected ownership checks to block cancellation by another username.");
        assertTrue(patientDB.cancelAppointment("patient1", appointmentId),
                "Expected the owning patient to cancel the appointment.");
        assertEquals("CANCELLED", getAppointmentStatus(appointmentId),
                "Expected the appointment status to change to CANCELLED.");
        assertTrue(patientDB.getDoctorAvailability(1, date).contains(time),
                "Expected the slot to return to availability after cancellation.");
    }

    private static void testDoctorRescheduleAndScheduleGuards() throws Exception {
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        String date = "2026-03-03";
        String originalTime = "11:00";
        String newTime = "13:00";

        assertTrue(doctorDB.updateDoctorSchedule(1, date, originalTime, true),
                "Expected the original slot to be created.");
        assertTrue(doctorDB.updateDoctorSchedule(1, date, newTime, true),
                "Expected the new slot to be created.");
        assertTrue(patientDB.bookAppointment("patient1", 1, date, originalTime),
                "Expected the appointment to be booked before rescheduling.");

        int appointmentId = findAppointmentId("patient1", 1, date, originalTime);
        assertTrue(doctorDB.rescheduleAppointment(appointmentId, date, newTime),
                "Expected the doctor to reschedule the appointment.");
        assertEquals(newTime, getAppointmentTime(appointmentId),
                "Expected the appointment time to move to the new slot.");
        assertTrue(patientDB.getDoctorAvailability(1, date).contains(originalTime),
                "Expected the old slot to be released.");
        assertFalse(patientDB.getDoctorAvailability(1, date).contains(newTime),
                "Expected the new slot to remain reserved.");
        assertFalse(doctorDB.updateDoctorSchedule(1, date, newTime, true),
                "Expected add-slot to reject reopening an occupied slot.");
        assertFalse(doctorDB.updateDoctorSchedule(1, date, newTime, false),
                "Expected remove-slot to reject altering an occupied slot.");
    }

    private static void testAdminProvisioningAndCleanup() throws Exception {
        AdminImpl admin = new AdminImpl();
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        String suffix = String.valueOf(System.currentTimeMillis());
        String doctorUser = "doctor_admin_" + suffix;
        String patientUser = "patient_admin_" + suffix;

        assertTrue(admin.registerUser(doctorUser, "doctor123", UserRole.DOCTOR),
                "Expected admin doctor registration to succeed.");
        int doctorId = doctorDB.getDoctorIdByUsername(doctorUser);
        assertTrue(doctorId > 0, "Expected a DOCTORS row for the new doctor account.");

        int doctorUserId = findUserId(doctorUser);
        assertTrue(admin.updateUserRole(doctorUserId, UserRole.RECEPTIONIST),
                "Expected role reassignment for an unused doctor account to succeed.");
        assertEquals("RECEPTIONIST", getUserRole(doctorUser),
                "Expected the user role to change in USERS.");
        assertEquals(-1, doctorDB.getDoctorIdByUsername(doctorUser),
                "Expected the doctor profile row to be removed after the role change.");

        assertTrue(admin.registerUser(patientUser, "patient123", UserRole.PATIENT),
                "Expected admin patient registration to succeed.");
        assertNotNull(patientDB.getPatientProfile(patientUser),
                "Expected a PATIENTS row for the new patient account.");

        int patientUserId = findUserId(patientUser);
        assertTrue(admin.deleteUser(patientUserId),
                "Expected an unused patient account to be deletable.");
        assertNull(getUserRole(patientUser),
                "Expected the deleted user to disappear from USERS.");
        assertNull(patientDB.getPatientProfile(patientUser),
                "Expected the deleted patient profile to disappear from PATIENTS.");
    }

    private static void testReportQueriesAgainstActualSchema() throws Exception {
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        ReportGenerator generator = new ReportGenerator();
        String date = "2026-03-04";
        String time = "09:00";

        assertTrue(doctorDB.updateDoctorSchedule(1, date, time, true),
                "Expected a report test slot to be created.");
        assertTrue(patientDB.bookAppointment("patient1", 1, date, time),
                "Expected the report test booking to succeed.");

        int appointmentId = findAppointmentId("patient1", 1, date, time);
        assertTrue(doctorDB.acceptAppointment(appointmentId),
                "Expected the doctor to accept the report test appointment.");
        assertTrue(doctorDB.updateConsultationNotes(appointmentId, 1, "patient1",
                "Flu", "Rest", "Paracetamol", "Recovered"),
                "Expected consultation notes to complete the appointment.");

        List<String[]> monthly = generator.generateMonthlyAppointments("2026-03-01", "2026-03-31");
        assertContains(monthly, 0, String.valueOf(appointmentId),
                "Expected monthly appointments report to include the completed appointment.");

        List<String[]> consultations = generator.generateDoctorConsultations("2026-03-01", "2026-03-31");
        assertContains(consultations, 0, "1",
                "Expected doctor consultations report to include doctor 1.");

        List<String[]> visits = generator.generatePatientVisits("2026-03-01", "2026-03-31");
        assertContains(visits, 1, "patient1",
                "Expected patient visits report to include patient1.");
    }

    private static int findAppointmentId(String username, int doctorId, String date, String time) throws Exception {
        try (Connection conn = DerbyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT AppointmentID FROM APPOINTMENTS "
                     + "WHERE Username = ? AND DoctorID = ? AND ApptDate = ? AND ApptTime = ? "
                     + "ORDER BY AppointmentID DESC")) {
            ps.setString(1, username);
            ps.setInt(2, doctorId);
            ps.setString(3, date);
            ps.setString(4, time);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("AppointmentID");
                }
            }
        }
        throw new AssertionError("Could not find appointment for " + username + " at " + date + " " + time);
    }

    private static int findUserId(String username) throws Exception {
        try (Connection conn = DerbyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT UserID FROM USERS WHERE Username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserID");
                }
            }
        }
        throw new AssertionError("Could not find user " + username);
    }

    private static String getUserRole(String username) throws Exception {
        try (Connection conn = DerbyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT Role FROM USERS WHERE Username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("Role") : null;
            }
        }
    }

    private static String getAppointmentStatus(int appointmentId) throws Exception {
        try (Connection conn = DerbyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT Status FROM APPOINTMENTS WHERE AppointmentID = ?")) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Status");
                }
            }
        }
        throw new AssertionError("Could not find appointment " + appointmentId);
    }

    private static String getAppointmentTime(int appointmentId) throws Exception {
        try (Connection conn = DerbyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT ApptTime FROM APPOINTMENTS WHERE AppointmentID = ?")) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ApptTime");
                }
            }
        }
        throw new AssertionError("Could not find appointment " + appointmentId);
    }

    private static void assertContains(List<String[]> rows, int columnIndex, String expectedValue, String message) {
        for (String[] row : rows) {
            if (row != null && row.length > columnIndex && expectedValue.equals(row[columnIndex])) {
                return;
            }
        }
        throw new AssertionError(message);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + " Actual: " + actual);
        }
    }

    private static void assertNotNull(Object value, String message) {
        assertTrue(value != null, message);
    }

    private static void assertNull(Object value, String message) {
        assertTrue(value == null, message);
    }

    private static void run(String name, CheckedRunnable test) throws Exception {
        try {
            test.run();
            System.out.println("[PASS] " + name);
        } catch (Exception e) {
            System.err.println("[FAIL] " + name + ": " + e.getMessage());
            throw e;
        }
    }

    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
