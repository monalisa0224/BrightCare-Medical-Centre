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
                    "SELECT a.AppointmentID, p.FirstName, p.LastName, d.Name, a.DateTime, a.Status "
                    + "FROM Appointments a "
                    + "JOIN Patients p ON a.PatientID = p.PatientID "
                    + "JOIN Doctors d ON a.DoctorID = d.DoctorID "
                    + "WHERE a.DateTime >= ? AND a.DateTime < ? "
                    + "ORDER BY a.DateTime");
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] row = new String[6];
                row[0] = String.valueOf(rs.getInt("AppointmentID"));
                row[1] = rs.getString("FirstName") + " " + rs.getString("LastName");
                row[2] = rs.getString("Name");
                row[3] = rs.getTimestamp("DateTime").toString();
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
                    "SELECT d.DoctorID, d.Name AS DoctorName, COUNT(c.ConsultationID) AS TotalConsultations "
                    + "FROM Doctors d "
                    + "LEFT JOIN Consultations c ON d.DoctorID = c.DoctorID "
                    + "WHERE c.ConsultationDate >= ? AND c.ConsultationDate < ? "
                    + "GROUP BY d.DoctorID, d.Name "
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
                    "SELECT p.PatientID, p.FirstName, p.LastName, "
                    + "COUNT(a.AppointmentID) AS TotalVisits, "
                    + "MIN(a.DateTime) AS FirstVisit, MAX(a.DateTime) AS LastVisit "
                    + "FROM Patients p "
                    + "JOIN Appointments a ON p.PatientID = a.PatientID "
                    + "WHERE a.DateTime >= ? AND a.DateTime < ? "
                    + "GROUP BY p.PatientID, p.FirstName, p.LastName "
                    + "ORDER BY TotalVisits DESC");
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String[] row = new String[5];
                row[0] = String.valueOf(rs.getInt("PatientID"));
                row[1] = rs.getString("FirstName") + " " + rs.getString("LastName");
                row[2] = String.valueOf(rs.getInt("TotalVisits"));
                row[3] = rs.getTimestamp("FirstVisit").toString();
                row[4] = rs.getTimestamp("LastVisit").toString();
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
