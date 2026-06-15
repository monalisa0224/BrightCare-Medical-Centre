package brigthcare_medical_centre.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import brigthcare_medical_centre.auth.User;

public interface AuthenticationInterface extends Remote {
    User login(String username, String password) throws RemoteException;
}
