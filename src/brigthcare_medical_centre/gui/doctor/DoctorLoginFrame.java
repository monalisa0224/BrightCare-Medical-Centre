package brigthcare_medical_centre.gui.doctor;

import brigthcare_medical_centre.common.AuthenticationInterface;
import brigthcare_medical_centre.common.DoctorInterface;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.util.Constants;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.*;
import java.awt.*;

public class DoctorLoginFrame extends JFrame {

    private AuthenticationInterface authService;
    private DoctorInterface doctorService;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public DoctorLoginFrame() {
        connectToServer();
        buildUI();
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(Constants.RMI_HOST, Constants.RMI_PORT);
            authService = (AuthenticationInterface) registry.lookup(Constants.AUTH_SERVICE);
            doctorService = (DoctorInterface) registry.lookup(Constants.DOCTOR_SERVICE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to server:\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildUI() {
        setTitle("BrightCare Medical Centre - Doctor Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Doctor Login", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(new Color(0, 102, 102));
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

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(0, 102, 102));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username / IC:"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
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
            if (user != null && user.getRole().toString().equalsIgnoreCase("DOCTOR")) {
                int doctorId = doctorService.getDoctorIdByUsername(username);
                if (doctorId <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Doctor profile not found for this account.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String[] docProfile = doctorService.getDoctorProfile(doctorId);
                String doctorName = (docProfile != null) ? docProfile[1] : username;

                JOptionPane.showMessageDialog(this, "Welcome, Dr. " + doctorName + "!");
                dispose();
                new DoctorDashboardFrame(doctorId, doctorName, username);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials or not a doctor account.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Login error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DoctorLoginFrame());
    }
}
