package brigthcare_medical_centre.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDB {

    private Connection getConnection() throws SQLException {
        return DerbyConnection.getConnection();
    }

    // Update patient personal info
    public boolean updatePersonalInfo(String username, String contactNumber, String address) {
        String sql = "UPDATE PATIENTS SET ContactNumber = ?, Address = ? WHERE Username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contactNumber);
            ps.setString(2, address);
            ps.setString(3, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Book appointment
    public boolean bookAppointment(String username, int doctorId, String date, String time) {
        String sql = "INSERT INTO APPOINTMENTS (Username, DoctorID, ApptDate, ApptTime, Status) "
                   + "VALUES (?, ?, ?, ?, 'UPCOMING')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, doctorId);
            ps.setString(3, date);
            ps.setString(4, time);
            boolean booked = ps.executeUpdate() > 0;

            if (booked) {
                // Mark slot as unavailable
                PreparedStatement updateSlot = conn.prepareStatement(
                    "UPDATE DOCTOR_SCHEDULE SET IsAvailable = false "
                    + "WHERE DoctorID = ? AND ScheduleDate = ? AND TimeSlot = ?");
                updateSlot.setInt(1, doctorId);
                updateSlot.setString(2, date);
                updateSlot.setString(3, time);
                updateSlot.executeUpdate();
                updateSlot.close();
            }
            return booked;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cancel appointment
    // Cancel appointment
    public boolean cancelAppointment(int appointmentId) {
        // First get the appointment details before cancelling
        String getAppt = "SELECT DoctorID, ApptDate, ApptTime FROM APPOINTMENTS WHERE AppointmentID = ?";
        try (Connection conn = getConnection();
             PreparedStatement getPs = conn.prepareStatement(getAppt)) {
            getPs.setInt(1, appointmentId);
            ResultSet rs = getPs.executeQuery();

            if (rs.next()) {
                int doctorId = rs.getInt("DoctorID");
                String date = rs.getString("ApptDate");
                String time = rs.getString("ApptTime");

                // Cancel the appointment
                PreparedStatement cancelPs = conn.prepareStatement(
                    "UPDATE APPOINTMENTS SET Status = 'CANCELLED' WHERE AppointmentID = ?");
                cancelPs.setInt(1, appointmentId);
                boolean cancelled = cancelPs.executeUpdate() > 0;
                cancelPs.close();

                if (cancelled) {
                    // Restore the slot availability
                    PreparedStatement restoreSlot = conn.prepareStatement(
                        "UPDATE DOCTOR_SCHEDULE SET IsAvailable = true "
                        + "WHERE DoctorID = ? AND ScheduleDate = ? AND TimeSlot = ?");
                    restoreSlot.setInt(1, doctorId);
                    restoreSlot.setString(2, date);
                    restoreSlot.setString(3, time);
                    restoreSlot.executeUpdate();
                    restoreSlot.close();
                }
                return cancelled;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // View upcoming appointments
    public List<String[]> getUpcomingAppointments(String username) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, d.DoctorName, a.ApptDate, a.ApptTime, a.Status "
                   + "FROM APPOINTMENTS a JOIN DOCTORS d ON a.DoctorID = d.DoctorID "
                   + "WHERE a.Username = ? AND a.Status = 'UPCOMING' "
                   + "ORDER BY a.ApptDate, a.ApptTime";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("AppointmentID")),
                    rs.getString("DoctorName"),
                    rs.getString("ApptDate"),
                    rs.getString("ApptTime"),
                    rs.getString("Status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // View appointment history (completed + cancelled)
    public List<String[]> getAppointmentHistory(String username) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, d.DoctorName, a.ApptDate, a.ApptTime, a.Status "
                   + "FROM APPOINTMENTS a JOIN DOCTORS d ON a.DoctorID = d.DoctorID "
                   + "WHERE a.Username = ? AND a.Status <> 'UPCOMING' "
                   + "ORDER BY a.ApptDate DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("AppointmentID")),
                    rs.getString("DoctorName"),
                    rs.getString("ApptDate"),
                    rs.getString("ApptTime"),
                    rs.getString("Status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Check doctor availability
    public List<String> getDoctorAvailability(int doctorId, String date) {
        List<String> slots = new ArrayList<>();
        System.out.println("DEBUG: Checking DoctorID=" + doctorId + " Date=" + date);
        String sql = "SELECT TimeSlot FROM DOCTOR_SCHEDULE "
                   + "WHERE DoctorID = ? AND ScheduleDate = ? AND IsAvailable = true";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                slots.add(rs.getString("TimeSlot"));
            }
            System.out.println("DEBUG: Found " + slots.size() + " slots");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return slots;
    }
    
    // Get all doctors
    public List<String[]> getDoctors() {
        List<String[]> doctors = new ArrayList<>();
        String sql = "SELECT DoctorID, DoctorName, Specialization FROM DOCTORS";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doctors.add(new String[]{
                    String.valueOf(rs.getInt("DoctorID")),
                    rs.getString("DoctorName"),
                    rs.getString("Specialization")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }
    
    // Get patient profile
public String[] getPatientProfile(String username) {
    String sql = "SELECT p.Username, p.ContactNumber, p.Address, u.Role "
               + "FROM PATIENTS p JOIN USERS u ON p.Username = u.Username "
               + "WHERE p.Username = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new String[]{
                rs.getString("Username"),
                rs.getString("ContactNumber"),
                rs.getString("Address"),
                rs.getString("Role")
            };
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
}