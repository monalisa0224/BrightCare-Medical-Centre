package brigthcare_medical_centre.util;

import java.rmi.server.RMISocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class SslUtil {

    private static boolean configured;

    public static void configureSSL() {
        if (configured) {
            return;
        }

        if (isBlank(System.getProperty("javax.net.ssl.keyStore"))
                || isBlank(System.getProperty("javax.net.ssl.keyStorePassword"))
                || isBlank(System.getProperty("javax.net.ssl.trustStore"))
                || isBlank(System.getProperty("javax.net.ssl.trustStorePassword"))) {
            throw new IllegalStateException(
                    "SSL is enabled but the required keyStore/trustStore properties are missing.");
        }

        try {
            RMISocketFactory.setSocketFactory(new RMISocketFactory() {
                @Override
                public Socket createSocket(String host, int port) throws IOException {
                    return SSLSocketFactory.getDefault().createSocket(host, port);
                }

                @Override
                public ServerSocket createServerSocket(int port) throws IOException {
                    return SSLServerSocketFactory.getDefault().createServerSocket(port);
                }
            });
            configured = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to configure SSL for RMI", e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
