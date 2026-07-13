package brigthcare_medical_centre.auth;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for User entity and UserRole enum
 */
public class UserTest {

    @Test
    public void testUserCreation() {
        User user = new User(1, "testuser", UserRole.PATIENT);
        assertEquals(1, user.getUserID());
        assertEquals("testuser", user.getUsername());
        assertEquals(UserRole.PATIENT, user.getRole());
    }

    @Test
    public void testUserSettersAndGetters() {
        User user = new User();
        user.setUserID(10);
        user.setUsername("testuser");
        user.setPasswordHash("hashedpassword");
        user.setRole(UserRole.DOCTOR);
        user.setCreatedDate("2026-01-15");

        assertEquals(10, user.getUserID());
        assertEquals("testuser", user.getUsername());
        assertEquals("hashedpassword", user.getPasswordHash());
        assertEquals(UserRole.DOCTOR, user.getRole());
        assertEquals("2026-01-15", user.getCreatedDate());
    }

    @Test
    public void testUserRoleValues() {
        assertEquals(4, UserRole.values().length);
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
        assertEquals(UserRole.DOCTOR, UserRole.valueOf("DOCTOR"));
        assertEquals(UserRole.RECEPTIONIST, UserRole.valueOf("RECEPTIONIST"));
        assertEquals(UserRole.PATIENT, UserRole.valueOf("PATIENT"));
    }
}