package brigthcare_medical_centre.gui.receptionist;

import brigthcare_medical_centre.common.PatientInfo;
import brigthcare_medical_centre.common.ReceptionistInterface;
import brigthcare_medical_centre.util.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ReceptionistDashboardFrame extends JFrame {

    private ReceptionistInterface receptionistService;

    // GUI Components for Registration
    private JTextField txtUsername, txtPassword, txtContact, txtAddress;

    // GUI Components for Management
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public ReceptionistDashboardFrame() {
        super("BrightCare Clinic - Receptionist Portal");
        
        connectToServer();
        initUI();
        refreshPatientTable();
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(Constants.RMI_HOST, Constants.RMI_PORT);
            receptionistService = (ReceptionistInterface) registry.lookup(Constants.RECEPTIONIST_SERVICE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Cannot connect to Server. Is the server running?\n" + e.getMessage(), 
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        this.setSize(850, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null); 
        this.setLayout(new BorderLayout());

        this.add(createHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Register New Patient", createRegistrationPanel());
        tabbedPane.addTab("Manage Patients", createManagementPanel());
        
        this.add(tabbedPane, BorderLayout.CENTER);
    }


    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204)); 
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblTitle = new JLabel("Receptionist Portal");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(231, 76, 60)); // Red Alert Color
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> logout());
        
        header.add(btnLogout, BorderLayout.EAST);

        return header;
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = new JTextField(20);
        txtPassword = new JTextField(20);
        txtContact = new JTextField(20);
        txtAddress = new JTextField(20);

        int row = 0;
        addFormField(panel, "Patient Username:", txtUsername, gbc, row++);
        addFormField(panel, "Temporary Password:", txtPassword, gbc, row++);
        addFormField(panel, "Contact Number:", txtContact, gbc, row++);
        addFormField(panel, "Home Address:", txtAddress, gbc, row++);

        JButton btnRegister = new JButton("Register Patient");
        btnRegister.setBackground(new Color(41, 128, 185));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 14));
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(btnRegister, gbc);

        btnRegister.addActionListener(e -> registerPatient());

        return panel;
    }

    private void addFormField(JPanel panel, String labelText, JTextField textField, GridBagConstraints gbc, int row) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel(labelText), gbc);
        
        gbc.gridx = 1;
        panel.add(textField, gbc);
    }

    private void registerPatient() {
        if (receptionistService == null) return;

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String contact = txtContact.getText().trim();
        String address = txtAddress.getText().trim();

        if (username.isEmpty() || password.isEmpty() || contact.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Registration Failed: All fields must be filled out before registering a patient.", 
                "Missing Information", JOptionPane.WARNING_MESSAGE);
            return; // Stop the method here so it doesn't send blank data to the server
        }

        try {
            PatientInfo newPatient = new PatientInfo(
                username, password, contact, address
            );

            boolean success = receptionistService.registerPatient(newPatient);

            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Patient " + txtUsername.getText() + " successfully registered!",
                    "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                
                txtUsername.setText(""); txtPassword.setText("");
                txtContact.setText(""); txtAddress.setText("");
                refreshPatientTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register. Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Server Error: " + ex.getMessage());
        }
    }

    private JPanel createManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search by Username");
        JButton btnRefresh = new JButton("Refresh All");
        
        topPanel.add(new JLabel("Search:"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnRefresh);
        // Logout removed from here!
        panel.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Patient ID", "Username", "Contact Number", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } 
        };
        patientTable = new JTable(tableModel);
        panel.add(new JScrollPane(patientTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // ---> NEW: Update Button added to GUI <---
        JButton btnEdit = new JButton("Edit Selected");
        btnEdit.setBackground(new Color(243, 156, 18)); // Orange warning color
        btnEdit.setForeground(Color.WHITE);
        
        JButton btnDelete = new JButton("Delete Selected");
        btnDelete.setBackground(new Color(231, 76, 60)); // Red danger color
        btnDelete.setForeground(Color.WHITE);
        
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> searchPatients());
        btnRefresh.addActionListener(e -> refreshPatientTable());
        btnDelete.addActionListener(e -> deleteSelectedPatient());
        btnEdit.addActionListener(e -> editSelectedPatient());

        return panel;
    }

    // ---> NEW: The Edit Patient Logic <---
    private void editSelectedPatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Grab current data from the table
        int patientId = (int) tableModel.getValueAt(selectedRow, 0); 
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String currentContact = (String) tableModel.getValueAt(selectedRow, 2);
        String currentAddress = (String) tableModel.getValueAt(selectedRow, 3);

        // 2. Create editable text fields pre-filled with the old data
        JTextField fieldContact = new JTextField(currentContact);
        JTextField fieldAddress = new JTextField(currentAddress);

        Object[] message = {
            "Username (Read-Only): " + username,
            "Contact Number:", fieldContact,
            "Address:", fieldAddress
        };

        // 3. Show a pop-up dialog
        int option = JOptionPane.showConfirmDialog(this, message, "Update Patient Profile", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            try {
                // 4. Pack the new data into a PatientInfo box
                PatientInfo updatedPatient = new PatientInfo(patientId, username, fieldContact.getText(), fieldAddress.getText());
                
                // 5. Send to Server!
                boolean success = receptionistService.updatePatient(updatedPatient);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Patient updated successfully!");
                    refreshPatientTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update patient.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Server Error: " + ex.getMessage());
            }
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose(); 
            SwingUtilities.invokeLater(() -> new ReceptionistLoginFrame().setVisible(true));
        }
    }

    private void refreshPatientTable() {
        if (receptionistService == null) return;
        try {
            List<PatientInfo> patients = receptionistService.getAllPatients();
            updateTableData(patients);
        } catch (Exception ex) {
            System.err.println("Failed to fetch patients: " + ex.getMessage());
        }
    }

    private void searchPatients() {
        if (receptionistService == null) return;
        try {
            String keyword = txtSearch.getText().trim();
            List<PatientInfo> patients = receptionistService.searchPatient(keyword);
            updateTableData(patients);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
    }

    private void updateTableData(List<PatientInfo> patients) {
        tableModel.setRowCount(0); 
        for (PatientInfo p : patients) {
            tableModel.addRow(new Object[]{
                p.getId(), p.getUsername(), p.getContactNumber(), p.getAddress()
            });
        }
    }

    private void deleteSelectedPatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId = (int) tableModel.getValueAt(selectedRow, 0); 
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to completely delete patient: " + username + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = receptionistService.deletePatient(patientId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Patient deleted.");
                    refreshPatientTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete patient.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Server Error: " + ex.getMessage());
            }
        }
    }
}

// DYNAMIC LAYOUT
//package brigthcare_medical_centre.gui.receptionist;
//
//import brigthcare_medical_centre.common.PatientInfo;
//import brigthcare_medical_centre.common.ReceptionistInterface;
//import brigthcare_medical_centre.util.Constants;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//import java.util.List;
//
//public class ReceptionistDashboardFrame extends JFrame {
//
//    private ReceptionistInterface receptionistService;
//
//    // GUI Components for Registration
//    private JTextField txtUsername, txtPassword, txtContact, txtAddress;
//
//    // GUI Components for Management
//    private JTable patientTable;
//    private DefaultTableModel tableModel;
//    private JTextField txtSearch;
//
//    public ReceptionistDashboardFrame() {
//        super("BrightCare Clinic - Receptionist Portal");
//        
//        connectToServer();
//        initUI();
//        refreshPatientTable();
//    }
//
//    private void connectToServer() {
//        try {
//            Registry registry = LocateRegistry.getRegistry(Constants.RMI_HOST, Constants.RMI_PORT);
//            receptionistService = (ReceptionistInterface) registry.lookup(Constants.RECEPTIONIST_SERVICE);
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(this, 
//                "Cannot connect to Server. Is the server running?\n" + e.getMessage(), 
//                "Connection Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    private void initUI() {
//        this.setSize(850, 600);
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        this.setLocationRelativeTo(null); 
//        this.setLayout(new BorderLayout());
//        
//        // 1. ---> NEW: The Top Blue Header Banner <---
//        this.add(createHeaderPanel(), BorderLayout.NORTH);
//        
//        // 2. The Main Content (Tabs)
//        JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.addTab("Register New Patient", createRegistrationPanel());
//        tabbedPane.addTab("Manage Patients", createManagementPanel());
//        
//        this.add(tabbedPane, BorderLayout.CENTER);
//    }
//
//    // Helper method to build the professional banner
//    private JPanel createHeaderPanel() {
//        JPanel header = new JPanel(new BorderLayout());
//        header.setBackground(new Color(0, 102, 204)); // BrightCare Blue
//        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
//
//        JLabel lblTitle = new JLabel("Receptionist Portal");
//        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
//        lblTitle.setForeground(Color.WHITE);
//        header.add(lblTitle, BorderLayout.WEST);
//
//        JButton btnLogout = new JButton("Logout");
//        btnLogout.setBackground(new Color(231, 76, 60)); // Red Alert Color
//        btnLogout.setForeground(Color.WHITE);
//        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
//        btnLogout.setFocusPainted(false);
//        btnLogout.addActionListener(e -> logout());
//        
//        header.add(btnLogout, BorderLayout.EAST);
//
//        return header;
//    }
//
//    private JPanel createRegistrationPanel() {
//        JPanel panel = new JPanel(new GridBagLayout());
//        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(10, 10, 10, 10);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        txtUsername = new JTextField(20);
//        txtPassword = new JTextField(20);
//        txtContact = new JTextField(20);
//        txtAddress = new JTextField(20);
//
//        int row = 0;
//        addFormField(panel, "Patient Username:", txtUsername, gbc, row++);
//        addFormField(panel, "Temporary Password:", txtPassword, gbc, row++);
//        addFormField(panel, "Contact Number:", txtContact, gbc, row++);
//        addFormField(panel, "Home Address:", txtAddress, gbc, row++);
//
//        JButton btnRegister = new JButton("Register Patient");
//        btnRegister.setBackground(new Color(41, 128, 185));
//        btnRegister.setForeground(Color.WHITE);
//        btnRegister.setFont(new Font("Arial", Font.BOLD, 14));
//        
//        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
//        panel.add(btnRegister, gbc);
//
//        btnRegister.addActionListener(e -> registerPatient());
//
//        return panel;
//    }
//
//    private void addFormField(JPanel panel, String labelText, JTextField textField, GridBagConstraints gbc, int row) {
//        gbc.gridwidth = 1;
//        gbc.gridx = 0; gbc.gridy = row;
//        panel.add(new JLabel(labelText), gbc);
//        
//        gbc.gridx = 1;
//        panel.add(textField, gbc);
//    }
//
//    private void registerPatient() {
//        if (receptionistService == null) return;
//
//        // ---> NEW: Input Validation Check <---
//        String username = txtUsername.getText().trim();
//        String password = txtPassword.getText().trim();
//        String contact = txtContact.getText().trim();
//        String address = txtAddress.getText().trim();
//
//        if (username.isEmpty() || password.isEmpty() || contact.isEmpty() || address.isEmpty()) {
//            JOptionPane.showMessageDialog(this, 
//                "Registration Failed: All fields must be filled out before registering a patient.", 
//                "Missing Information", JOptionPane.WARNING_MESSAGE);
//            return; // Stop the method here so it doesn't send blank data to the server
//        }
//
//        try {
//            PatientInfo newPatient = new PatientInfo(
//                username, password, contact, address
//            );
//
//            boolean success = receptionistService.registerPatient(newPatient);
//
//            if (success) {
//                JOptionPane.showMessageDialog(this, 
//                    "Patient " + txtUsername.getText() + " successfully registered!",
//                    "Registration Success", JOptionPane.INFORMATION_MESSAGE);
//                
//                txtUsername.setText(""); txtPassword.setText("");
//                txtContact.setText(""); txtAddress.setText("");
//                refreshPatientTable();
//            } else {
//                JOptionPane.showMessageDialog(this, "Failed to register. Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this, "Server Error: " + ex.getMessage());
//        }
//    }
//
//    private JPanel createManagementPanel() {
//        JPanel panel = new JPanel(new BorderLayout(10, 10));
//        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        txtSearch = new JTextField(20);
//        JButton btnSearch = new JButton("Search by Username");
//        JButton btnRefresh = new JButton("Refresh All");
//        
//        topPanel.add(new JLabel("Search:"));
//        topPanel.add(txtSearch);
//        topPanel.add(btnSearch);
//        topPanel.add(btnRefresh);
//        // Logout removed from here!
//        panel.add(topPanel, BorderLayout.NORTH);
//
//        String[] columns = {"Patient ID", "Username", "Contact Number", "Address"};
//        tableModel = new DefaultTableModel(columns, 0) {
//            @Override
//            public boolean isCellEditable(int row, int column) { return false; } 
//        };
//        patientTable = new JTable(tableModel);
//        panel.add(new JScrollPane(patientTable), BorderLayout.CENTER);
//
//        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        
//        // ---> NEW: Update Button added to GUI <---
//        JButton btnEdit = new JButton("Edit Selected");
//        btnEdit.setBackground(new Color(243, 156, 18)); // Orange warning color
//        btnEdit.setForeground(Color.WHITE);
//        
//        JButton btnDelete = new JButton("Delete Selected");
//        btnDelete.setBackground(new Color(231, 76, 60)); // Red danger color
//        btnDelete.setForeground(Color.WHITE);
//        
//        bottomPanel.add(btnEdit);
//        bottomPanel.add(btnDelete);
//        panel.add(bottomPanel, BorderLayout.SOUTH);
//
//        btnSearch.addActionListener(e -> searchPatients());
//        btnRefresh.addActionListener(e -> refreshPatientTable());
//        btnDelete.addActionListener(e -> deleteSelectedPatient());
//        btnEdit.addActionListener(e -> editSelectedPatient());
//
//        return panel;
//    }
//
//    // ---> NEW: The Edit Patient Logic <---
//    private void editSelectedPatient() {
//        int selectedRow = patientTable.getSelectedRow();
//        if (selectedRow == -1) {
//            JOptionPane.showMessageDialog(this, "Please select a patient from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        // 1. Grab current data from the table
//        int patientId = (int) tableModel.getValueAt(selectedRow, 0); 
//        String username = (String) tableModel.getValueAt(selectedRow, 1);
//        String currentContact = (String) tableModel.getValueAt(selectedRow, 2);
//        String currentAddress = (String) tableModel.getValueAt(selectedRow, 3);
//
//        // 2. Create editable text fields pre-filled with the old data
//        JTextField fieldContact = new JTextField(currentContact);
//        JTextField fieldAddress = new JTextField(currentAddress);
//
//        Object[] message = {
//            "Username (Read-Only): " + username,
//            "Contact Number:", fieldContact,
//            "Address:", fieldAddress
//        };
//
//        // 3. Show a pop-up dialog
//        int option = JOptionPane.showConfirmDialog(this, message, "Update Patient Profile", JOptionPane.OK_CANCEL_OPTION);
//        
//        if (option == JOptionPane.OK_OPTION) {
//            try {
//                // 4. Pack the new data into a PatientInfo box
//                PatientInfo updatedPatient = new PatientInfo(patientId, username, fieldContact.getText(), fieldAddress.getText());
//                
//                // 5. Send to Server!
//                boolean success = receptionistService.updatePatient(updatedPatient);
//                
//                if (success) {
//                    JOptionPane.showMessageDialog(this, "Patient updated successfully!");
//                    refreshPatientTable();
//                } else {
//                    JOptionPane.showMessageDialog(this, "Failed to update patient.", "Database Error", JOptionPane.ERROR_MESSAGE);
//                }
//            } catch (Exception ex) {
//                JOptionPane.showMessageDialog(this, "Server Error: " + ex.getMessage());
//            }
//        }
//    }
//
//    private void logout() {
//        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
//        if (confirm == JOptionPane.YES_OPTION) {
//            this.dispose(); 
//            SwingUtilities.invokeLater(() -> new ReceptionistLoginFrame().setVisible(true));
//        }
//    }
//
//    private void refreshPatientTable() {
//        if (receptionistService == null) return;
//        try {
//            List<PatientInfo> patients = receptionistService.getAllPatients();
//            updateTableData(patients);
//        } catch (Exception ex) {
//            System.err.println("Failed to fetch patients: " + ex.getMessage());
//        }
//    }
//
//    private void searchPatients() {
//        if (receptionistService == null) return;
//        try {
//            String keyword = txtSearch.getText().trim();
//            List<PatientInfo> patients = receptionistService.searchPatient(keyword);
//            updateTableData(patients);
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
//        }
//    }
//
//    private void updateTableData(List<PatientInfo> patients) {
//        tableModel.setRowCount(0); 
//        for (PatientInfo p : patients) {
//            tableModel.addRow(new Object[]{
//                p.getId(), p.getUsername(), p.getContactNumber(), p.getAddress()
//            });
//        }
//    }
//
//    private void deleteSelectedPatient() {
//        int selectedRow = patientTable.getSelectedRow();
//        if (selectedRow == -1) {
//            JOptionPane.showMessageDialog(this, "Please select a patient from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        int patientId = (int) tableModel.getValueAt(selectedRow, 0); 
//        String username = (String) tableModel.getValueAt(selectedRow, 1);
//
//        int confirm = JOptionPane.showConfirmDialog(this, 
//            "Are you sure you want to completely delete patient: " + username + "?", 
//            "Confirm Delete", JOptionPane.YES_NO_OPTION);
//
//        if (confirm == JOptionPane.YES_OPTION) {
//            try {
//                boolean success = receptionistService.deletePatient(patientId);
//                if (success) {
//                    JOptionPane.showMessageDialog(this, "Patient deleted.");
//                    refreshPatientTable();
//                } else {
//                    JOptionPane.showMessageDialog(this, "Failed to delete patient.", "Error", JOptionPane.ERROR_MESSAGE);
//                }
//            } catch (Exception ex) {
//                JOptionPane.showMessageDialog(this, "Server Error: " + ex.getMessage());
//            }
//        }
//    }
//}