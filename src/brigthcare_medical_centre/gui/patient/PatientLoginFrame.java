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
        setSize(420, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(41, 128, 185));

        // ── TOP BRANDING AREA ──
        JPanel brandPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        brandPanel.setBackground(new Color(41, 128, 185));
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));

        JLabel hospitalName = new JLabel("BrightCare", SwingConstants.CENTER);
        hospitalName.setFont(new Font("Times New Roman", Font.BOLD, 34));
        hospitalName.setForeground(Color.WHITE);

        JLabel hospitalSub = new JLabel("Medical Centre", SwingConstants.CENTER);
        hospitalSub.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        hospitalSub.setForeground(new Color(200, 230, 255));

        JLabel tagline = new JLabel("Patient Portal", SwingConstants.CENTER);
        tagline.setFont(new Font("Times New Roman", Font.ITALIC, 13));
        tagline.setForeground(new Color(180, 215, 255));

        brandPanel.add(hospitalName);
        brandPanel.add(hospitalSub);
        brandPanel.add(tagline);
        add(brandPanel, BorderLayout.NORTH);

        // ── LOGIN CARD ──
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 35, 20, 35));

        // Card title
        JPanel cardHeader = new JPanel(new GridLayout(2, 1, 0, 4));
        cardHeader.setBackground(Color.WHITE);
        cardHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel loginTitle = new JLabel("Welcome Back");
        loginTitle.setFont(new Font("Times New Roman", Font.BOLD, 22));
        loginTitle.setForeground(new Color(44, 62, 80));

        JLabel loginSub = new JLabel("Sign in to your patient account");
        loginSub.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        loginSub.setForeground(new Color(150, 150, 150));

        cardHeader.add(loginTitle);
        cardHeader.add(loginSub);
        card.add(cardHeader, BorderLayout.NORTH);

        // Form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        // Username field
        JLabel usernameLabel = new JLabel("Username / IC");
        usernameLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));
        usernameLabel.setForeground(new Color(44, 62, 80));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));
        passwordLabel.setForeground(new Color(44, 62, 80));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Login button
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Times New Roman", Font.BOLD, 14));
        loginBtn.setBackground(new Color(41, 128, 185));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setOpaque(true);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Hover effect
        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                loginBtn.setBackground(new Color(31, 97, 141));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                loginBtn.setBackground(new Color(41, 128, 185));
            }
        });

        // Assemble form
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(loginBtn);

        card.add(formPanel, BorderLayout.CENTER);

        // ── BOTTOM NOTE ──
        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        notePanel.setBackground(Color.WHITE);
        notePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel noteLabel = new JLabel("Contact reception if you need help accessing your account.");
        noteLabel.setFont(new Font("Times New Roman", Font.ITALIC, 11));
        noteLabel.setForeground(new Color(180, 180, 180));
        notePanel.add(noteLabel);
        card.add(notePanel, BorderLayout.SOUTH);

        // ── WRAPPER (blue sides) ──
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(41, 128, 185));
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 25, 30, 25));
        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        // Login actions
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