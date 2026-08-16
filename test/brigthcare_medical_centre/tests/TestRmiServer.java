package brigthcare_medical_centre.tests;

import brigthcare_medical_centre.admin.AdminImpl;
import brigthcare_medical_centre.auth.AuthenticationImpl;
import brigthcare_medical_centre.database.DatabaseSetup;
import brigthcare_medical_centre.report.ReportImpl;
import brigthcare_medical_centre.server.DoctorImpl;
import brigthcare_medical_centre.server.PatientImpl;
import brigthcare_medical_centre.server.ReceptionistImpl;
import brigthcare_medical_centre.util.Constants;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts an in-process RMI server against a fresh temporary Derby database so
 * the integration tests can run without a separately started server. The
 * registry is created on a dedicated test port so it never collides with a
 * production server on the default port.
 */
public final class TestRmiServer {

    public static final int RMI_PORT = 21099;
    public static final String RMI_URL = "rmi://localhost:" + RMI_PORT + "/";

    private static final String DB_NAME = "BrightCareDB_TestInt_" + System.currentTimeMillis();

    private static volatile boolean started;
    private static Registry registry;
    private static final List<Remote> exportedServices = new ArrayList<>();

    private TestRmiServer() {
    }

    public static synchronized void ensureStarted() throws RemoteException {
        if (started) {
            return;
        }
        start();
        started = true;
    }

    public static synchronized void stop() {
        if (!started) {
            return;
        }
        for (Remote service : exportedServices) {
            try {
                UnicastRemoteObject.unexportObject(service, true);
            } catch (Exception ignored) {
                // Best effort cleanup.
            }
        }
        exportedServices.clear();
        if (registry != null) {
            try {
                UnicastRemoteObject.unexportObject(registry, true);
            } catch (Exception ignored) {
                // Best effort cleanup.
            }
            registry = null;
        }
        // Shut down only the test database. Using a per-database shutdown keeps
        // the embedded Derby driver registered so a later test that restarts the
        // server can still open a connection.
        try {
            DriverManager.getConnection("jdbc:derby:" + DB_NAME + ";shutdown=true");
        } catch (Exception ignored) {
            // Derby throws SQLException on a successful shutdown.
        }
        started = false;
    }

    private static void start() throws RemoteException {
        System.setProperty("brightcare.rmi.port", String.valueOf(RMI_PORT));
        System.setProperty("brightcare.receptionist.rmi.port", String.valueOf(RMI_PORT + 1));
        System.setProperty("brightcare.db.url", "jdbc:derby:" + DB_NAME + ";create=true");

        DatabaseSetup.initialize();

        registry = LocateRegistry.createRegistry(RMI_PORT);

        bind(Constants.AUTH_SERVICE, new AuthenticationImpl());
        bind(Constants.ADMIN_SERVICE, new AdminImpl());
        bind(Constants.REPORT_SERVICE, new ReportImpl());
        bind(Constants.PATIENT_SERVICE, new PatientImpl());
        bind(Constants.DOCTOR_SERVICE, new DoctorImpl());
        bind(Constants.RECEPTIONIST_SERVICE, new ReceptionistImpl());

        System.out.println("Test RMI server started on port " + RMI_PORT);
    }

    private static void bind(String name, Remote service) throws RemoteException {
        registry.rebind(name, service);
        exportedServices.add(service);
    }
}