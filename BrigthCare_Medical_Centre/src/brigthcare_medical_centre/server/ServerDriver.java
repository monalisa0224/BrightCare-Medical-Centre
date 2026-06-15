package brigthcare_medical_centre.server;

public class ServerDriver {

    public static void main(String[] args) {
        System.out.println("Starting BrightCare Medical Centre Server...");
        RmiServer server = new RmiServer();
        server.start();
    }
}
