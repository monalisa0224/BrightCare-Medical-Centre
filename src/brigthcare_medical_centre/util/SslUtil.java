package brigthcare_medical_centre.util;

import java.rmi.RemoteException;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class SslUtil {

    public static void configureSSL() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
