package brigthcare_medical_centre.report;

import brigthcare_medical_centre.common.ReportInterface;
import brigthcare_medical_centre.report.ReportType;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Integration tests for Report module using actual RMI server
 * Requires RMI server running on localhost:1099
 */
public class ReportIntegrationTest {

    private static ReportInterface reportService;
    private static final String RMI_URL = "rmi://localhost:1099/";

    @BeforeClass
    public static void setUpClass() {
        try {
            ReportInterface report = (ReportInterface) Naming.lookup(RMI_URL + "ReportService");
            reportService = report;
            System.out.println("Connected to ReportService on " + RMI_URL + "ReportService");
        } catch (Exception e) {
            System.err.println("Could not connect to RMI server. Make sure start_server.bat is running.");
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testGetMonthlyAppointmentsReport() throws RemoteException {
        assertNotNull("Report service should be available", reportService);
        
        List<String[]> data = reportService.getReportData(ReportType.MONTHLY_APPOINTMENTS, "2026-01-01", "2026-01-31");
        assertNotNull("Report data should not be null", data);
        
        // Should have column headers
        if (!data.isEmpty()) {
            String[] header = data.get(0);
            assertEquals("Should have 5 columns", 5, header.length);
            System.out.println("Monthly Appointments Report Header: " + String.join(" | ", header));
        }
    }

    @Test
    public void testGetDoctorConsultationsReport() throws RemoteException {
        assertNotNull("Report service should be available", reportService);
        
        List<String[]> data = reportService.getReportData(ReportType.DOCTOR_CONSULTATIONS, "2026-01-01", "2026-01-31");
        assertNotNull("Report data should not be null", data);
        
        if (!data.isEmpty()) {
            String[] header = data.get(0);
            assertEquals("Should have 3 columns", 3, header.length);
            System.out.println("Doctor Consultations Report Header: " + String.join(" | ", header));
        }
    }

    @Test
    public void testGetPatientVisitsReport() throws RemoteException {
        assertNotNull("Report service should be available", reportService);
        
        List<String[]> data = reportService.getReportData(ReportType.PATIENT_VISITS, "2026-01-01", "2026-12-31");
        assertNotNull("Report data should not be null", data);
        
        if (!data.isEmpty()) {
            String[] header = data.get(0);
            assertEquals("Should have 5 columns", 5, header.length);
            System.out.println("Patient Visits Report Header: " + String.join(" | ", header));
        }
    }

    @Test
    public void testGenerateReport() throws RemoteException {
        assertNotNull("Report service should be available", reportService);
        
        // Generate a report as admin (userID 1)
        int adminId = 1;
        Report report = reportService.generateReport(adminId, ReportType.MONTHLY_APPOINTMENTS, "2026-01-01", "2026-01-31");
        
        assertNotNull("Generated report should not be null", report);
        assertEquals(adminId, report.getAdminID());
        assertEquals(ReportType.MONTHLY_APPOINTMENTS, report.getReportType());
        assertNotNull("Report should have generated date", report.getGeneratedDate());
        assertNotNull("Report should have parameters", report.getParameters());
        
        System.out.println("Generated Report: ID=" + report.getReportID() + ", Type=" + report.getReportType());
    }

    @Test
    public void testGetReportHistory() throws RemoteException {
        assertNotNull("Report service should be available", reportService);
        
        List<Report> history = reportService.getReportHistory(1); // admin user
        assertNotNull("Report history should not be null", history);
        
        for (Report report : history) {
            System.out.println("Report: ID=" + report.getReportID() + ", Type=" + report.getReportType() + ", Date=" + report.getGeneratedDate());
        }
    }
}