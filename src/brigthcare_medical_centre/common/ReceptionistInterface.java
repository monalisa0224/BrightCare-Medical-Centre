package brigthcare_medical_centre.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ReceptionistInterface extends Remote {

    boolean registerPatient(PatientInfo patientInfo) throws RemoteException;

    List<PatientInfo> getAllPatients() throws RemoteException;

    List<PatientInfo> searchPatient(String keyword) throws RemoteException;

    boolean updatePatient(PatientInfo patientInfo) throws RemoteException;

    boolean deletePatient(int id) throws RemoteException;
}