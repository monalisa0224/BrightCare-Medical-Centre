package brigthcare_medical_centre.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import brigthcare_medical_centre.admin.Admin;

public interface AdminInterface extends Remote {
    Admin getAdminById(int adminID) throws RemoteException;
    List<String[]> viewLogs() throws RemoteException;
}
