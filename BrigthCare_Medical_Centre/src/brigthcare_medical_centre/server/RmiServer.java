package brigthcare_medical_centre.server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import brigthcare_medical_centre.admin.AdminImpl;
import brigthcare_medical_centre.auth.AuthenticationImpl;
import brigthcare_medical_centre.common.AdminInterface;
import brigthcare_medical_centre.common.AuthenticationInterface;
import brigthcare_medical_centre.common.ReportInterface;
import brigthcare_medical_centre.database.DatabaseSetup;
import brigthcare_medical_centre.report.ReportImpl;
import brigthcare_medical_centre.util.Constants;

public class RmiServer {

    public void start() {
        try {
            System.setProperty("java.rmi.server.hostname", Constants.RMI_HOST);

            DatabaseSetup.initialize();

            LocateRegistry.createRegistry(Constants.RMI_PORT);
            System.out.println("RMI registry started on port " + Constants.RMI_PORT);

            AuthenticationInterface authService = new AuthenticationImpl();
            Naming.rebind(Constants.AUTH_SERVICE, authService);
            System.out.println("AuthenticationService bound.");

            AdminInterface adminService = new AdminImpl();
            Naming.rebind(Constants.ADMIN_SERVICE, adminService);
            System.out.println("AdminService bound.");

            ReportInterface reportService = new ReportImpl();
            Naming.rebind(Constants.REPORT_SERVICE, reportService);
            System.out.println("ReportService bound.");

            System.out.println("BrightCare Medical Centre RMI Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
