package brigthcare_medical_centre.admin;

import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.auth.UserRole;
import brigthcare_medical_centre.common.AdminInterface;
import brigthcare_medical_centre.server.RmiServer;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Integration tests for Admin module using actual RMI server
 * These tests require the RMI server to be running on localhost:1099
 * 
 * Run with: start_server.bat first, then run these tests
 */
public class AdminIntegrationTest {

    private static RmiServer rmiServer;
    private static AdminInterface adminService;
    private static final String RMI_URL = "rmi://localhost:1099/";

    @BeforeClass
    public static void setUpClass() {
        // Server should be started externally before running tests
        // Use start_server.bat before running these tests
        try {
            AdminInterface admin = (AdminInterface) Naming.lookup(RMI_URL + "AdminService");
            adminService = admin;
            System.out.println("Connected to AdminService on " + RMI_URL + "AdminService");
        } catch (Exception e) {
            System.err.println("Could not connect to RMI server. Make sure start_server.bat is running.");
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        // Cleanup if needed
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetAllUsers() throws RemoteException {
        assertNotNull("Admin service should be available", adminService);
        
        List<String[]> users = adminService.getAllUsers();
        assertNotNull("User list should not be null", users);
        assertFalse("User list should not be empty", users.isEmpty());
        
        // Verify structure: [UserID, Username, Role, CreatedDate, ActivityCount]
        for (String[] user : users) {
            assertEquals("Each user row should have 5 columns", 5, user.length);
            System.out.println("User: " + String.join(" | ", user));
        }
        
        // Should have at least the seeded users
        assertTrue("Should have at least 3 seeded users", users.size() >= 3);
    }

    @Test
    public void testRegisterUser() throws RemoteException {
        assertNotNull("Admin service should be available", adminService);
        
        String testUsername = "testuser_" + System.currentTimeMillis();
        String testPassword = "testpass123";
        
        // Test registration
        boolean result = adminService.registerUser(testUsername, testPassword, UserRole.PATIENT);
        assertTrue("User registration should succeed", result);
        
        // Verify user appears in list
        List<String[]> users = adminService.getAllUsers();
        boolean found = users.stream()
            .anyMatch(u -> testUsername.equals(u[1]) && "PATIENT".equals(u[2]));
        assertTrue("Newly registered user should appear in list", found);
        
        // Test duplicate registration fails
        boolean duplicateResult = adminService.registerUser(testUsername, "otherpass", UserRole.DOCTOR);
        assertFalse("Duplicate username registration should fail", duplicateResult);
    }

    @Test
    public void testUpdateUserRole() throws RemoteException {
        assertNotNull("Admin service should be available", adminService);
        
        String testUsername = "roleuser_" + System.currentTimeMillis();
        String testPassword = "testpass123";
        
        // Register a test user first
        boolean regResult = adminService.registerUser(testUsername, testPassword, UserRole.PATIENT);
        assertTrue("Registration should succeed", regResult);
        
        // Find the user ID
        List<String[]> users = adminService.getAllUsers();
        String userId = null;
        for (String[] user : users) {
            if (testUsername.equals(user[1])) {
                userId = user[0];
                break;
            }
        }
        assertNotNull("Should find registered user", userId);
        
        int userIdInt = Integer.parseInt(userId);
        
        // Test role update
        boolean updateResult = adminService.updateUserRole(userIdInt, UserRole.DOCTOR);
        assertTrue("Role update should succeed", updateResult);
        
        // Verify role changed
        List<String[]> updatedUsers = adminService.getAllUsers();
        for (String[] user : updatedUsers) {
            if (testUsername.equals(user[1])) {
                assertEquals("Role should be updated to DOCTOR", "DOCTOR", user[2]);
                return;
            }
        }
        fail("Updated user not found in list");
    }

    @Test
    public void testDeleteUser() throws RemoteException {
        assertNotNull("Admin service should be available", adminService);
        
        String testUsername = "deleteuser_" + System.currentTimeMillis();
        String testPassword = "testpass123";
        
        // Register a test user first
        boolean regResult = adminService.registerUser(testUsername, testPassword, UserRole.RECEPTIONIST);
        assertTrue("Registration should succeed", regResult);
        
        // Find the user ID
        List<String[]> users = adminService.getAllUsers();
        String userId = null;
        for (String[] user : users) {
            if (testUsername.equals(user[1])) {
                userId = user[0];
                break;
            }
        }
        assertNotNull("Should find registered user", userId);
        
        int userIdInt = Integer.parseInt(userId);
        
        // Test deletion
        boolean deleteResult = adminService.deleteUser(userIdInt);
        assertTrue("User deletion should succeed", deleteResult);
        
        // Verify user is gone
        List<String[]> usersAfterDelete = adminService.getAllUsers();
        boolean stillExists = usersAfterDelete.stream()
            .anyMatch(u -> testUsername.equals(u[1]));
        assertFalse("Deleted user should not appear in list", stillExists);
    }

    @Test
    public void testDeleteAdminUserFails() throws RemoteException {
        // Find admin user ID
        List<String[]> users = adminService.getAllUsers();
        String adminId = null;
        for (String[] user : users) {
            if ("ADMIN".equals(user[2])) {
                adminId = user[0];
                break;
            }
        }
        assertNotNull("Should have at least one admin user", adminId);
        
        int adminIdInt = Integer.parseInt(adminId);
        
        // Attempt to delete admin - should fail or throw exception
        boolean result = adminService.deleteUser(adminIdInt);
        // Implementation prevents admin deletion - should return false
        assertFalse("Admin user deletion should fail", result);
    }

    @Test
    public void testViewLogs() throws RemoteException {
        List<String[]> logs = adminService.viewLogs();
        assertNotNull("Logs list should not be null", logs);
        
        // Logs structure: [LogID, UserID, Username, Action, Timestamp, Details]
        for (String[] log : logs) {
            assertEquals("Each log entry should have 6 columns", 6, log.length);
            System.out.println("Log: " + String.join(" | ", log));
        }
    }
}