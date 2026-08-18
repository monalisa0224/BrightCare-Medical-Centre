package brigthcare_medical_centre.common;

import java.io.Serializable;

public class PatientInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String icPassportNumber;
    private String medicalRecordId;
    private String contactNumber;
    private String address;
    private String password; // Used only during registration

    // Constructor for registering a NEW patient
    public PatientInfo(String username, String password, String firstName, String lastName,
            String icPassportNumber, String contactNumber, String address) {
        this(username, password, firstName, lastName, icPassportNumber, null, contactNumber, address);
    }

    /** Retained for compatibility; receptionist registration generates this value on the server. */
    public PatientInfo(String username, String password, String firstName, String lastName,
            String icPassportNumber, String medicalRecordId, String contactNumber, String address) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassportNumber = icPassportNumber;
        this.medicalRecordId = medicalRecordId;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    /** Retained for callers compiled against the original registration DTO. */
    public PatientInfo(String username, String password, String contactNumber, String address) {
        this(username, password, null, null, null, null, contactNumber, address);
    }

    // Constructor for fetching EXISTING patients (Password is hidden)
    public PatientInfo(int id, String username, String firstName, String lastName,
            String icPassportNumber, String medicalRecordId, String contactNumber, String address) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassportNumber = icPassportNumber;
        this.medicalRecordId = medicalRecordId;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    /** Retained for callers compiled against the original patient-list DTO. */
    public PatientInfo(int id, String username, String contactNumber, String address) {
        this(id, username, null, null, null, null, contactNumber, address);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return id; }
    public void setPatientId(int patientId) { this.id = patientId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getIcPassportNumber() { return icPassportNumber; }
    public void setIcPassportNumber(String icPassportNumber) { this.icPassportNumber = icPassportNumber; }

    public String getMedicalRecordId() { return medicalRecordId; }
    public void setMedicalRecordId(String medicalRecordId) { this.medicalRecordId = medicalRecordId; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
