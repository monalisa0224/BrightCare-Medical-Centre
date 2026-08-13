package brigthcare_medical_centre.gui.patient;

import brigthcare_medical_centre.common.AuthenticationInterface;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.util.Constants;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.*;
import java.awt.*;

public class PatientLoginFrame extends JFrame {

    private AuthenticationInterface authService;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public PatientLoginFrame() {
        connectToServer();
        buildUI();
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(
                    Constants.RMI_HOST, Constants.RMI_PORT);
            authService = (AuthenticationInterface) registry.lookup(Constants.AUTH_SERVICE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to server:\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildUI() {
        setTitle("BrightCare Medical Centre - Patient Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("Patient Login", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(new Color(41, 128, 185));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(header, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        formPanel.add(new JLabel("Username / IC:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(41, 128, 185));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(new JLabel()); // empty cell
        formPanel.add(loginBtn);

        add(formPanel, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("BrightCare Medical Centre", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.ITALIC, 11));
        footer.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        add(footer, BorderLayout.SOUTH);

        // Login action
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin()); // Enter key also logs in

        setLocationRelativeTo(null);
        setVisible(true);
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
            if (user != null && user.getRole().toString().equalsIgnoreCase("PATIENT")) {
                JOptionPane.showMessageDialog(this, "Welcome, " + username + "!");
                dispose(); // close login window
                new PatientDashboardFrame(username); // open dashboard
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials or not a patient account.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Login error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PatientLoginFrame());
    }
}   