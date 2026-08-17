package brigthcare_medical_centre.gui.doctor;

import brigthcare_medical_centre.common.AuthenticationInterface;
import brigthcare_medical_centre.common.DoctorInterface;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.auth.CredentialStore;
import brigthcare_medical_centre.util.Constants;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoctorLoginFrame extends JFrame {

    private AuthenticationInterface authService;
    private DoctorInterface doctorService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckBox;

    private static final Color PRIMARY       = new Color(53, 47, 68);    // #352F44
    private static final Color PRIMARY_DARK  = new Color(40, 35, 52);    // darker #352F44
    private static final Color PRIMARY_LIGHT = new Color(185, 180, 199); // #B9B4C7
    private static final Color DARK_BG       = new Color(40, 35, 52);    // darker #352F44
    private static final Color TEXT_PRIMARY  = new Color(53, 47, 68);    // #352F44
    private static final Color TEXT_SECONDARY = new Color(92, 84, 112);  // #5C5470
    private static final Color TEXT_MUTED    = new Color(185, 180, 199); // #B9B4C7
    private static final Color BORDER       = new Color(185, 180, 199);  // #B9B4C7
    private static final Color SURFACE      = new Color(255, 248, 231);  // #FFF8E7

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
        setSize(860, 520);
        setMinimumSize(new Dimension(780, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(SURFACE);

        // ── LEFT PANEL: Branding & Features ──
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(DARK_BG);
        leftPanel.setPreferredSize(new Dimension(380, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Medical cross icon
        JLabel iconLabel = new JLabel("\u2695");
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 48));
        iconLabel.setForeground(PRIMARY_LIGHT);
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(iconLabel);
        leftPanel.add(Box.createVerticalStrut(10));

        // Hospital name
        JLabel hospName = new JLabel("BrightCare");
        hospName.setFont(new Font("Segoe UI", Font.BOLD, 30));
        hospName.setForeground(Color.WHITE);
        hospName.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(hospName);

        JLabel hospSub = new JLabel("Medical Centre");
        hospSub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        hospSub.setForeground(PRIMARY_LIGHT);
        hospSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(hospSub);
        leftPanel.add(Box.createVerticalStrut(20));

        // Divider
        JPanel divider = new JPanel();
        divider.setMaximumSize(new Dimension(200, 2));
        divider.setBackground(PRIMARY);
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(divider);
        leftPanel.add(Box.createVerticalStrut(20));

        // Tagline
        JLabel tagline = new JLabel("Doctor Portal");
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tagline.setForeground(Color.WHITE);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(tagline);
        leftPanel.add(Box.createVerticalStrut(8));

        JLabel desc = new JLabel("<html>Access your dashboard to manage<br/>appointments, consultations, and<br/>patient records efficiently.</html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(PRIMARY_LIGHT);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(desc);
        leftPanel.add(Box.createVerticalStrut(24));

        leftPanel.add(Box.createVerticalGlue());

        // Bottom branding
        JLabel bottomBrand = new JLabel("BrightCare Medical Centre");
        bottomBrand.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        bottomBrand.setForeground(PRIMARY_LIGHT);
        bottomBrand.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(bottomBrand);

        add(leftPanel, BorderLayout.WEST);

        // ── RIGHT PANEL: Login Form ──
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(SURFACE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel formWrapper = new JPanel();
        formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
        formWrapper.setBackground(SURFACE);
        formWrapper.setMaximumSize(new Dimension(340, Integer.MAX_VALUE));

        // Welcome header
        JLabel welcomeTitle = new JLabel("Welcome Back");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeTitle.setForeground(TEXT_PRIMARY);
        welcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(welcomeTitle);

        JLabel welcomeSub = new JLabel("Sign in to your doctor account");
        welcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeSub.setForeground(TEXT_SECONDARY);
        welcomeSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(welcomeSub);
        formWrapper.add(Box.createVerticalStrut(30));

        // Separator line
        JPanel sepLine = new JPanel();
        sepLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sepLine.setBackground(BORDER);
        sepLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(sepLine);
        formWrapper.add(Box.createVerticalStrut(24));

        // Username
        JLabel usernameLabel = new JLabel("USERNAME");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        usernameLabel.setForeground(TEXT_SECONDARY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(usernameLabel);
        formWrapper.add(Box.createVerticalStrut(6));

        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        formWrapper.add(usernameField);
        formWrapper.add(Box.createVerticalStrut(16));

        // Password
        JLabel passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passwordLabel.setForeground(TEXT_SECONDARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(passwordLabel);
        formWrapper.add(Box.createVerticalStrut(6));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        formWrapper.add(passwordField);
        formWrapper.add(Box.createVerticalStrut(10));

        // Remember me + forgot password row
        JPanel optRow = new JPanel(new BorderLayout());
        optRow.setBackground(SURFACE);
        optRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        optRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        rememberMeCheckBox = new JCheckBox("Remember Me");
        rememberMeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMeCheckBox.setForeground(TEXT_SECONDARY);
        rememberMeCheckBox.setBackground(SURFACE);

        JLabel forgotLabel = new JLabel("Forgot password?");
        forgotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotLabel.setForeground(PRIMARY);
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(DoctorLoginFrame.this,
                        "Please contact the system administrator for password reset.",
                        "Password Recovery", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        optRow.add(rememberMeCheckBox, BorderLayout.WEST);
        optRow.add(forgotLabel, BorderLayout.EAST);
        formWrapper.add(optRow);
        formWrapper.add(Box.createVerticalStrut(22));

        // Sign In button
        JButton loginBtn = new JButton("SIGN IN");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(PRIMARY);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setOpaque(true);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginBtn.setBackground(PRIMARY_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                loginBtn.setBackground(PRIMARY);
            }
        });

        formWrapper.add(loginBtn);
        formWrapper.add(Box.createVerticalStrut(16));

        // Bottom note
        JLabel noteLabel = new JLabel("Contact admin if you need help accessing your account.",
                SwingConstants.CENTER);
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(TEXT_MUTED);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrapper.add(noteLabel);

        rightPanel.add(formWrapper);

        add(rightPanel, BorderLayout.CENTER);

        // ── ACTIONS ──
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
            if (user != null && user.getRole().toString().equalsIgnoreCase("DOCTOR")) {
                int doctorId = doctorService.getDoctorIdByUsername(username);
                if (doctorId <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Doctor profile not found for this account.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (rememberMeCheckBox.isSelected()) {
                    CredentialStore.save(username, password, "DOCTOR");
                } else {
                    CredentialStore.clear();
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
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 13));
        SwingUtilities.invokeLater(() -> new DoctorLoginFrame());
    }

    private void loadSavedCredentials() {
        CredentialStore saved = CredentialStore.load();
        if (saved != null && !saved.isExpired() && "DOCTOR".equals(saved.getRole())) {
            usernameField.setText(saved.getUsername());
            passwordField.setText(saved.getPassword());
            rememberMeCheckBox.setSelected(true);
        }
    }
}
