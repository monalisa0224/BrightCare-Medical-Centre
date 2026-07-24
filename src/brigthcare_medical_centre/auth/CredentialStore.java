package brigthcare_medical_centre.auth;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class CredentialStore implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE_NAME = "brightcare_login.ser";
    private static final long EXPIRY_MILLIS = TimeUnit.DAYS.toMillis(7);

    private String username;
    private String password;
    private String role;
    private long timestamp;

    private CredentialStore(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.timestamp = System.currentTimeMillis();
    }

    public static void save(String username, String password, String role) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FILE_NAME))) {
            oos.writeObject(new CredentialStore(username, password, role));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CredentialStore load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(FILE_NAME))) {
            return (CredentialStore) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public static void clear() {
        new File(FILE_NAME).delete();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > EXPIRY_MILLIS;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}