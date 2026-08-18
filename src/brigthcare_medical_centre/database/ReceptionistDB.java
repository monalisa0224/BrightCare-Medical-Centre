package brigthcare_medical_centre.database;

import brigthcare_medical_centre.common.PatientInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistDB {

    private Connection getConnection() throws SQLException {
        return DerbyConnection.getConnection();
    }

    public boolean registerPatient(PatientInfo p) {
        if (!hasRequiredRegistrationDetails(p)) {
            System.out.println("[DB Error] Registration failed: all patient details are required.");
            return false;
        }
        String userSql = "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)";
        String patientSql = "INSERT INTO PATIENTS (Username, FirstName, LastName, ICPassportNumber, MedicalRecordID, ContactNumber, Address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                lockPatientsTable(conn);
                if (icPassportExists(conn, p.getIcPassportNumber(), null)) {
                    conn.rollback();
                    return false;
                }
                String medicalRecordId = generateMedicalRecordId(conn);
                
                // Create Login Account
                try (PreparedStatement userPs = conn.prepareStatement(userSql)) {
                    userPs.setString(1, p.getUsername());
                    userPs.setString(2, DatabaseSetup.hashPassword(p.getPassword())); 
                    userPs.setString(3, "PATIENT");
                    userPs.executeUpdate();
                }

                // Insert Profile Data
                try (PreparedStatement ps = conn.prepareStatement(patientSql)) {
                    ps.setString(1, p.getUsername());
                    ps.setString(2, p.getFirstName());
                    ps.setString(3, p.getLastName());
                    ps.setString(4, p.getIcPassportNumber());
                    ps.setString(5, medicalRecordId);
                    ps.setString(6, p.getContactNumber());
                    ps.setString(7, p.getAddress());
                    ps.executeUpdate();
                }
                
                conn.commit();
                p.setMedicalRecordId(medicalRecordId);
                return true;
                
            } catch (SQLException e) {
                System.out.println("[DB Error] Registration failed. Rolling back... " + e.getMessage());
                conn.rollback();
                return false; 
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
        public List<PatientInfo> getAllPatients() {
        List<PatientInfo> list = new ArrayList<>();
        String sql = "SELECT PatientID, Username, FirstName, LastName, ICPassportNumber, MedicalRecordID, ContactNumber, Address FROM PATIENTS";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PatientInfo(
                    rs.getInt("PatientID"),
                    rs.getString("Username"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("ICPassportNumber"),
                    rs.getString("MedicalRecordID"),
                    rs.getString("ContactNumber"),
                    rs.getString("Address")
                ));
            }
            rs.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
        
    public List<PatientInfo> searchPatient(String keyword) {
        List<PatientInfo> list = new ArrayList<>();
        String sql = "SELECT PatientID, Username, FirstName, LastName, ICPassportNumber, MedicalRecordID, ContactNumber, Address "
                + "FROM PATIENTS WHERE LOWER(Username) LIKE ? OR LOWER(FirstName) LIKE ? OR LOWER(LastName) LIKE ? "
                + "OR LOWER(ICPassportNumber) LIKE ? OR LOWER(MedicalRecordID) LIKE ?";
                   
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            String pattern = "%" + keyword.toLowerCase() + "%";
            for (int index = 1; index <= 5; index++) {
                ps.setString(index, pattern);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PatientInfo(
                    rs.getInt("PatientID"),
                    rs.getString("Username"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("ICPassportNumber"),
                    rs.getString("MedicalRecordID"),
                    rs.getString("ContactNumber"),
                    rs.getString("Address")
                ));
            }
            rs.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updatePatient(PatientInfo p) {
        if (!hasRequiredProfileDetails(p)) {
            return false;
        }
        String sql = "UPDATE PATIENTS SET FirstName = ?, LastName = ?, ICPassportNumber = ?, "
                + "ContactNumber = ?, Address = ? WHERE Username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (icPassportExists(conn, p.getIcPassportNumber(), p.getUsername())) {
                return false;
            }
             
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getIcPassportNumber());
            ps.setString(4, p.getContactNumber());
            ps.setString(5, p.getAddress());
            ps.setString(6, p.getUsername());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean icPassportExists(Connection conn, String icPassportNumber,
            String excludedUsername) throws SQLException {
        String sql = "SELECT 1 FROM PATIENTS WHERE ICPassportNumber = ?"
                + (excludedUsername == null ? "" : " AND Username <> ?");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, icPassportNumber);
            if (excludedUsername != null) {
                ps.setString(2, excludedUsername);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void lockPatientsTable(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("LOCK TABLE PATIENTS IN EXCLUSIVE MODE")) {
            ps.executeUpdate();
        }
    }

    private String generateMedicalRecordId(Connection conn) throws SQLException {
        String prefix = "MR-" + Year.now().getValue() + "-";
        int highestSequence = 0;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MedicalRecordID FROM PATIENTS WHERE MedicalRecordID LIKE ?")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        int sequence = Integer.parseInt(rs.getString("MedicalRecordID").substring(prefix.length()));
                        highestSequence = Math.max(highestSequence, sequence);
                    } catch (RuntimeException ignored) {
                        // Legacy IDs with this prefix but a different suffix do not affect the sequence.
                    }
                }
            }
        }
        return prefix + String.format("%04d", highestSequence + 1);
    }

    private boolean hasRequiredRegistrationDetails(PatientInfo patient) {
        return hasRequiredProfileDetails(patient) && !isBlank(patient.getUsername()) && !isBlank(patient.getPassword());
    }

    private boolean hasRequiredProfileDetails(PatientInfo patient) {
        return patient != null && !isBlank(patient.getFirstName()) && !isBlank(patient.getLastName())
                && !isBlank(patient.getIcPassportNumber())
                && !isBlank(patient.getContactNumber()) && !isBlank(patient.getAddress());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public boolean deletePatient(int id) {
        String getUsernameSql = "SELECT Username FROM PATIENTS WHERE PatientID = ?";
        String delPatientSql = "DELETE FROM PATIENTS WHERE PatientID = ?";
        String delUserSql = "DELETE FROM USERS WHERE Username = ?";
        
        try (Connection conn = getConnection()) {
            String username = null;
            
            try (PreparedStatement psGet = conn.prepareStatement(getUsernameSql)) {
                psGet.setInt(1, id);
                ResultSet rs = psGet.executeQuery();
                if (rs.next()) username = rs.getString("Username");
                rs.close();
            }
            
            if (username != null) {
                try {
                    conn.setAutoCommit(false);
                    
                    try (PreparedStatement psDelPat = conn.prepareStatement(delPatientSql)) {
                        psDelPat.setInt(1, id);
                        psDelPat.executeUpdate();
                    }
                    try (PreparedStatement psDelUsr = conn.prepareStatement(delUserSql)) {
                        psDelUsr.setString(1, username);
                        psDelUsr.executeUpdate();
                    }

                    conn.commit();
                    return true;
                    
                } catch (SQLException e) {
                    System.out.println("[DB Error] Delete failed. Rolling back... " + e.getMessage());
                    conn.rollback();
                    return false;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
