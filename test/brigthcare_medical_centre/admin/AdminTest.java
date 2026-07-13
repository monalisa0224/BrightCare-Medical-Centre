/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package brigthcare_medical_centre.admin;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author User
 */
public class AdminTest {
    
    public AdminTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getAdminID method, of class Admin.
     */
    @Test
    public void testGetAdminID() {
        System.out.println("getAdminID");
        Admin instance = new Admin();
        int expResult = 0;
        int result = instance.getAdminID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAdminID method, of class Admin.
     */
    @Test
    public void testSetAdminID() {
        System.out.println("setAdminID");
        int adminID = 0;
        Admin instance = new Admin();
        instance.setAdminID(adminID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class Admin.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        Admin instance = new Admin();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class Admin.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        Admin instance = new Admin();
        instance.setName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRole method, of class Admin.
     */
    @Test
    public void testGetRole() {
        System.out.println("getRole");
        Admin instance = new Admin();
        String expResult = "";
        String result = instance.getRole();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRole method, of class Admin.
     */
    @Test
    public void testSetRole() {
        System.out.println("setRole");
        String role = "";
        Admin instance = new Admin();
        instance.setRole(role);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
