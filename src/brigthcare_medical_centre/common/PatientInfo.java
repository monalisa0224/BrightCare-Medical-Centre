package brigthcare_medical_centre.common;

import java.io.Serializable;

public class PatientInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id; 
    private String username;
    private String contactNumber;
    private String address;
    private String password; // Used only during registration

    // Constructor for registering a NEW patient
    public PatientInfo(String username, String password, String contactNumber, String address) {
        this.username = username;
        this.password = password;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    // Constructor for fetching EXISTING patients (Password is hidden)
    public PatientInfo(int id, String username, String contactNumber, String address) {
        this.id = id;
        this.username = username;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}