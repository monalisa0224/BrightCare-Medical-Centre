package brigthcare_medical_centre.tests;

import brigthcare_medical_centre.admin.AdminImpl;
import brigthcare_medical_centre.auth.UserRole;
import brigthcare_medical_centre.auth.AuthenticationImpl;
import brigthcare_medical_centre.database.DatabaseSetup;
import brigthcare_medical_centre.database.DerbyConnection;
import brigthcare_medical_centre.database.DoctorDB;
import brigthcare_medical_centre.database.PatientDB;
import brigthcare_medical_centre.database.ReceptionistDB;
import brigthcare_medical_centre.common.PatientInfo;
import brigthcare_medical_centre.report.ReportGenerator;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

public class RegressionSmokeTests {

    public static void main(String[] args) throws Exception {
        System.setProperty("brightcare.db.url",
                "jdbc:derby:BrightCareDB_Test_" + System.currentTimeMillis() + ";create=true");
        DatabaseSetup.initialize();

        run("booking integrity", RegressionSmokeTests::testBookingIntegrity);
        run("receptionist patient registration details", RegressionSmokeTests::testReceptionistPatientRegistrationDetails);
        run("receptionist multi-field patient search", RegressionSmokeTests::testReceptionistMultiFieldSearch);
        run("patient cancellation ownership", RegressionSmokeTests::testPatientCancellationOwnershipAndRestore);
        run("doctor reschedule and slot guardrails", RegressionSmokeTests::testDoctorRescheduleAndScheduleGuards);
        run("admin role provisioning", RegressionSmokeTests::testAdminProvisioningAndCleanup);
        run("doctor role change with schedule only", RegressionSmokeTests::testDoctorRoleChangeWithScheduleOnly);
        run("report schema compatibility", RegressionSmokeTests::testReportQueriesAgainstActualSchema);

        System.out.println("All regression smoke tests passed.");
        System.exit(0);
    }

    private static void testBookingIntegrity() throws Exception {
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        String date = futureDate(7);
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

    private static void testReceptionistPatientRegistrationDetails() throws Exception {
        String suffix = String.valueOf(System.currentTimeMillis());
        String username = "john_tan_" + suffix;
        String secondUsername = "jane_tan_" + suffix;
        PatientInfo patient = new PatientInfo(username, "patient123", "John", "Tan",
                "900101-14-1234", "0123456789", "Kuala Lumpur");
        PatientInfo secondPatient = new PatientInfo(secondUsername, "patient123", "Jane", "Tan",
                "900101-14-1235", "0123456790", "Kuala Lumpur");

        assertTrue(new ReceptionistDB().registerPatient(patient),
                "Expected receptionist registration without a medical record ID to succeed.");
        assertTrue(new ReceptionistDB().registerPatient(secondPatient),
                "Expected a second receptionist registration to succeed.");
        assertTrue(patient.getMedicalRecordId().matches("MR-\\d{4}-\\d{4,}"),
                "Expected a readable system-generated medical record ID.");
        assertFalse(patient.getMedicalRecordId().equals(secondPatient.getMedicalRecordId()),
                "Generated medical record IDs must be unique.");
        assertNotNull(new AuthenticationImpl().login(username, "patient123"),
                "Expected the newly registered patient to be able to log in.");
        try (Connection conn = DerbyConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT FirstName, LastName, ICPassportNumber, MedicalRecordID, ContactNumber, Address "
                     + "FROM PATIENTS WHERE Username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Expected the registered patient profile row.");
                assertEquals("John", rs.getString("FirstName"), "First name was not stored.");
                assertEquals("Tan", rs.getString("LastName"), "Last name was not stored.");
                assertEquals("900101-14-1234", rs.getString("ICPassportNumber"), "IC/passport was not stored.");
                assertEquals(patient.getMedicalRecordId(), rs.getString("MedicalRecordID"), "Generated medical record ID was not stored.");
                assertTrue(rs.getString("MedicalRecordID").startsWith("MR-" + Year.now().getValue() + "-"),
                        "Medical record ID used the wrong year.");
                assertEquals("0123456789", rs.getString("ContactNumber"), "Contact number was not stored.");
                assertEquals("Kuala Lumpur", rs.getString("Address"), "Address was not stored.");
            }
        }
    }

    private static void testPatientCancellationOwnershipAndRestore() throws Exception {
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        String date = futureDate(7);
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

    private static void testReceptionistMultiFieldSearch() throws Exception {
        String suffix = String.valueOf(System.currentTimeMillis());
        String username = "search_patient_" + suffix;
        PatientInfo patient = new PatientInfo(username, "patient123", "SearchFirst", "SearchLast",
                "IC-SEARCH-" + suffix, "0190000000", "Search Address");
        ReceptionistDB receptionistDB = new ReceptionistDB();
        assertTrue(receptionistDB.registerPatient(patient), "Expected searchable patient registration to succeed.");
        assertSearchFinds(receptionistDB, username, username);
        assertSearchFinds(receptionistDB, "SearchFirst", username);
        assertSearchFinds(receptionistDB, "SearchLast", username);
        assertSearchFinds(receptionistDB, "IC-SEARCH-" + suffix, username);
        assertSearchFinds(receptionistDB, patient.getMedicalRecordId(), username);
    }

    private static void assertSearchFinds(ReceptionistDB receptionistDB, String keyword, String username) {
        for (PatientInfo result : receptionistDB.searchPatient(keyword)) {
            if (username.equals(result.getUsername())) {
                return;
            }
        }
        throw new AssertionError("Expected search for '" + keyword + "' to return " + username);
    }

    private static void testDoctorRescheduleAndScheduleGuards() throws Exception {
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        String date = futureDate(7);
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

    private static void testDoctorRoleChangeWithScheduleOnly() throws Exception {
        AdminImpl admin = new AdminImpl();
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        String suffix = String.valueOf(System.currentTimeMillis());
        String date = futureDate(9);

        String scheduleOnlyDoctor = "sched_doctor_" + suffix;
        assertTrue(admin.registerUser(scheduleOnlyDoctor, "doctor123", UserRole.DOCTOR),
                "Expected a schedule-only doctor account to be registered.");
        int scheduleOnlyDoctorId = doctorDB.getDoctorIdByUsername(scheduleOnlyDoctor);
        assertTrue(scheduleOnlyDoctorId > 0, "Expected a DOCTORS row for the schedule-only doctor.");
        assertTrue(doctorDB.updateDoctorSchedule(scheduleOnlyDoctorId, date, "09:00", true),
                "Expected a schedule slot to be created for the schedule-only doctor.");

        int scheduleOnlyUserId = findUserId(scheduleOnlyDoctor);
        assertTrue(admin.updateUserRole(scheduleOnlyUserId, UserRole.RECEPTIONIST),
                "Expected a doctor with only schedule slots to be re-assignable.");
        assertEquals("RECEPTIONIST", getUserRole(scheduleOnlyDoctor),
                "Expected the schedule-only doctor role to change in USERS.");
        assertEquals(-1, doctorDB.getDoctorIdByUsername(scheduleOnlyDoctor),
                "Expected the schedule-only doctor profile to be removed after the role change.");

        String activeDoctor = "active_doctor_" + suffix;
        assertTrue(admin.registerUser(activeDoctor, "doctor123", UserRole.DOCTOR),
                "Expected an active doctor account to be registered.");
        int activeDoctorId = doctorDB.getDoctorIdByUsername(activeDoctor);
        assertTrue(activeDoctorId > 0, "Expected a DOCTORS row for the active doctor.");
        assertTrue(doctorDB.updateDoctorSchedule(activeDoctorId, date, "10:00", true),
                "Expected a schedule slot to be created for the active doctor.");
        assertTrue(patientDB.bookAppointment("patient1", activeDoctorId, date, "10:00"),
                "Expected an appointment to be booked for the active doctor.");

        int activeDoctorUserId = findUserId(activeDoctor);
        try {
            boolean result = admin.updateUserRole(activeDoctorUserId, UserRole.RECEPTIONIST);
            throw new AssertionError(
                    "Expected role change to be blocked for a doctor with an active appointment, but it returned: " + result);
        } catch (RemoteException expected) {
            assertTrue(expected.getMessage().contains("cannot be re-assigned"),
                    "Expected the guard message to explain the blocked role change.");
        }
        assertEquals("DOCTOR", getUserRole(activeDoctor),
                "Expected the active doctor role to remain unchanged.");
    }

    private static void testReportQueriesAgainstActualSchema() throws Exception {
        DoctorDB doctorDB = new DoctorDB();
        PatientDB patientDB = new PatientDB();
        ReportGenerator generator = new ReportGenerator();
        String date = futureDate(8);
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

        String startDate = LocalDate.now().toString();
        List<String[]> monthly = generator.generateMonthlyAppointments(startDate, date);
        assertContains(monthly, 0, String.valueOf(appointmentId),
                "Expected monthly appointments report to include the completed appointment.");

        List<String[]> consultations = generator.generateDoctorConsultations(startDate, date);
        assertContains(consultations, 0, "1",
                "Expected doctor consultations report to include doctor 1.");

        List<String[]> visits = generator.generatePatientVisits(startDate, date);
        assertContains(visits, 1, "patient1",
                "Expected patient visits report to include patient1.");
    }

    private static String futureDate(int offsetDays) {
        return LocalDate.now().plusDays(offsetDays).toString();
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
