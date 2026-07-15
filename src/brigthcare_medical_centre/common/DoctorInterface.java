package brigthcare_medical_centre.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DoctorInterface extends Remote {

    List<String[]> getPendingAppointments(int doctorId) throws RemoteException;

    boolean acceptAppointment(int appointmentId) throws RemoteException;

    boolean rejectAppointment(int appointmentId) throws RemoteException;

    List<String[]> getDoctorAppointments(int doctorId) throws RemoteException;

    boolean cancelAppointmentByDoctor(int appointmentId) throws RemoteException;

    boolean rescheduleAppointment(int appointmentId, String newDate, String newTime) throws RemoteException;

    boolean updateConsultationNotes(int appointmentId, int doctorId,
            String patientUsername, String diagnosis, String treatment,
            String prescription, String notes) throws RemoteException;

    String[] getConsultationNotes(int appointmentId) throws RemoteException;

    List<String[]> getPatientHistory(String patientUsername) throws RemoteException;

    List<String[]> getDistinctPatientsForDoctor(int doctorId) throws RemoteException;

    List<String[]> getDoctorTimetable(int doctorId, String weekStartDate) throws RemoteException;

    boolean updateDoctorSchedule(int doctorId, String date, String slot, boolean isAvailable) throws RemoteException;

    List<String> getAvailableSlots(int doctorId, String date) throws RemoteException;

    String[] getDoctorProfile(int doctorId) throws RemoteException;

    boolean updateDoctorProfile(int doctorId, String doctorName, String specialization) throws RemoteException;

    boolean changePassword(int doctorId, String oldPassword, String newPassword) throws RemoteException;

    boolean updateContactInfo(int doctorId, String contactNumber) throws RemoteException;

    int[] getTodaySummary(int doctorId) throws RemoteException;

    List<String[]> getTodayAppointments(int doctorId) throws RemoteException;

    int getDoctorIdByUsername(String username) throws RemoteException;
}
