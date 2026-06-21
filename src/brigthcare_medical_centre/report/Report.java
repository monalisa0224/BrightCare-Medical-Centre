package brigthcare_medical_centre.report;

import java.io.Serializable;

public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    private int reportID;
    private int adminID;
    private ReportType reportType;
    private String generatedDate;
    private String parameters;
    private String resultData;

    public Report() {}

    public Report(int reportID, int adminID, ReportType reportType, String generatedDate) {
        this.reportID = reportID;
        this.adminID = adminID;
        this.reportType = reportType;
        this.generatedDate = generatedDate;
    }

    public int getReportID() { return reportID; }
    public void setReportID(int reportID) { this.reportID = reportID; }

    public int getAdminID() { return adminID; }
    public void setAdminID(int adminID) { this.adminID = adminID; }

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public String getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(String generatedDate) { this.generatedDate = generatedDate; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public String getResultData() { return resultData; }
    public void setResultData(String resultData) { this.resultData = resultData; }
}
