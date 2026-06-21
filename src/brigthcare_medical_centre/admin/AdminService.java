package brigthcare_medical_centre.admin;

public class AdminService {

    private final AdminImpl adminImpl;

    public AdminService(AdminImpl adminImpl) {
        this.adminImpl = adminImpl;
    }

    public boolean validateAdminAccess(int adminID) {
        try {
            return adminImpl.getAdminById(adminID) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
