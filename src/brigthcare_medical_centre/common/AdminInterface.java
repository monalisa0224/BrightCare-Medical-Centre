package brigthcare_medical_centre.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import brigthcare_medical_centre.admin.Admin;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.auth.UserRole;

public interface AdminInterface extends Remote {
    Admin getAdminById(int adminID) throws RemoteException;
    List<String[]> viewLogs() throws RemoteException;
    boolean registerUser(String username, String password, UserRole role) throws RemoteException;
    boolean updateUserRole(int userID, UserRole newRole) throws RemoteException;
    boolean deleteUser(int userID) throws RemoteException;
    List<String[]> getAllUsers() throws RemoteException;
}
