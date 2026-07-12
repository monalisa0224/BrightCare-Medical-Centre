package brigthcare_medical_centre.server;

import brigthcare_medical_centre.common.PatientInfo;
import brigthcare_medical_centre.common.ReceptionistInterface;
import brigthcare_medical_centre.database.ReceptionistDB;
import brigthcare_medical_centre.database.DerbyConnection;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class ReceptionistImpl extends UnicastRemoteObject implements ReceptionistInterface {

    private ReceptionistDB receptionistDB;

    public ReceptionistImpl() throws RemoteException {
        super();
        receptionistDB = new ReceptionistDB();
    }

    @Override
    public boolean registerPatient(PatientInfo patientInfo) throws RemoteException {
        System.out.println("[Server] Receptionist is registering patient: " + patientInfo.getUsername());
        
        boolean success = receptionistDB.registerPatient(patientInfo);
        
        // Audit Log Update
        if (success) {
            try (Connection conn = DerbyConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO LOGS (Action, Details) VALUES (?, ?)")) {
                
                ps.setString(1, "REGISTER_PATIENT");
                ps.setString(2, "Registered new patient: " + patientInfo.getUsername());
                ps.executeUpdate();
                
            } catch (Exception e) {
                System.err.println("Failed to write to Audit Log: " + e.getMessage());
            }
        }
        return success;
    }

    @Override
    public List<PatientInfo> getAllPatients() throws RemoteException {
        return receptionistDB.getAllPatients();
    }

    @Override
    public List<PatientInfo> searchPatient(String keyword) throws RemoteException {
        return receptionistDB.searchPatient(keyword);
    }

    @Override
    public boolean updatePatient(PatientInfo patientInfo) throws RemoteException {
        return receptionistDB.updatePatient(patientInfo);
    }

    @Override
    public boolean deletePatient(int id) throws RemoteException {
        return receptionistDB.deletePatient(id);
    }
}