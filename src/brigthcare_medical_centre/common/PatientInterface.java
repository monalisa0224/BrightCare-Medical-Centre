package brigthcare_medical_centre.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PatientInterface extends Remote {
    
    // Update personal information
    boolean updatePersonalInfo(String username, String contactNumber, String address) throws RemoteException;
    
    // Book appointment
    boolean bookAppointment(String username, int doctorId, String date, String time) throws RemoteException;
    
    // Cancel appointment
    boolean cancelAppointment(String username, int appointmentId) throws RemoteException;
    
    // View upcoming appointment schedules
    List<String[]> viewAppointmentSchedules(String username) throws RemoteException;
    
    // View appointment history
    List<String[]> viewAppointmentHistory(String username) throws RemoteException;
    
    // Check doctor availability
    List<String> checkDoctorAvailability(int doctorId, String date) throws RemoteException;
    
    // Get list of all doctors
    List<String[]> getDoctors() throws RemoteException;
    
    String[] getPatientProfile(String username) throws RemoteException;
}