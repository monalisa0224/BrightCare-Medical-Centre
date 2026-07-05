package brigthcare_medical_centre.doctor;

import java.io.Serializable;

public class ConsultationNote implements Serializable {
    private static final long serialVersionUID = 1L;

    private int noteID;
    private int appointmentID;
    private int doctorID;
    private String patientUsername;
    private String consultationDate;
    private String diagnosis;
    private String treatment;
    private String prescription;
    private String notes;
    private String createdDate;

    public ConsultationNote() {}

    public ConsultationNote(int noteID, int appointmentID, int doctorID, String patientUsername,
                            String consultationDate, String diagnosis, String treatment,
                            String prescription, String notes, String createdDate) {
        this.noteID = noteID;
        this.appointmentID = appointmentID;
        this.doctorID = doctorID;
        this.patientUsername = patientUsername;
        this.consultationDate = consultationDate;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.prescription = prescription;
        this.notes = notes;
        this.createdDate = createdDate;
    }

    public int getNoteID() { return noteID; }
    public void setNoteID(int noteID) { this.noteID = noteID; }

    public int getAppointmentID() { return appointmentID; }
    public void setAppointmentID(int appointmentID) { this.appointmentID = appointmentID; }

    public int getDoctorID() { return doctorID; }
    public void setDoctorID(int doctorID) { this.doctorID = doctorID; }

    public String getPatientUsername() { return patientUsername; }
    public void setPatientUsername(String patientUsername) { this.patientUsername = patientUsername; }

    public String getConsultationDate() { return consultationDate; }
    public void setConsultationDate(String consultationDate) { this.consultationDate = consultationDate; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}
