package brigthcare_medical_centre.admin;

import java.io.Serializable;

public class Admin implements Serializable {
    private static final long serialVersionUID = 1L;
    private int adminID;
    private String name;
    private String role;

    public Admin() {}

    public Admin(int adminID, String name, String role) {
        this.adminID = adminID;
        this.name = name;
        this.role = role;
    }

    public int getAdminID() { return adminID; }
    public void setAdminID(int adminID) { this.adminID = adminID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
