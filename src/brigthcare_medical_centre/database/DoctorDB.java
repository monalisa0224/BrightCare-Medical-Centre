package brigthcare_medical_centre.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DoctorDB {

    private Connection getConnection() throws SQLException {
        return DerbyConnection.getConnection();
    }

    public List<String[]> getPendingAppointments(int doctorId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, u.Username, p.ContactNumber, "
                   + "a.ApptDate, a.ApptTime, a.Status "
                   + "FROM APPOINTMENTS a "
                   + "JOIN USERS u ON a.Username = u.Username "
                   + "JOIN PATIENTS p ON a.Username = p.Username "
                   + "WHERE a.DoctorID = ? AND a.Status = 'PENDING' "
                   + "ORDER BY a.ApptDate, a.ApptTime";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("AppointmentID")),
                    rs.getString("Username"),
                    rs.getString("ContactNumber"),
                    rs.getString("ApptDate"),
                    rs.getString("ApptTime"),
                    rs.getString("Status")
                });
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean acceptAppointment(int appointmentId) {
        String sql = "UPDATE APPOINTMENTS SET Status = 'ACCEPTED' "
                + "WHERE AppointmentID = ? AND Status = 'PENDING'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean rejectAppointment(int appointmentId) {
        return updateAppointmentStatusAndReleaseSlot(appointmentId, "PENDING", "REJECTED");
    }

    private boolean updateAppointmentStatusAndReleaseSlot(int appointmentId, String expectedStatus,
            String newStatus) {
        String getAppt = "SELECT DoctorID, ApptDate, ApptTime, Status FROM APPOINTMENTS WHERE AppointmentID = ?";
        String updateSql = "UPDATE APPOINTMENTS SET Status = ? WHERE AppointmentID = ? AND Status = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement getPs = conn.prepareStatement(getAppt)) {
                getPs.setInt(1, appointmentId);
                try (ResultSet rs = getPs.executeQuery()) {
                    if (!rs.next() || !expectedStatus.equals(rs.getString("Status"))) {
                        conn.rollback();
                        return false;
                    }

                    int doctorId = rs.getInt("DoctorID");
                    String date = rs.getString("ApptDate");
                    String time = rs.getString("ApptTime");

                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, newStatus);
                        updatePs.setInt(2, appointmentId);
                        updatePs.setString(3, expectedStatus);
                        if (updatePs.executeUpdate() != 1) {
                            conn.rollback();
                            return false;
                        }
                    }

                    AppointmentDbSupport.releaseSlotIfUnused(conn, doctorId, date, time, appointmentId);
                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String[]> getDoctorAppointments(int doctorId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, u.Username, a.ApptDate, a.ApptTime, a.Status "
                   + "FROM APPOINTMENTS a "
                   + "JOIN USERS u ON a.Username = u.Username "
                   + "WHERE a.DoctorID = ? "
                   + "ORDER BY a.ApptDate DESC, a.ApptTime DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("AppointmentID")),
                    rs.getString("Username"),
                    rs.getString("ApptDate"),
                    rs.getString("ApptTime"),
                    rs.getString("Status")
                });
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean cancelAppointmentByDoctor(int appointmentId) {
        return cancelPendingOrAcceptedAppointment(appointmentId, "CANCELLED");
    }

    private boolean cancelPendingOrAcceptedAppointment(int appointmentId, String newStatus) {
        String getAppt = "SELECT DoctorID, ApptDate, ApptTime, Status FROM APPOINTMENTS WHERE AppointmentID = ?";
        String updateSql = "UPDATE APPOINTMENTS SET Status = ? "
                + "WHERE AppointmentID = ? AND Status IN ('PENDING', 'ACCEPTED')";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement getPs = conn.prepareStatement(getAppt)) {
                getPs.setInt(1, appointmentId);
                try (ResultSet rs = getPs.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }

                    String status = rs.getString("Status");
                    if (!"PENDING".equals(status) && !"ACCEPTED".equals(status)) {
                        conn.rollback();
                        return false;
                    }

                    int doctorId = rs.getInt("DoctorID");
                    String date = rs.getString("ApptDate");
                    String time = rs.getString("ApptTime");

                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, newStatus);
                        updatePs.setInt(2, appointmentId);
                        if (updatePs.executeUpdate() != 1) {
                            conn.rollback();
                            return false;
                        }
                    }

                    AppointmentDbSupport.releaseSlotIfUnused(conn, doctorId, date, time, appointmentId);
                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean rescheduleAppointment(int appointmentId, String newDate, String newTime) {
        String getAppt = "SELECT DoctorID, ApptDate, ApptTime, Status FROM APPOINTMENTS WHERE AppointmentID = ?";
        String updateSql = "UPDATE APPOINTMENTS SET ApptDate = ?, ApptTime = ? "
                + "WHERE AppointmentID = ? AND Status IN ('PENDING', 'ACCEPTED')";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement getPs = conn.prepareStatement(getAppt)) {
                getPs.setInt(1, appointmentId);
                try (ResultSet rs = getPs.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }

                    String status = rs.getString("Status");
                    if (!"PENDING".equals(status) && !"ACCEPTED".equals(status)) {
                        conn.rollback();
                        return false;
                    }

                    int doctorId = rs.getInt("DoctorID");
                    String oldDate = rs.getString("ApptDate");
                    String oldTime = rs.getString("ApptTime");

                    if (oldDate.equals(newDate) && oldTime.equals(newTime)) {
                        conn.rollback();
                        return true;
                    }

                    if (!AppointmentDbSupport.reserveAvailableSlot(conn, doctorId, newDate, newTime, appointmentId)) {
                        conn.rollback();
                        return false;
                    }

                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, newDate);
                        updatePs.setString(2, newTime);
                        updatePs.setInt(3, appointmentId);
                        if (updatePs.executeUpdate() != 1) {
                            conn.rollback();
                            return false;
                        }
                    }

                    AppointmentDbSupport.releaseSlotIfUnused(conn, doctorId, oldDate, oldTime, appointmentId);
                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateConsultationNotes(int appointmentId, int doctorId,
            String patientUsername, String diagnosis, String treatment,
            String prescription, String notes) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String checkSql = "SELECT COUNT(*) FROM CONSULTATION_NOTES WHERE AppointmentID = ?";
        String insertSql = "INSERT INTO CONSULTATION_NOTES "
            + "(AppointmentID, DoctorID, PatientUsername, ConsultationDate, "
            + "Diagnosis, Treatment, Prescription, Notes) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE CONSULTATION_NOTES SET "
            + "Diagnosis = ?, Treatment = ?, Prescription = ?, Notes = ? "
            + "WHERE AppointmentID = ?";
        try (Connection conn = getConnection()) {
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, appointmentId);
            ResultSet rs = checkPs.executeQuery();
            rs.next();
            boolean exists = rs.getInt(1) > 0;
            rs.close();
            checkPs.close();

            boolean ok;
            if (exists) {
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setString(1, diagnosis);
                updatePs.setString(2, treatment);
                updatePs.setString(3, prescription);
                updatePs.setString(4, notes);
                updatePs.setInt(5, appointmentId);
                ok = updatePs.executeUpdate() > 0;
                updatePs.close();

                PreparedStatement statusPs = conn.prepareStatement(
                    "UPDATE APPOINTMENTS SET Status = 'COMPLETED' WHERE AppointmentID = ? AND Status = 'ACCEPTED'");
                statusPs.setInt(1, appointmentId);
                statusPs.executeUpdate();
                statusPs.close();
            } else {
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                insertPs.setInt(1, appointmentId);
                insertPs.setInt(2, doctorId);
                insertPs.setString(3, patientUsername);
                insertPs.setString(4, date);
                insertPs.setString(5, diagnosis);
                insertPs.setString(6, treatment);
                insertPs.setString(7, prescription);
                insertPs.setString(8, notes);
                ok = insertPs.executeUpdate() > 0;
                insertPs.close();

                PreparedStatement statusPs = conn.prepareStatement(
                    "UPDATE APPOINTMENTS SET Status = 'COMPLETED' WHERE AppointmentID = ? AND Status = 'ACCEPTED'");
                statusPs.setInt(1, appointmentId);
                statusPs.executeUpdate();
                statusPs.close();
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String[] getConsultationNotes(int appointmentId) {
        String sql = "SELECT cn.*, u.Username FROM CONSULTATION_NOTES cn "
                   + "JOIN USERS u ON cn.PatientUsername = u.Username "
                   + "WHERE cn.AppointmentID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String[] note = new String[]{
                    String.valueOf(rs.getInt("NoteID")),
                    String.valueOf(rs.getInt("AppointmentID")),
                    rs.getString("PatientUsername"),
                    rs.getString("ConsultationDate"),
                    rs.getString("Diagnosis"),
                    rs.getString("Treatment"),
                    rs.getString("Prescription"),
                    rs.getString("Notes"),
                    rs.getString("CreatedDate")
                };
                rs.close();
                return note;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String[]> getPatientHistory(String patientUsername) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT a.AppointmentID, a.ApptDate, a.ApptTime, a.Status, "
                   + "cn.Diagnosis, cn.Treatment, cn.Prescription, cn.Notes "
                   + "FROM APPOINTMENTS a "
                   + "LEFT JOIN CONSULTATION_NOTES cn ON a.AppointmentID = cn.AppointmentID "
                   + "WHERE a.Username = ? "
                   + "ORDER BY a.ApptDate DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientUsername);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("AppointmentID")),
                    rs.getString("ApptDate"),
                    rs.getString("ApptTime"),
                    rs.getString("Status"),
                    rs.getString("Diagnosis"),
                    rs.getString("Treatment"),
                    rs.getString("Prescription"),
                    rs.getString("Notes")
                });
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String[]> getDoctorTimetable(int doctorId, String weekStartDate) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT ScheduleDate, TimeSlot, IsAvailable "
                   + "FROM DOCTOR_SCHEDULE "
                   + "WHERE DoctorID = ? AND ScheduleDate >= ? "
                   + "ORDER BY ScheduleDate, TimeSlot";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, weekStartDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("ScheduleDate"),
                    rs.getString("TimeSlot"),
                    String.valueOf(rs.getBoolean("IsAvailable"))
                });
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateDoctorSchedule(int doctorId, String date, String slot, boolean isAvailable) {
        try (Connection conn = getConnection()) {
            if (AppointmentDbSupport.hasActiveAppointment(conn, doctorId, date, slot, null)) {
                return false;
            }

            if (AppointmentDbSupport.slotExists(conn, doctorId, date, slot)) {
                return AppointmentDbSupport.updateSlotAvailability(conn, doctorId, date, slot, isAvailable);
            }

            AppointmentDbSupport.insertSlotAvailability(conn, doctorId, date, slot, isAvailable);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getAvailableSlots(int doctorId, String date) {
        List<String> slots = new ArrayList<>();
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
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return slots;
    }

    public String[] getDoctorProfile(int doctorId) {
        String sql = "SELECT d.DoctorID, d.DoctorName, d.Specialization, "
                   + "d.ContactNumber, d.Username, u.Role "
                   + "FROM DOCTORS d JOIN USERS u ON d.Username = u.Username "
                   + "WHERE d.DoctorID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String[] profile = new String[]{
                    String.valueOf(rs.getInt("DoctorID")),
                    rs.getString("DoctorName"),
                    rs.getString("Specialization"),
                    rs.getString("ContactNumber"),
                    rs.getString("Username"),
                    rs.getString("Role")
                };
                rs.close();
                return profile;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateDoctorProfile(int doctorId, String doctorName, String specialization) {
        String sql = "UPDATE DOCTORS SET DoctorName = ?, Specialization = ? WHERE DoctorID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorName);
            ps.setString(2, specialization);
            ps.setInt(3, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changePassword(int doctorId, String oldPassword, String newPassword) {
        String getUsername = "SELECT Username FROM DOCTORS WHERE DoctorID = ?";
        try (Connection conn = getConnection();
             PreparedStatement getPs = conn.prepareStatement(getUsername)) {
            getPs.setInt(1, doctorId);
            ResultSet rs = getPs.executeQuery();
            if (rs.next()) {
                String username = rs.getString("Username");
                rs.close();
                getPs.close();

                PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT PasswordHash FROM USERS WHERE Username = ?");
                checkPs.setString(1, username);
                ResultSet checkRs = checkPs.executeQuery();
                if (checkRs.next()) {
                    String storedHash = checkRs.getString("PasswordHash");
                    String inputHash = DatabaseSetup.hashPassword(oldPassword);
                    checkRs.close();
                    checkPs.close();

                    if (!storedHash.equals(inputHash)) {
                        return false;
                    }

                    PreparedStatement updatePs = conn.prepareStatement(
                        "UPDATE USERS SET PasswordHash = ? WHERE Username = ?");
                    updatePs.setString(1, DatabaseSetup.hashPassword(newPassword));
                    updatePs.setString(2, username);
                    boolean ok = updatePs.executeUpdate() > 0;
                    updatePs.close();
                    return ok;
                }
                checkRs.close();
                checkPs.close();
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateContactInfo(int doctorId, String contactNumber) {
        String sql = "UPDATE DOCTORS SET ContactNumber = ? WHERE DoctorID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contactNumber);
            ps.setInt(2, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int[] getTodaySummary(int doctorId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        int pending = 0, accepted = 0, completed = 0, cancelled = 0, total = 0;
        String sql = "SELECT Status, COUNT(*) AS cnt FROM APPOINTMENTS "
                   + "WHERE DoctorID = ? AND ApptDate = ? GROUP BY Status";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String status = rs.getString("Status");
                int cnt = rs.getInt("cnt");
                total += cnt;
                switch (status) {
                    case "PENDING":   pending = cnt; break;
                    case "ACCEPTED":  accepted = cnt; break;
                    case "COMPLETED": completed = cnt; break;
                    case "CANCELLED": cancelled = cnt; break;
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{pending, accepted, completed, cancelled, total};
    }

    public List<String[]> getTodayAppointments(int doctorId) {
        List<String[]> list = new ArrayList<>();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String sql = "SELECT a.AppointmentID, u.Username, a.ApptTime, a.Status "
                   + "FROM APPOINTMENTS a "
                   + "JOIN USERS u ON a.Username = u.Username "
                   + "WHERE a.DoctorID = ? AND a.ApptDate = ? "
                   + "ORDER BY a.ApptTime";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("AppointmentID")),
                    rs.getString("Username"),
                    rs.getString("ApptTime"),
                    rs.getString("Status")
                });
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getDoctorIdByUsername(String username) {
        String sql = "SELECT DoctorID FROM DOCTORS WHERE Username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("DoctorID");
                rs.close();
                return id;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<String[]> getDistinctPatientsForDoctor(int doctorId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT DISTINCT a.Username, p.ContactNumber "
                   + "FROM APPOINTMENTS a "
                   + "LEFT JOIN PATIENTS p ON a.Username = p.Username "
                   + "WHERE a.DoctorID = ? "
                   + "ORDER BY a.Username";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("Username"),
                    rs.getString("ContactNumber")
                });
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
