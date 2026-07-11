package brigthcare_medical_centre.gui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.rmi.RemoteException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import brigthcare_medical_centre.auth.UserRole;
import brigthcare_medical_centre.common.AdminInterface;

public class UserManagementPanel extends JPanel {

    private final AdminInterface adminService;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JComboBox<UserRole> roleComboBox;

    public UserManagementPanel(AdminInterface adminService) {
        this.adminService = adminService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel - Registration form
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);

        // Center panel - User table
        String[] columns = {"User ID", "Username", "Role", "Created Date", "Activity Count"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel - Action buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        loadUsers();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Register New User"));

        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        roleComboBox = new JComboBox<>(UserRole.values());

        gridPanel.add(new JLabel("Username:"));
        gridPanel.add(usernameField);
        gridPanel.add(new JLabel("Password:"));
        gridPanel.add(passwordField);
        gridPanel.add(new JLabel("Role:"));
        gridPanel.add(roleComboBox);

        panel.add(gridPanel, BorderLayout.CENTER);

        JButton registerButton = new JButton("Register User");
        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            UserRole role = (UserRole) roleComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean success = adminService.registerUser(username, password, role);
                if (success) {
                    JOptionPane.showMessageDialog(this, "User registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    usernameField.setText("");
                    passwordField.setText("");
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed. Username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(registerButton);
        panel.add(buttonWrapper, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadUsers());

        JButton updateRoleButton = new JButton("Update Role");
        updateRoleButton.addActionListener(e -> updateSelectedUserRole());

        JButton deleteButton = new JButton("Delete User");
        deleteButton.addActionListener(e -> deleteSelectedUser());

        panel.add(refreshButton);
        panel.add(updateRoleButton);
        panel.add(deleteButton);

        return panel;
    }

    private void loadUsers() {
        try {
            List<String[]> users = adminService.getAllUsers();
            tableModel.setRowCount(0);
            for (String[] user : users) {
                tableModel.addRow(user);
            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedUserRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        String currentRole = tableModel.getValueAt(selectedRow, 2).toString();

        UserRole newRole = (UserRole) JOptionPane.showInputDialog(
                this,
                "Select new role for user ID " + userId + ":",
                "Update User Role",
                JOptionPane.PLAIN_MESSAGE,
                null,
                UserRole.values(),
                UserRole.valueOf(currentRole)
        );

        if (newRole == null) return;

        try {
            boolean success = adminService.updateUserRole(userId, newRole);
            if (success) {
                JOptionPane.showMessageDialog(this, "User role updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user role.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        String username = tableModel.getValueAt(selectedRow, 1).toString();
        String role = tableModel.getValueAt(selectedRow, 2).toString();

        if ("ADMIN".equals(role)) {
            JOptionPane.showMessageDialog(this, "Cannot delete admin users.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete user: " + username + " (ID: " + userId + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean success = adminService.deleteUser(userId);
            if (success) {
                JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}