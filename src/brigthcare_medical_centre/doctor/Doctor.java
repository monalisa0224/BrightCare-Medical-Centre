package brigthcare_medical_centre.doctor;

import java.io.Serializable;

public class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;

    private int doctorID;
    private String username;
    private String doctorName;
    private String specialization;
    private String contactNumber;

    public Doctor() {}

    public Doctor(int doctorID, String username, String doctorName, String specialization, String contactNumber) {
        this.doctorID = doctorID;
        this.username = username;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
    }

    public int getDoctorID() { return doctorID; }
    public void setDoctorID(int doctorID) { this.doctorID = doctorID; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}
