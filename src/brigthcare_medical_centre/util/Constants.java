package brigthcare_medical_centre.util;

public class Constants {
    public static final int RMI_PORT = Integer.getInteger("brightcare.rmi.port", 1099);
    public static final String RMI_HOST = System.getProperty("brightcare.rmi.host", "localhost");
    public static final String DB_URL = System.getProperty("brightcare.db.url", "jdbc:derby:BrightCareDB;create=true");
    public static final String DB_USER = "";
    public static final String DB_PASSWORD = "";
    public static final boolean SSL_ENABLED = Boolean.parseBoolean(
            System.getProperty("brightcare.ssl.enabled", "false"));
    public static final String AUTH_SERVICE = "AuthenticationService";
    public static final String ADMIN_SERVICE = "AdminService";
    public static final String REPORT_SERVICE = "ReportService";
    public static final String DEFAULT_ADMIN_USER = "admin";
    public static final String DEFAULT_ADMIN_PASS = "admin123";
    public static final String PATIENT_SERVICE = "PatientService";
    public static final String DOCTOR_SERVICE = "DoctorService";
    public static final String RECEPTIONIST_SERVICE = "ReceptionistService";
    
}
