package brigthcare_medical_centre.server;

import brigthcare_medical_centre.common.DoctorInterface;
import brigthcare_medical_centre.database.DoctorDB;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class DoctorImpl extends UnicastRemoteObject implements DoctorInterface {

    private DoctorDB doctorDB;

    public DoctorImpl() throws RemoteException {
        super();
        doctorDB = new DoctorDB();
    }

    @Override
    public List<String[]> getPendingAppointments(int doctorId) throws RemoteException {
        return doctorDB.getPendingAppointments(doctorId);
    }

    @Override
    public boolean acceptAppointment(int appointmentId) throws RemoteException {
        return doctorDB.acceptAppointment(appointmentId);
    }

    @Override
    public boolean rejectAppointment(int appointmentId) throws RemoteException {
        return doctorDB.rejectAppointment(appointmentId);
    }

    @Override
    public List<String[]> getDoctorAppointments(int doctorId) throws RemoteException {
        return doctorDB.getDoctorAppointments(doctorId);
    }

    @Override
    public boolean cancelAppointmentByDoctor(int appointmentId) throws RemoteException {
        return doctorDB.cancelAppointmentByDoctor(appointmentId);
    }

    @Override
    public boolean rescheduleAppointment(int appointmentId, String newDate, String newTime) throws RemoteException {
        return doctorDB.rescheduleAppointment(appointmentId, newDate, newTime);
    }

    @Override
    public boolean updateConsultationNotes(int appointmentId, int doctorId,
            String patientUsername, String diagnosis, String treatment,
            String prescription, String notes) throws RemoteException {
        return doctorDB.updateConsultationNotes(appointmentId, doctorId, patientUsername,
                diagnosis, treatment, prescription, notes);
    }

    @Override
    public String[] getConsultationNotes(int appointmentId) throws RemoteException {
        return doctorDB.getConsultationNotes(appointmentId);
    }

    @Override
    public List<String[]> getPatientHistory(String patientUsername) throws RemoteException {
        return doctorDB.getPatientHistory(patientUsername);
    }

    @Override
    public List<String[]> getDistinctPatientsForDoctor(int doctorId) throws RemoteException {
        return doctorDB.getDistinctPatientsForDoctor(doctorId);
    }

    @Override
    public List<String[]> getDoctorTimetable(int doctorId, String weekStartDate) throws RemoteException {
        return doctorDB.getDoctorTimetable(doctorId, weekStartDate);
    }

    @Override
    public boolean updateDoctorSchedule(int doctorId, String date, String slot, boolean isAvailable) throws RemoteException {
        return doctorDB.updateDoctorSchedule(doctorId, date, slot, isAvailable);
    }

    @Override
    public List<String> getAvailableSlots(int doctorId, String date) throws RemoteException {
        return doctorDB.getAvailableSlots(doctorId, date);
    }

    @Override
    public String[] getDoctorProfile(int doctorId) throws RemoteException {
        return doctorDB.getDoctorProfile(doctorId);
    }

    @Override
    public boolean updateDoctorProfile(int doctorId, String doctorName, String specialization) throws RemoteException {
        return doctorDB.updateDoctorProfile(doctorId, doctorName, specialization);
    }

    @Override
    public boolean changePassword(int doctorId, String oldPassword, String newPassword) throws RemoteException {
        return doctorDB.changePassword(doctorId, oldPassword, newPassword);
    }

    @Override
    public boolean updateContactInfo(int doctorId, String contactNumber) throws RemoteException {
        return doctorDB.updateContactInfo(doctorId, contactNumber);
    }

    @Override
    public int[] getTodaySummary(int doctorId) throws RemoteException {
        return doctorDB.getTodaySummary(doctorId);
    }

    @Override
    public List<String[]> getTodayAppointments(int doctorId) throws RemoteException {
        return doctorDB.getTodayAppointments(doctorId);
    }

    @Override
    public int getDoctorIdByUsername(String username) throws RemoteException {
        return doctorDB.getDoctorIdByUsername(username);
    }
}
