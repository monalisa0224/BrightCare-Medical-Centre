package brigthcare_medical_centre.database;

import brigthcare_medical_centre.common.PatientInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistDB {

    private Connection getConnection() throws SQLException {
        return DerbyConnection.getConnection();
    }

    public boolean registerPatient(PatientInfo p) {
        String userSql = "INSERT INTO USERS (Username, PasswordHash, Role) VALUES (?, ?, ?)";
        String patientSql = "INSERT INTO PATIENTS (Username, ContactNumber, Address) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                
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
                    ps.setString(2, p.getContactNumber());
                    ps.setString(3, p.getAddress());
                    ps.executeUpdate();
                }
                
                conn.commit();
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
        String sql = "SELECT PatientID, Username, ContactNumber, Address FROM PATIENTS";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PatientInfo(
                    rs.getInt("PatientID"),
                    rs.getString("Username"),
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
        String sql = "SELECT PatientID, Username, ContactNumber, Address FROM PATIENTS WHERE LOWER(Username) LIKE ?";
                   
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PatientInfo(
                    rs.getInt("PatientID"),
                    rs.getString("Username"),
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
        String sql = "UPDATE PATIENTS SET ContactNumber = ?, Address = ? WHERE Username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, p.getContactNumber());
            ps.setString(2, p.getAddress());
            ps.setString(3, p.getUsername());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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