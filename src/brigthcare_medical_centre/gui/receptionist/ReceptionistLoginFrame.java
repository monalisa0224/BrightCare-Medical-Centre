package brigthcare_medical_centre.gui.receptionist;

import brigthcare_medical_centre.common.AuthenticationInterface;
import brigthcare_medical_centre.common.ReceptionistInterface;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.auth.CredentialStore;
import brigthcare_medical_centre.util.Constants;
import brigthcare_medical_centre.util.SslUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ReceptionistLoginFrame extends JFrame {

    private AuthenticationInterface authService;
    private ReceptionistInterface receptionistService;
    
    private PlaceholderTextField usernameField;
    private PlaceholderPasswordField passwordField;
    private CustomCheckBox rememberMeCheckBox;
    private TealButton btnSignIn;

    public ReceptionistLoginFrame() {
        connectToServer();
        buildUI();
    }

    private void connectToServer() {
        try {
            Registry registry;
            if (Constants.SSL_ENABLED) {
                System.setProperty("javax.net.ssl.trustStore", "clienttrust.jks");
                System.setProperty("javax.net.ssl.trustStorePassword", "brightcare123");
                SslUtil.validateClientSSL();
                registry = LocateRegistry.getRegistry(
                        Constants.RMI_HOST,
                        Constants.RECEPTIONIST_RMI_PORT,
                        SslUtil.clientSocketFactory());
            } else {
                registry = LocateRegistry.getRegistry(Constants.RMI_HOST, Constants.RMI_PORT);
            }
            authService = (AuthenticationInterface) registry.lookup(Constants.AUTH_SERVICE);
            receptionistService = (ReceptionistInterface) registry.lookup(Constants.RECEPTIONIST_SERVICE);
        } catch (Exception e) {
            ModernDialog.showMessage(this, "Connection Error",
                    "Cannot connect to server:\n" + e.getMessage(),
                    ModernDialog.Type.ERROR);
        }
    }

    private void buildUI() {
        setTitle("BrightCare Clinic - Receptionist Portal Login");
        setSize(960, 620);
        setMinimumSize(new Dimension(880, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Main Container Split 50/50
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        mainContainer.setBackground(Color.WHITE);

        // Left Brand Hero Panel & Right Form Panel
        mainContainer.add(createLeftHeroPanel());
        mainContainer.add(createRightFormPanel());

        this.setContentPane(mainContainer);

        // Key Listener for Enter Key
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> doLogin());

        loadSavedCredentials();
    }

    private JPanel createLeftHeroPanel() {
        JPanel heroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep Navy/Blue Vertical Gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(11, 86, 138),
                        0, getHeight(), new Color(15, 72, 112)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        heroPanel.setLayout(new BorderLayout());
        heroPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Top Brand Header (BrightCare Clinic title only, + icon badge removed)
        JPanel brandHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brandHeader.setOpaque(false);

        JLabel lblBrandTitle = new JLabel("BrightCare Clinic");
        lblBrandTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblBrandTitle.setForeground(Color.WHITE);

        brandHeader.add(lblBrandTitle);

        heroPanel.add(brandHeader, BorderLayout.NORTH);

        // Center Hero Text Box
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        JLabel lblMainHeader1 = new JLabel("Care begins with a");
        lblMainHeader1.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblMainHeader1.setForeground(Color.WHITE);
        lblMainHeader1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMainHeader2 = new JLabel("secure welcome.");
        lblMainHeader2.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblMainHeader2.setForeground(Color.WHITE);
        lblMainHeader2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtSub = new JTextArea("Sign in to register patients, maintain profiles, and keep every medical record organised.");
        txtSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSub.setForeground(new Color(195, 225, 247));
        txtSub.setWrapStyleWord(true);
        txtSub.setLineWrap(true);
        txtSub.setEditable(false);
        txtSub.setFocusable(false);
        txtSub.setOpaque(false);
        txtSub.setMaximumSize(new Dimension(380, 90));
        txtSub.setBorder(new EmptyBorder(16, 0, 0, 0));
        txtSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerBox.add(Box.createVerticalGlue());
        centerBox.add(lblMainHeader1);
        centerBox.add(Box.createVerticalStrut(4));
        centerBox.add(lblMainHeader2);
        centerBox.add(txtSub);
        centerBox.add(Box.createVerticalGlue());

        heroPanel.add(centerBox, BorderLayout.CENTER);

        // Bottom Footer (Shield Icon + Secure receptionist access)
        JPanel footerBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        footerBox.setOpaque(false);

        JLabel lblShieldIcon = new JLabel("🛡");
        lblShieldIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblShieldIcon.setForeground(new Color(195, 225, 247));

        JLabel lblFooterText = new JLabel("Secure receptionist access");
        lblFooterText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFooterText.setForeground(new Color(195, 225, 247));

        footerBox.add(lblShieldIcon);
        footerBox.add(lblFooterText);

        heroPanel.add(footerBox, BorderLayout.SOUTH);

        return heroPanel;
    }

    private JPanel createRightFormPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(40, 50, 30, 50));

        // Form Inner Stack
        JPanel formStack = new JPanel();
        formStack.setLayout(new BoxLayout(formStack, BoxLayout.Y_AXIS));
        formStack.setOpaque(false);

        // Welcome Header
        JLabel lblWelcome = new JLabel("Welcome back");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblWelcome.setForeground(new Color(30, 41, 59));
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubWelcome = new JLabel("Enter your receptionist credentials to continue.");
        lblSubWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubWelcome.setForeground(new Color(100, 116, 139));
        lblSubWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        formStack.add(Box.createVerticalStrut(10));
        formStack.add(lblWelcome);
        formStack.add(Box.createVerticalStrut(6));
        formStack.add(lblSubWelcome);
        formStack.add(Box.createVerticalStrut(28));

        // Username Field
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsername.setForeground(new Color(30, 41, 59));
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new PlaceholderTextField("Your username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formStack.add(lblUsername);
        formStack.add(Box.createVerticalStrut(6));
        formStack.add(usernameField);
        formStack.add(Box.createVerticalStrut(18));

        // Password Field
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPassword.setForeground(new Color(30, 41, 59));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new PlaceholderPasswordField("Your password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formStack.add(lblPassword);
        formStack.add(Box.createVerticalStrut(6));
        formStack.add(passwordField);
        formStack.add(Box.createVerticalStrut(18));

        // Remember Me Checkbox
        rememberMeCheckBox = new CustomCheckBox("Remember me on this device");
        rememberMeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        formStack.add(rememberMeCheckBox);
        formStack.add(Box.createVerticalStrut(24));

        // Sign In Button
        btnSignIn = new TealButton("Sign in to portal");
        btnSignIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnSignIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSignIn.addActionListener(e -> doLogin());

        formStack.add(btnSignIn);

        rightPanel.add(formStack, BorderLayout.CENTER);

        // Bottom Copyright / Sub-caption
        JLabel lblBottomCaption = new JLabel("BrightCare Medical Centre · Receptionist access", SwingConstants.CENTER);
        lblBottomCaption.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBottomCaption.setForeground(new Color(148, 163, 184));
        lblBottomCaption.setBorder(new EmptyBorder(12, 0, 0, 0));

        rightPanel.add(lblBottomCaption, BorderLayout.SOUTH);

        return rightPanel;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            ModernDialog.showMessage(this, "Missing Credentials",
                    "Please enter both username and password.",
                    ModernDialog.Type.WARNING);
            return;
        }

        try {
            if (authService == null) {
                ModernDialog.showMessage(this, "Service Unavailable",
                        "Authentication service is not connected. Please verify server status.",
                        ModernDialog.Type.ERROR);
                return;
            }

            User user = authService.login(username, password);

            if (user != null && user.getRole().toString().equalsIgnoreCase("RECEPTIONIST")) {
                if (rememberMeCheckBox.isSelected()) {
                    CredentialStore.save(username, password, "RECEPTIONIST");
                } else {
                    CredentialStore.clear();
                }

                ModernDialog.showMessage(this, "Welcome",
                        "Welcome back, Receptionist " + username + "!",
                        ModernDialog.Type.SUCCESS);

                dispose(); // Close login frame

                // Open Receptionist Dashboard Frame
                SwingUtilities.invokeLater(() -> new ReceptionistDashboardFrame(receptionistService).setVisible(true));

            } else {
                ModernDialog.showMessage(this, "Login Failed",
                        "Invalid credentials or not authorized as a receptionist account.",
                        ModernDialog.Type.ERROR);
            }
        } catch (Exception ex) {
            ModernDialog.showMessage(this, "Authentication Error",
                    "Login error: " + ex.getMessage(),
                    ModernDialog.Type.ERROR);
        }
    }

    private void loadSavedCredentials() {
        try {
            CredentialStore saved = CredentialStore.load();
            if (saved != null && !saved.isExpired() && "RECEPTIONIST".equals(saved.getRole())) {
                usernameField.setText(saved.getUsername());
                passwordField.setText(saved.getPassword());
                rememberMeCheckBox.setSelected(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load stored credentials: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReceptionistLoginFrame().setVisible(true));
    }


    public static class PlaceholderTextField extends JTextField {
        private String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(30, 41, 59));
            setCaretColor(new Color(30, 41, 59));
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fill rounded background
            g2.setColor(new Color(250, 250, 252));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            // Draw rounded border
            if (hasFocus()) {
                g2.setColor(new Color(18, 140, 126));
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            super.paintComponent(g);

            // Render ghost placeholder text if empty
            if (getText().isEmpty() && placeholder != null) {
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, getInsets().left, y);
            }
            g2.dispose();
        }
    }

    public static class PlaceholderPasswordField extends JPasswordField {
        private String placeholder;

        public PlaceholderPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(30, 41, 59));
            setCaretColor(new Color(30, 41, 59));
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(250, 250, 252));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            if (hasFocus()) {
                g2.setColor(new Color(18, 140, 126));
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            super.paintComponent(g);

            if (getPassword().length == 0 && placeholder != null) {
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, getInsets().left, y);
            }
            g2.dispose();
        }
    }

    public static class TealButton extends JButton {
        private Color baseBg = new Color(18, 140, 126);
        private Color hoverBg = new Color(14, 120, 108);

        public TealButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBackground(baseBg);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { setBackground(hoverBg); }
                @Override
                public void mouseExited(MouseEvent e) { setBackground(baseBg); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class CustomCheckBox extends JCheckBox {
        public CustomCheckBox(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(71, 85, 105));
            setOpaque(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    public static class ModernDialog extends JDialog {
        public enum Type { SUCCESS, INFO, WARNING, ERROR }

        public static void showMessage(Window owner, String title, String message, Type type) {
            ModernDialog dialog = new ModernDialog(owner, title, message, type);
            dialog.setVisible(true);
        }

        private ModernDialog(Window owner, String title, String message, Type type) {
            super(owner, title, ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            try {
                setBackground(new Color(0, 0, 0, 0));
            } catch (Exception ignored) {}

            setSize(460, 220);
            setLocationRelativeTo(owner);

            JPanel root = new JPanel(new BorderLayout(16, 16)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Fill rounded white card
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                    // Draw subtle rounded border
                    g2.setColor(new Color(226, 232, 240));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            root.setOpaque(false);
            root.setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));

            // Icon Panel
            JPanel iconBadge = createIconBadge(type);

            // Message Panel - Flush Left Alignment
            JPanel msgPanel = new JPanel();
            msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
            msgPanel.setOpaque(false);

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
            lblTitle.setForeground(new Color(30, 41, 59));
            lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            JTextArea txtMsg = new JTextArea(message);
            txtMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtMsg.setForeground(new Color(71, 85, 105));
            txtMsg.setWrapStyleWord(true);
            txtMsg.setLineWrap(true);
            txtMsg.setEditable(false);
            txtMsg.setFocusable(false);
            txtMsg.setOpaque(false);
            txtMsg.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
            txtMsg.setAlignmentX(Component.LEFT_ALIGNMENT);

            msgPanel.add(lblTitle);
            msgPanel.add(txtMsg);

            JPanel centerBox = new JPanel(new BorderLayout(16, 0));
            centerBox.setOpaque(false);
            centerBox.add(iconBadge, BorderLayout.WEST);
            centerBox.add(msgPanel, BorderLayout.CENTER);

            // Button Panel
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            btnPanel.setOpaque(false);

            TealButton btnOk = new TealButton("OK");
            btnOk.setPreferredSize(new Dimension(90, 36));
            btnOk.addActionListener(e -> dispose());
            btnPanel.add(btnOk);

            root.add(centerBox, BorderLayout.CENTER);
            root.add(btnPanel, BorderLayout.SOUTH);
            this.add(root);
        }

        private JPanel createIconBadge(Type type) {
            return new JPanel() {
                { setPreferredSize(new Dimension(48, 48)); setOpaque(false); }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                    Color bg, fg;
                    switch (type) {
                        case SUCCESS:
                            bg = new Color(220, 252, 231);
                            fg = new Color(22, 163, 74);
                            break;
                        case WARNING:
                            bg = new Color(254, 243, 199);
                            fg = new Color(217, 119, 6);
                            break;
                        case ERROR:
                            bg = new Color(254, 226, 226);
                            fg = new Color(220, 38, 38);
                            break;
                        default:
                            bg = new Color(224, 242, 254);
                            fg = new Color(14, 116, 144);
                            break;
                    }

                    // Background circle
                    g2.setColor(bg);
                    g2.fillOval(0, 0, 46, 46);

                    // Crisp vector icon stroke
                    g2.setColor(fg);
                    g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                    switch (type) {
                        case SUCCESS:
                            g2.drawLine(14, 24, 21, 31);
                            g2.drawLine(21, 31, 32, 16);
                            break;
                        case WARNING:
                            g2.drawLine(23, 13, 23, 26);
                            g2.fillOval(21, 30, 4, 4);
                            break;
                        case ERROR:
                            g2.drawLine(15, 15, 31, 31);
                            g2.drawLine(31, 15, 15, 31);
                            break;
                        default:
                            g2.fillOval(21, 12, 4, 4);
                            g2.drawLine(23, 20, 23, 32);
                            break;
                    }
                    g2.dispose();
                }
            };
        }
    }
}