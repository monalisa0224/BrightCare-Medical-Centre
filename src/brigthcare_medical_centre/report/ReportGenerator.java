package brigthcare_medical_centre.report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import brigthcare_medical_centre.database.DerbyConnection;

public class ReportGenerator {

    public List<String[]> generateMonthlyAppointments(String startDate, String endDate) {
        List<String[]> data = new ArrayList<>();
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT a.AppointmentID, a.Username, d.DoctorName, a.ApptDate, a.ApptTime, a.Status "
                    + "FROM APPOINTMENTS a "
                    + "JOIN DOCTORS d ON a.DoctorID = d.DoctorID "
                    + "WHERE a.ApptDate >= ? AND a.ApptDate <= ? "
                    + "ORDER BY a.ApptDate, a.ApptTime");
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] row = new String[5];
                row[0] = String.valueOf(rs.getInt("AppointmentID"));
                row[1] = rs.getString("Username");
                row[2] = rs.getString("DoctorName");
                row[3] = rs.getString("ApptDate") + " " + rs.getString("ApptTime");
                row[4] = rs.getString("Status");
                data.add(row);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public List<String[]> generateDoctorConsultations(String startDate, String endDate) {
        List<String[]> data = new ArrayList<>();
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT d.DoctorID, d.DoctorName, COUNT(cn.NoteID) AS TotalConsultations "
                    + "FROM DOCTORS d "
                    + "LEFT JOIN CONSULTATION_NOTES cn ON d.DoctorID = cn.DoctorID "
                    + "AND cn.ConsultationDate >= ? AND cn.ConsultationDate <= ? "
                    + "GROUP BY d.DoctorID, d.DoctorName "
                    + "ORDER BY TotalConsultations DESC");
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] row = new String[3];
                row[0] = String.valueOf(rs.getInt("DoctorID"));
                row[1] = rs.getString("DoctorName");
                row[2] = String.valueOf(rs.getInt("TotalConsultations"));
                data.add(row);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public List<String[]> generatePatientVisits(String startDate, String endDate) {
        List<String[]> data = new ArrayList<>();
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT p.PatientID, p.Username, COUNT(a.AppointmentID) AS TotalVisits, "
                    + "MIN(a.ApptDate) AS FirstVisit, MAX(a.ApptDate) AS LastVisit "
                    + "FROM PATIENTS p "
                    + "JOIN APPOINTMENTS a ON p.Username = a.Username "
                    + "WHERE a.ApptDate >= ? AND a.ApptDate <= ? "
                    + "AND a.Status = 'COMPLETED' "
                    + "GROUP BY p.PatientID, p.Username "
                    + "ORDER BY TotalVisits DESC");
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] row = new String[5];
                row[0] = String.valueOf(rs.getInt("PatientID"));
                row[1] = rs.getString("Username");
                row[2] = String.valueOf(rs.getInt("TotalVisits"));
                row[3] = rs.getString("FirstVisit");
                row[4] = rs.getString("LastVisit");
                data.add(row);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
