package brigthcare_medical_centre.util;

import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class SslUtil {

    public static void validateServerSSL() {
        if (isBlank(System.getProperty("javax.net.ssl.keyStore"))
                || isBlank(System.getProperty("javax.net.ssl.keyStorePassword"))) {
            throw new IllegalStateException(
                    "Receptionist TLS is enabled but server keyStore or keyStorePassword is missing.");
        }
    }

    public static void validateClientSSL() {
        if (isBlank(System.getProperty("javax.net.ssl.trustStore"))
                || isBlank(System.getProperty("javax.net.ssl.trustStorePassword"))) {
            throw new IllegalStateException(
                    "Receptionist TLS is enabled but client trustStore or trustStorePassword is missing.");
        }
    }

    public static RMIClientSocketFactory clientSocketFactory() {
        return new SslRMIClientSocketFactory();
    }

    public static RMIServerSocketFactory serverSocketFactory() {
        return new SslRMIServerSocketFactory();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
