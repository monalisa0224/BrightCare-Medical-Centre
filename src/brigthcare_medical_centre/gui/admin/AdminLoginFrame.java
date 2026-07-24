package brigthcare_medical_centre.gui.admin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.rmi.Naming;
import javax.swing.*;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.auth.CredentialStore;
import brigthcare_medical_centre.common.AdminInterface;
import brigthcare_medical_centre.common.AuthenticationInterface;
import brigthcare_medical_centre.common.ReportInterface;
import brigthcare_medical_centre.util.Constants;

public class AdminLoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckBox;

    public AdminLoginFrame() {
        setTitle("BrightCare - Admin Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 280);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Admin Login", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18.0f));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);
        rememberMeCheckBox = new JCheckBox("Remember Me");
        formPanel.add(new JLabel());
        formPanel.add(rememberMeCheckBox);
        panel.add(formPanel, BorderLayout.CENTER);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);

        loadSavedCredentials();
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        try {
            AuthenticationInterface authService = (AuthenticationInterface)
                    Naming.lookup("rmi://" + Constants.RMI_HOST + ":" + Constants.RMI_PORT + "/" + Constants.AUTH_SERVICE);
            AdminInterface adminService = (AdminInterface)
                    Naming.lookup("rmi://" + Constants.RMI_HOST + ":" + Constants.RMI_PORT + "/" + Constants.ADMIN_SERVICE);
            ReportInterface reportService = (ReportInterface)
                    Naming.lookup("rmi://" + Constants.RMI_HOST + ":" + Constants.RMI_PORT + "/" + Constants.REPORT_SERVICE);

            User user = authService.login(username, password);

            if (user != null) {
                if (user.getRole() == brigthcare_medical_centre.auth.UserRole.ADMIN) {
                    if (rememberMeCheckBox.isSelected()) {
                        CredentialStore.save(username, password, "ADMIN");
                    } else {
                        CredentialStore.clear();
                    }
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    dispose();
                    new AdminDashboardFrame(user, adminService, reportService).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Access denied. Admin role required.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminLoginFrame().setVisible(true));
    }

    private void loadSavedCredentials() {
        CredentialStore saved = CredentialStore.load();
        if (saved != null && !saved.isExpired() && "ADMIN".equals(saved.getRole())) {
            usernameField.setText(saved.getUsername());
            passwordField.setText(saved.getPassword());
            rememberMeCheckBox.setSelected(true);
        }
    }
}
