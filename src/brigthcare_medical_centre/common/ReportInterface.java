package brigthcare_medical_centre.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import brigthcare_medical_centre.report.Report;
import brigthcare_medical_centre.report.ReportType;

public interface ReportInterface extends Remote {
    Report generateReport(int adminID, ReportType type, String startDate, String endDate) throws RemoteException;
    List<Report> getReportHistory(int adminID) throws RemoteException;
    List<String[]> getReportData(ReportType type, String startDate, String endDate) throws RemoteException;
}
