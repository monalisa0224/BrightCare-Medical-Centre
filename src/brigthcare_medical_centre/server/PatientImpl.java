package brigthcare_medical_centre.server;

import brigthcare_medical_centre.common.PatientInterface;
import brigthcare_medical_centre.database.PatientDB;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class PatientImpl extends UnicastRemoteObject implements PatientInterface {

    private PatientDB patientDB;

    public PatientImpl() throws RemoteException {
        super();
        patientDB = new PatientDB();
    }

    @Override
    public boolean updatePersonalInfo(String username, String contactNumber, String address)
            throws RemoteException {
        return patientDB.updatePersonalInfo(username, contactNumber, address);
    }

    @Override
    public boolean bookAppointment(String username, int doctorId, String date, String time)
            throws RemoteException {
        return patientDB.bookAppointment(username, doctorId, date, time);
    }

    @Override
    public boolean cancelAppointment(String username, int appointmentId) throws RemoteException {
        return patientDB.cancelAppointment(username, appointmentId);
    }

    @Override
    public List<String[]> viewAppointmentSchedules(String username) throws RemoteException {
        return patientDB.getUpcomingAppointments(username);
    }

    @Override
    public List<String[]> viewAppointmentHistory(String username) throws RemoteException {
        return patientDB.getAppointmentHistory(username);
    }

    @Override
    public List<String> checkDoctorAvailability(int doctorId, String date) throws RemoteException {
        return patientDB.getDoctorAvailability(doctorId, date);
    }
    
    @Override
    public List<String[]> getDoctors() throws RemoteException {
        return patientDB.getDoctors();
    }
    
    @Override
    public String[] getPatientProfile(String username) throws RemoteException {
        return patientDB.getPatientProfile(username);
    }
}