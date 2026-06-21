package brigthcare_medical_centre.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ServerDriver {

    public static void main(String[] args) {
        System.out.println("Starting BrightCare Medical Centre Server...");
        RmiServer server = new RmiServer();
        server.start();

        System.out.println("Press Enter to stop the server...");
        try {
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Shutting down server...");
        System.exit(0);
    }
}
