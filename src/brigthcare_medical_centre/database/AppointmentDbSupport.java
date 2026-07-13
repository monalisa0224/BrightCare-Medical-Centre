package brigthcare_medical_centre.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class AppointmentDbSupport {

    private AppointmentDbSupport() {
    }

    static boolean hasActiveAppointment(Connection conn, int doctorId, String date, String time,
            Integer excludeAppointmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM APPOINTMENTS "
                + "WHERE DoctorID = ? AND ApptDate = ? AND ApptTime = ? "
                + "AND Status IN ('PENDING', 'ACCEPTED')";
        if (excludeAppointmentId != null) {
            sql += " AND AppointmentID <> ?";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, date);
            ps.setString(3, time);
            if (excludeAppointmentId != null) {
                ps.setInt(4, excludeAppointmentId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    static boolean reserveAvailableSlot(Connection conn, int doctorId, String date, String time,
            Integer excludeAppointmentId) throws SQLException {
        if (hasActiveAppointment(conn, doctorId, date, time, excludeAppointmentId)) {
            return false;
        }

        String sql = "UPDATE DOCTOR_SCHEDULE SET IsAvailable = false "
                + "WHERE DoctorID = ? AND ScheduleDate = ? AND TimeSlot = ? AND IsAvailable = true";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, date);
            ps.setString(3, time);
            return ps.executeUpdate() == 1;
        }
    }

    static void releaseSlotIfUnused(Connection conn, int doctorId, String date, String time,
            Integer excludeAppointmentId) throws SQLException {
        if (hasActiveAppointment(conn, doctorId, date, time, excludeAppointmentId)) {
            return;
        }

        if (!updateSlotAvailability(conn, doctorId, date, time, true)) {
            insertSlotAvailability(conn, doctorId, date, time, true);
        }
    }

    static boolean updateSlotAvailability(Connection conn, int doctorId, String date, String time,
            boolean isAvailable) throws SQLException {
        String sql = "UPDATE DOCTOR_SCHEDULE SET IsAvailable = ? "
                + "WHERE DoctorID = ? AND ScheduleDate = ? AND TimeSlot = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isAvailable);
            ps.setInt(2, doctorId);
            ps.setString(3, date);
            ps.setString(4, time);
            return ps.executeUpdate() > 0;
        }
    }

    static boolean slotExists(Connection conn, int doctorId, String date, String time) throws SQLException {
        String sql = "SELECT 1 FROM DOCTOR_SCHEDULE WHERE DoctorID = ? AND ScheduleDate = ? AND TimeSlot = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, date);
            ps.setString(3, time);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static void insertSlotAvailability(Connection conn, int doctorId, String date, String time,
            boolean isAvailable) throws SQLException {
        String sql = "INSERT INTO DOCTOR_SCHEDULE (DoctorID, ScheduleDate, TimeSlot, IsAvailable) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, date);
            ps.setString(3, time);
            ps.setBoolean(4, isAvailable);
            ps.executeUpdate();
        }
    }
}
