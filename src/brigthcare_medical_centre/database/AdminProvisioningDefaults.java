package brigthcare_medical_centre.database;

public final class AdminProvisioningDefaults {

    private AdminProvisioningDefaults() {
    }

    public static String doctorName(String username) {
        return username;
    }

    public static String receptionistName(String username) {
        return username;
    }
}
