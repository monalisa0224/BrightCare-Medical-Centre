package brigthcare_medical_centre.database;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the SHA-256 password hashing used to store credentials.
 */
public class PasswordHashTest {

    @Test
    public void testHashIsHexOfExpectedLength() {
        String hash = DatabaseSetup.hashPassword("admin123");
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue("Expected 64 lowercase hex characters", hash.matches("[0-9a-f]{64}"));
    }

    @Test
    public void testHashIsDeterministic() {
        assertEquals(DatabaseSetup.hashPassword("secret"),
                DatabaseSetup.hashPassword("secret"));
    }

    @Test
    public void testDifferentInputsProduceDifferentHashes() {
        assertNotEquals(DatabaseSetup.hashPassword("password1"),
                DatabaseSetup.hashPassword("password2"));
    }

    @Test
    public void testHashDoesNotContainPlaintext() {
        String plain = "admin123";
        String hash = DatabaseSetup.hashPassword(plain);
        assertFalse(hash.equals(plain));
        assertFalse(hash.contains(plain));
    }

    @Test
    public void testEmptyPasswordStillHashes() {
        String hash = DatabaseSetup.hashPassword("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}