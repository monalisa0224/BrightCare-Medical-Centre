package brigthcare_medical_centre.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import brigthcare_medical_centre.admin.AdminImpl;
import brigthcare_medical_centre.auth.AuthenticationImpl;
import brigthcare_medical_centre.common.AdminInterface;
import brigthcare_medical_centre.common.AuthenticationInterface;
import brigthcare_medical_centre.common.ReportInterface;
import brigthcare_medical_centre.database.DatabaseSetup;
import brigthcare_medical_centre.report.ReportImpl;
import brigthcare_medical_centre.util.Constants;
import brigthcare_medical_centre.util.SslUtil;
import brigthcare_medical_centre.common.PatientInterface;
import brigthcare_medical_centre.server.PatientImpl;
import brigthcare_medical_centre.common.DoctorInterface;
import brigthcare_medical_centre.server.DoctorImpl;
import brigthcare_medical_centre.common.ReceptionistInterface;
import brigthcare_medical_centre.server.ReceptionistImpl;

public class RmiServer {

    public void start() {
        try {
            System.setProperty("java.rmi.server.hostname", Constants.RMI_HOST);

            DatabaseSetup.initialize();

            Registry plainRegistry = LocateRegistry.createRegistry(Constants.RMI_PORT);
            System.out.println("Plain RMI registry started on port " + Constants.RMI_PORT);

            AuthenticationInterface authService = new AuthenticationImpl();
            plainRegistry.rebind(Constants.AUTH_SERVICE, authService);
            System.out.println("AuthenticationService bound on plain RMI.");

            AdminInterface adminService = new AdminImpl();
            plainRegistry.rebind(Constants.ADMIN_SERVICE, adminService);
            System.out.println("AdminService bound on plain RMI.");

            ReportInterface reportService = new ReportImpl();
            plainRegistry.rebind(Constants.REPORT_SERVICE, reportService);
            System.out.println("ReportService bound on plain RMI.");
            
            PatientInterface patientService = new PatientImpl();
            plainRegistry.rebind(Constants.PATIENT_SERVICE, patientService);
            System.out.println("PatientService bound on plain RMI.");

            DoctorInterface doctorService = new DoctorImpl();
            plainRegistry.rebind(Constants.DOCTOR_SERVICE, doctorService);
            System.out.println("DoctorService bound on plain RMI.");

            if (Constants.SSL_ENABLED) {
                SslUtil.validateServerSSL();
                Registry receptionistTlsRegistry = LocateRegistry.createRegistry(
                        Constants.RECEPTIONIST_RMI_PORT,
                        SslUtil.clientSocketFactory(),
                        SslUtil.serverSocketFactory());
                System.out.println("TLS RMI registry started for receptionist on port "
                        + Constants.RECEPTIONIST_RMI_PORT);

                AuthenticationInterface receptionistAuthService = new AuthenticationImpl(true);
                receptionistTlsRegistry.rebind(Constants.AUTH_SERVICE, receptionistAuthService);
                System.out.println("AuthenticationService bound on receptionist TLS RMI.");

                ReceptionistInterface receptionistService = new ReceptionistImpl(true);
                receptionistTlsRegistry.rebind(Constants.RECEPTIONIST_SERVICE, receptionistService);
                System.out.println("ReceptionistService bound on receptionist TLS RMI.");
            } else {
                ReceptionistInterface receptionistService = new ReceptionistImpl();
                plainRegistry.rebind(Constants.RECEPTIONIST_SERVICE, receptionistService);
                System.out.println("ReceptionistService bound on plain RMI because SSL is disabled.");
            }

            System.out.println("BrightCare Medical Centre RMI Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
