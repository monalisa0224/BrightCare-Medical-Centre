package brigthcare_medical_centre.gui.receptionist;

import brigthcare_medical_centre.common.AuthenticationInterface;
import brigthcare_medical_centre.common.ReceptionistInterface;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.auth.CredentialStore;
import brigthcare_medical_centre.util.Constants;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.*;
import java.awt.*;

public class ReceptionistLoginFrame extends JFrame {

    private AuthenticationInterface authService;
    private ReceptionistInterface receptionistService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckBox;

    public ReceptionistLoginFrame() {
        connectToServer();
        
        buildUI();
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(Constants.RMI_HOST, Constants.RMI_PORT);
            authService = (AuthenticationInterface) registry.lookup(Constants.AUTH_SERVICE);
            
            // Looking up the Receptionist Service 
            // (Note: If your team added RECEPTIONIST_SERVICE to Constants.java, you can use Constants.RECEPTIONIST_SERVICE here)
            receptionistService = (ReceptionistInterface) registry.lookup("ReceptionistService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to server:\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildUI() {
        setTitle("BrightCare Medical Centre - Receptionist Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Receptionist Login", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(new Color(0, 102, 204));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));
        add(header, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        rememberMeCheckBox = new JCheckBox("Remember Me");

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(0, 102, 204)); // Matching Blue
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(rememberMeCheckBox, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(loginBtn, gbc);

        add(formPanel, BorderLayout.CENTER);

        JLabel footer = new JLabel("BrightCare Medical Centre", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.ITALIC, 11));
        footer.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        add(footer, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());

        setLocationRelativeTo(null);
        setVisible(true);

        loadSavedCredentials();
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = authService.login(username, password);
            
            // Checking if the user exists and has the RECEPTIONIST role
            if (user != null && user.getRole().toString().equalsIgnoreCase("RECEPTIONIST")) {
                if (rememberMeCheckBox.isSelected()) {
                    CredentialStore.save(username, password, "RECEPTIONIST");
                } else {
                    CredentialStore.clear();
                }
                
                JOptionPane.showMessageDialog(this, "Welcome, Receptionist " + username + "!");
                dispose(); // Close the login screen
                
                // Open the Receptionist Dashboard
                new ReceptionistDashboardFrame().setVisible(true);
                
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials or not a receptionist account.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Login error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReceptionistLoginFrame());
    }

    private void loadSavedCredentials() {
        CredentialStore saved = CredentialStore.load();
        if (saved != null && !saved.isExpired() && "RECEPTIONIST".equals(saved.getRole())) {
            usernameField.setText(saved.getUsername());
            passwordField.setText(saved.getPassword());
            rememberMeCheckBox.setSelected(true);
        }
    }
}