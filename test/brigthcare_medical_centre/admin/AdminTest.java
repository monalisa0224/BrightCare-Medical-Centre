package brigthcare_medical_centre.admin;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Admin entity.
 */
public class AdminTest {

    @Test
    public void testGetAdminID() {
        Admin instance = new Admin();
        assertEquals(0, instance.getAdminID());

        Admin constructed = new Admin(7, "Dr. Lim", "ADMIN");
        assertEquals(7, constructed.getAdminID());
    }

    @Test
    public void testSetAdminID() {
        Admin instance = new Admin();
        instance.setAdminID(42);
        assertEquals(42, instance.getAdminID());
    }

    @Test
    public void testGetName() {
        Admin instance = new Admin();
        assertNull(instance.getName());

        Admin constructed = new Admin(1, "Boss", "ADMIN");
        assertEquals("Boss", constructed.getName());
    }

    @Test
    public void testSetName() {
        Admin instance = new Admin();
        instance.setName("Manager");
        assertEquals("Manager", instance.getName());
    }

    @Test
    public void testGetRole() {
        Admin instance = new Admin();
        assertNull(instance.getRole());

        Admin constructed = new Admin(1, "Boss", "ADMIN");
        assertEquals("ADMIN", constructed.getRole());
    }

    @Test
    public void testSetRole() {
        Admin instance = new Admin();
        instance.setRole("ADMIN");
        assertEquals("ADMIN", instance.getRole());
    }

    @Test
    public void testFullConstructor() {
        Admin admin = new Admin(99, "System Admin", "ADMIN");
        assertEquals(99, admin.getAdminID());
        assertEquals("System Admin", admin.getName());
        assertEquals("ADMIN", admin.getRole());
    }
}