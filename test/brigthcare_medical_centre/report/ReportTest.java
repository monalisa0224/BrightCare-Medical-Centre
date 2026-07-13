package brigthcare_medical_centre.report;

import brigthcare_medical_centre.report.Report;
import brigthcare_medical_centre.report.ReportType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Report entity and ReportType enum
 */
public class ReportTest {

    @Test
    public void testReportCreation() {
        Report report = new Report(1, 1, ReportType.MONTHLY_APPOINTMENTS, "2026-01-15");
        assertEquals(1, report.getReportID());
        assertEquals(1, report.getAdminID());
        assertEquals(ReportType.MONTHLY_APPOINTMENTS, report.getReportType());
        assertEquals("2026-01-15", report.getGeneratedDate());
    }

    @Test
    public void testReportSettersAndGetters() {
        Report report = new Report();
        report.setReportID(10);
        report.setAdminID(5);
        report.setReportType(ReportType.DOCTOR_CONSULTATIONS);
        report.setGeneratedDate("2026-06-01");
        report.setParameters("month=1&year=2026");
        report.setResultData("Report data here");

        assertEquals(10, report.getReportID());
        assertEquals(5, report.getAdminID());
        assertEquals(ReportType.DOCTOR_CONSULTATIONS, report.getReportType());
        assertEquals("2026-06-01", report.getGeneratedDate());
        assertEquals("month=1&year=2026", report.getParameters());
        assertEquals("Report data here", report.getResultData());
    }

    @Test
    public void testReportTypeValues() {
        assertEquals(3, ReportType.values().length);
        assertEquals(ReportType.MONTHLY_APPOINTMENTS, ReportType.valueOf("MONTHLY_APPOINTMENTS"));
        assertEquals(ReportType.DOCTOR_CONSULTATIONS, ReportType.valueOf("DOCTOR_CONSULTATIONS"));
        assertEquals(ReportType.PATIENT_VISITS, ReportType.valueOf("PATIENT_VISITS"));
    }
}