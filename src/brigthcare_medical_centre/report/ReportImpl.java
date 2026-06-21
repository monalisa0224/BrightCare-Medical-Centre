package brigthcare_medical_centre.report;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import brigthcare_medical_centre.common.ReportInterface;
import brigthcare_medical_centre.database.AuditLogger;
import brigthcare_medical_centre.database.DerbyConnection;
import brigthcare_medical_centre.util.DateUtils;

public class ReportImpl extends UnicastRemoteObject implements ReportInterface {

    private final ReportGenerator reportGenerator;

    public ReportImpl() throws RemoteException {
        super();
        this.reportGenerator = new ReportGenerator();
    }

    @Override
    public List<String[]> getReportData(ReportType type, String startDate, String endDate) throws RemoteException {
        switch (type) {
            case MONTHLY_APPOINTMENTS:
                return reportGenerator.generateMonthlyAppointments(startDate, endDate);
            case DOCTOR_CONSULTATIONS:
                return reportGenerator.generateDoctorConsultations(startDate, endDate);
            case PATIENT_VISITS:
                return reportGenerator.generatePatientVisits(startDate, endDate);
            default:
                return new ArrayList<>();
        }
    }

    @Override
    public Report generateReport(int adminID, ReportType type, String startDate, String endDate) throws RemoteException {
        try {
            List<String[]> data = getReportData(type, startDate, endDate);
            StringBuilder resultData = new StringBuilder();
            for (String[] row : data) {
                resultData.append(String.join(",", row)).append("\n");
            }

            String parameters = startDate + " to " + endDate;

            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO REPORTS (AdminID, ReportType, GeneratedDate, Parameters, ResultData) "
                    + "VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, adminID);
            ps.setString(2, type.name());
            ps.setString(3, parameters);
            ps.setString(4, resultData.toString());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            int reportID = -1;
            if (keys.next()) {
                reportID = keys.getInt(1);
            }
            keys.close();
            ps.close();

            AuditLogger.log(adminID, "GENERATE_REPORT",
                    "Report type: " + type.name() + ", period: " + parameters);

            Report report = new Report(reportID, adminID, type, DateUtils.getCurrentDateTime());
            report.setParameters(parameters);
            report.setResultData(resultData.toString());
            return report;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to generate report: " + e.getMessage());
        }
    }

    @Override
    public List<Report> getReportHistory(int adminID) throws RemoteException {
        List<Report> reports = new ArrayList<>();
        try {
            Connection conn = DerbyConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT ReportID, AdminID, ReportType, GeneratedDate, Parameters, ResultData "
                    + "FROM REPORTS WHERE AdminID = ? ORDER BY GeneratedDate DESC");
            ps.setInt(1, adminID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Report report = new Report(
                        rs.getInt("ReportID"),
                        rs.getInt("AdminID"),
                        ReportType.valueOf(rs.getString("ReportType")),
                        rs.getTimestamp("GeneratedDate").toString());
                report.setParameters(rs.getString("Parameters"));
                report.setResultData(rs.getString("ResultData"));
                reports.add(report);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Failed to get report history: " + e.getMessage());
        }
        return reports;
    }
}
