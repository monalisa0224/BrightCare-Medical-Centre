package brigthcare_medical_centre.gui.receptionist;

import brigthcare_medical_centre.common.PatientInfo;
import brigthcare_medical_centre.common.ReceptionistInterface;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * ReceptionistDashboardFrame - Modernized UI matching BrightCare Medical Centre theme.
 * Java 8 compliant syntax.
 */
public class ReceptionistDashboardFrame extends JFrame {

    private ReceptionistInterface receptionistService;

    // GUI Components for Registration
    private PlaceholderTextField txtUsername;
    private PlaceholderTextField txtPassword;
    private PlaceholderTextField txtFirstName;
    private PlaceholderTextField txtLastName;
    private PlaceholderTextField txtIcPassport;
    private PlaceholderTextField txtContact;
    private PlaceholderTextField txtAddress;

    // GUI Components for Management
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private PlaceholderTextField txtSearch;

    // Navigation and Layout Components
    private CardLayout cardLayout;
    private JPanel contentCardPanel;
    private NavTabButton btnRegisterTab;
    private NavTabButton btnManageTab;

    public ReceptionistDashboardFrame() {
        this(null);
    }

    public ReceptionistDashboardFrame(ReceptionistInterface receptionistService) {
        super("BrightCare Clinic - Receptionist Portal");
        this.receptionistService = receptionistService;
        initUI();
        refreshPatientTable();
    }

    private void initUI() {
        this.setSize(960, 720);
        this.setMinimumSize(new Dimension(880, 660));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        // Top Navy Header Banner
        this.add(createHeaderPanel(), BorderLayout.NORTH);

        // Center Panel with Navigation Tabs and Card Layout Content
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(new Color(244, 246, 249));

        // Custom Tab Strip
        centerContainer.add(createTabStripPanel(), BorderLayout.NORTH);

        // Main Card Stack (Register Patient vs Manage Patients)
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setOpaque(false);

        contentCardPanel.add(createRegistrationPanel(), "REGISTER");
        contentCardPanel.add(createManagementPanel(), "MANAGE");

        centerContainer.add(contentCardPanel, BorderLayout.CENTER);
        this.add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 108, 172)); // Modern navy blue accent
        header.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));

        // Left Header Titles
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Receptionist Portal");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubTitle = new JLabel("Patient registration and profile management");
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubTitle.setForeground(new Color(208, 227, 243));

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(lblSubTitle);

        header.add(titleBox, BorderLayout.WEST);

        // Right Sign Out Button
        RoundedButton btnLogout = new RoundedButton("Sign out", new Color(217, 83, 79), 8);
        btnLogout.setPreferredSize(new Dimension(100, 36));
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.addActionListener(e -> logout());

        JPanel rightBox = new JPanel(new GridBagLayout());
        rightBox.setOpaque(false);
        rightBox.add(btnLogout);

        header.add(rightBox, BorderLayout.EAST);

        return header;
    }

    private JPanel createTabStripPanel() {
        JPanel tabStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        tabStrip.setBackground(Color.WHITE);
        tabStrip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 233, 240)));

        btnRegisterTab = new NavTabButton("Register patient");
        btnManageTab = new NavTabButton("Manage patients");

        btnRegisterTab.setActive(true);

        btnRegisterTab.addActionListener(e -> {
            btnRegisterTab.setActive(true);
            btnManageTab.setActive(false);
            cardLayout.show(contentCardPanel, "REGISTER");
        });

        btnManageTab.addActionListener(e -> {
            btnManageTab.setActive(true);
            btnRegisterTab.setActive(false);
            cardLayout.show(contentCardPanel, "MANAGE");
        });

        tabStrip.add(btnRegisterTab);
        tabStrip.add(btnManageTab);

        return tabStrip;
    }

    private JPanel createRegistrationPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(false);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // White Card Container
        RoundedCardPanel card = new RoundedCardPanel();
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // Card Header Text
        JPanel cardHeader = new JPanel();
        cardHeader.setLayout(new BoxLayout(cardHeader, BoxLayout.Y_AXIS));
        cardHeader.setOpaque(false);

        JLabel lblCardTitle = new JLabel("Register a new patient");
        lblCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblCardTitle.setForeground(new Color(30, 41, 59));

        JLabel lblCardSub = new JLabel("Complete all details below. Medical Record ID is generated securely by the system.");
        lblCardSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCardSub.setForeground(new Color(100, 116, 139));

        cardHeader.add(lblCardTitle);
        cardHeader.add(Box.createVerticalStrut(6));
        cardHeader.add(lblCardSub);

        card.add(cardHeader, BorderLayout.NORTH);

        // Form Fields (2-Column Grid Layout)
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 12, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        txtFirstName = new PlaceholderTextField("e.g. John");
        txtLastName = new PlaceholderTextField("e.g. Tan");
        txtIcPassport = new PlaceholderTextField("e.g. 900101-14-1234");
        txtContact = new PlaceholderTextField("e.g. 012 345 6789");
        txtUsername = new PlaceholderTextField("e.g. johntan");
        txtPassword = new PlaceholderTextField("Create a secure password");
        txtAddress = new PlaceholderTextField("Street, city, postcode");

        // Row 1: First Name & Last Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        formGrid.add(createFieldGroup("First name", txtFirstName), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        formGrid.add(createFieldGroup("Last name", txtLastName), gbc);

        // Row 2: IC / Passport & Contact Number
        gbc.gridx = 0; gbc.gridy = 1;
        formGrid.add(createFieldGroup("IC / Passport number", txtIcPassport), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        formGrid.add(createFieldGroup("Contact number", txtContact), gbc);

        // Row 3: Username & Temporary Password
        gbc.gridx = 0; gbc.gridy = 2;
        formGrid.add(createFieldGroup("Patient username", txtUsername), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        formGrid.add(createFieldGroup("Temporary password", txtPassword), gbc);

        // Row 4: Home Address (Spans Full Width)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
        formGrid.add(createFieldGroup("Home address", txtAddress), gbc);

        card.add(formGrid, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(12, 8, 0, 8));

        JLabel lblRequired = new JLabel("All fields are required");
        lblRequired.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRequired.setForeground(new Color(148, 163, 184));

        RoundedButton btnRegister = new RoundedButton("Register patient", new Color(22, 155, 139), 8);
        btnRegister.setPreferredSize(new Dimension(160, 42));
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.addActionListener(e -> registerPatient());

        footerPanel.add(lblRequired, BorderLayout.WEST);
        footerPanel.add(btnRegister, BorderLayout.EAST);

        card.add(footerPanel, BorderLayout.SOUTH);

        outerPanel.add(card, BorderLayout.NORTH);
        return outerPanel;
    }

    private JPanel createFieldGroup(String labelText, PlaceholderTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(30, 41, 59));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createManagementPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(false);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        RoundedCardPanel card = new RoundedCardPanel();
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Top Action Bar
        JPanel topPanel = new JPanel(new BorderLayout(12, 0));
        topPanel.setOpaque(false);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBox.setOpaque(false);

        txtSearch = new PlaceholderTextField("Search by username, first name, last name, IC/passport, or record ID...");
        txtSearch.setPreferredSize(new Dimension(420, 38));
        txtSearch.addActionListener(e -> searchPatients());

        RoundedButton btnSearch = new RoundedButton("Search", new Color(15, 108, 172), 6);
        btnSearch.setPreferredSize(new Dimension(90, 38));

        RoundedButton btnRefresh = new RoundedButton("Refresh All", new Color(100, 116, 139), 6);
        btnRefresh.setPreferredSize(new Dimension(110, 38));

        searchBox.add(txtSearch);
        searchBox.add(btnSearch);
        searchBox.add(btnRefresh);

        topPanel.add(searchBox, BorderLayout.WEST);
        card.add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Patient ID", "Username", "First Name", "Last Name", "IC/Passport", "Medical Record ID", "Contact Number", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        patientTable = new JTable(tableModel);
        patientTable.setRowHeight(36);
        patientTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        patientTable.setSelectionBackground(new Color(230, 247, 245));
        patientTable.setSelectionForeground(new Color(22, 155, 139));
        patientTable.setGridColor(new Color(229, 233, 240));
        patientTable.setShowVerticalLines(false);

        JTableHeader tableHeader = patientTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(241, 245, 249));
        tableHeader.setForeground(new Color(30, 41, 59));
        tableHeader.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.LEFT);
        patientTable.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 233, 240)));
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        RoundedButton btnEdit = new RoundedButton("Edit Selected", new Color(243, 156, 18), 6);
        btnEdit.setPreferredSize(new Dimension(120, 38));

        RoundedButton btnDelete = new RoundedButton("Delete Selected", new Color(217, 83, 79), 6);
        btnDelete.setPreferredSize(new Dimension(130, 38));

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        card.add(bottomPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> searchPatients());
        btnRefresh.addActionListener(e -> refreshPatientTable());
        btnDelete.addActionListener(e -> deleteSelectedPatient());
        btnEdit.addActionListener(e -> editSelectedPatient());

        outerPanel.add(card, BorderLayout.CENTER);
        return outerPanel;
    }

    private void registerPatient() {
        if (receptionistService == null) return;

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String icPassport = txtIcPassport.getText().trim();
        String contact = txtContact.getText().trim();
        String address = txtAddress.getText().trim();

        if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                || icPassport.isEmpty() || contact.isEmpty() || address.isEmpty()) {
            ModernDialog.showMessage(this, "Missing Information",
                    "Registration Failed: All fields must be filled out before registering a patient.",
                    ModernDialog.Type.WARNING);
            return;
        }

        try {
            PatientInfo newPatient = new PatientInfo(
                    username, password, firstName, lastName, icPassport, contact, address
            );

            boolean success = receptionistService.registerPatient(newPatient);

            if (success) {
                String medicalRecordId = findMedicalRecordId(username);
                String msg = "Patient " + username + " successfully registered!\nMedical Record ID: "
                        + (medicalRecordId == null ? "Available in patient list." : medicalRecordId);
                ModernDialog.showMessage(this, "Registration Success", msg, ModernDialog.Type.SUCCESS);

                txtUsername.setText(""); txtPassword.setText(""); txtFirstName.setText(""); txtLastName.setText("");
                txtIcPassport.setText(""); txtContact.setText(""); txtAddress.setText("");
                refreshPatientTable();
            } else {
                ModernDialog.showMessage(this, "Registration Error",
                        "Failed to register. Username already exists.", ModernDialog.Type.ERROR);
            }
        } catch (Exception ex) {
            ModernDialog.showMessage(this, "Server Error", ex.getMessage(), ModernDialog.Type.ERROR);
        }
    }

    private String findMedicalRecordId(String username) throws Exception {
        List<PatientInfo> patients = receptionistService.searchPatient(username);
        for (PatientInfo patient : patients) {
            if (username.equals(patient.getUsername())) {
                return patient.getMedicalRecordId();
            }
        }
        return null;
    }

    private void editSelectedPatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            ModernDialog.showMessage(this, "Selection Required",
                    "Please select a patient from the table first.", ModernDialog.Type.WARNING);
            return;
        }

        int patientId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String currentFirstName = (String) tableModel.getValueAt(selectedRow, 2);
        String currentLastName = (String) tableModel.getValueAt(selectedRow, 3);
        String currentIcPassport = (String) tableModel.getValueAt(selectedRow, 4);
        String currentMedicalRecord = (String) tableModel.getValueAt(selectedRow, 5);
        String currentContact = (String) tableModel.getValueAt(selectedRow, 6);
        String currentAddress = (String) tableModel.getValueAt(selectedRow, 7);

        PatientInfo patient = new PatientInfo(patientId, username, currentFirstName, currentLastName,
                currentIcPassport, currentMedicalRecord, currentContact, currentAddress);

        EditPatientDialog editDialog = new EditPatientDialog(this, patient);
        editDialog.setVisible(true);

        if (editDialog.isSaved()) {
            try {
                PatientInfo updatedPatient = editDialog.getUpdatedPatient();
                boolean success = receptionistService.updatePatient(updatedPatient);

                if (success) {
                    ModernDialog.showMessage(this, "Success",
                            "Patient profile updated successfully!", ModernDialog.Type.SUCCESS);
                    refreshPatientTable();
                } else {
                    ModernDialog.showMessage(this, "Database Error",
                            "Failed to update patient profile.", ModernDialog.Type.ERROR);
                }
            } catch (Exception ex) {
                ModernDialog.showMessage(this, "Server Error", ex.getMessage(), ModernDialog.Type.ERROR);
            }
        }
    }

    private void logout() {
        boolean confirm = ModernDialog.showConfirm(this, "Logout Confirmation", "Are you sure you want to sign out?");
        if (confirm) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                try {
                    Class<?> loginClass = Class.forName("brigthcare_medical_centre.gui.receptionist.ReceptionistLoginFrame");
                    JFrame loginFrame = (JFrame) loginClass.getDeclaredConstructor().newInstance();
                    loginFrame.setVisible(true);
                } catch (Exception e) {
                    System.out.println("Returned to login screen.");
                }
            });
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
            String keyword = txtSearch.getText().trim().toLowerCase();

            // If query is empty, reload full patient list
            if (keyword.isEmpty()) {
                refreshPatientTable();
                return;
            }

            // Fetch all patients and filter across Username, First Name, Last Name, Full Name, IC/Passport & Medical Record ID
            List<PatientInfo> allPatients = receptionistService.getAllPatients();
            List<PatientInfo> searchResults = new java.util.ArrayList<PatientInfo>();

            if (allPatients != null) {
                for (PatientInfo p : allPatients) {
                    boolean matchesUsername = p.getUsername() != null && p.getUsername().toLowerCase().contains(keyword);
                    boolean matchesFirstName = p.getFirstName() != null && p.getFirstName().toLowerCase().contains(keyword);
                    boolean matchesLastName = p.getLastName() != null && p.getLastName().toLowerCase().contains(keyword);
                    boolean matchesFullName = (p.getFirstName() != null && p.getLastName() != null) && 
                            (p.getFirstName().toLowerCase() + " " + p.getLastName().toLowerCase()).contains(keyword);
                    boolean matchesIcPassport = p.getIcPassportNumber() != null && p.getIcPassportNumber().toLowerCase().contains(keyword);
                    boolean matchesMedicalRecordId = p.getMedicalRecordId() != null && p.getMedicalRecordId().toLowerCase().contains(keyword);

                    if (matchesUsername || matchesFirstName || matchesLastName || matchesFullName || matchesIcPassport || matchesMedicalRecordId) {
                        searchResults.add(p);
                    }
                }
            }

            updateTableData(searchResults);
        } catch (Exception ex) {
            ModernDialog.showMessage(this, "Search Failed", ex.getMessage(), ModernDialog.Type.ERROR);
        }
    }

    private void updateTableData(List<PatientInfo> patients) {
        tableModel.setRowCount(0);
        if (patients == null) return;
        for (PatientInfo p : patients) {
            tableModel.addRow(new Object[]{
                    p.getId(), p.getUsername(), p.getFirstName(), p.getLastName(), p.getIcPassportNumber(),
                    p.getMedicalRecordId(), p.getContactNumber(), p.getAddress()
            });
        }
    }

    private void deleteSelectedPatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            ModernDialog.showMessage(this, "Selection Required",
                    "Please select a patient from the table first.", ModernDialog.Type.WARNING);
            return;
        }

        int patientId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        boolean confirm = ModernDialog.showConfirm(this, "Confirm Delete",
                "Are you sure you want to completely delete patient: " + username + "?");

        if (confirm) {
            try {
                boolean success = receptionistService.deletePatient(patientId);
                if (success) {
                    ModernDialog.showMessage(this, "Deleted",
                            "Patient " + username + " deleted successfully.", ModernDialog.Type.SUCCESS);
                    refreshPatientTable();
                } else {
                    ModernDialog.showMessage(this, "Error", "Failed to delete patient.", ModernDialog.Type.ERROR);
                }
            } catch (Exception ex) {
                ModernDialog.showMessage(this, "Server Error", ex.getMessage(), ModernDialog.Type.ERROR);
            }
        }
    }

    /* ========================================================================
       CUSTOM UI HELPER COMPONENTS (Placeholders, Cards, Rounded Buttons, Tabs)
       ======================================================================== */

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
            g2.setColor(new Color(252, 253, 255));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            // Draw rounded border
            if (hasFocus()) {
                g2.setColor(new Color(22, 155, 139));
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            super.paintComponent(g);

            // Render ghost placeholder text if text is empty
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

    public static class RoundedCardPanel extends JPanel {
        private int cornerRadius = 14;
        private Color borderColor = new Color(229, 233, 240);

        public RoundedCardPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class RoundedButton extends JButton {
        private Color baseBg;
        private Color hoverBg;
        private int cornerRadius;

        public RoundedButton(String text, Color bg, int cornerRadius) {
            super(text);
            this.baseBg = bg;
            this.hoverBg = bg.brighter();
            this.cornerRadius = cornerRadius;
            setBackground(bg);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

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
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class NavTabButton extends JButton {
        private boolean active = false;
        private Color activeColor = new Color(22, 155, 139);
        private Color inactiveColor = new Color(85, 96, 110);

        public NavTabButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setForeground(inactiveColor);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        }

        public void setActive(boolean active) {
            this.active = active;
            setForeground(active ? activeColor : inactiveColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (active) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(activeColor);
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                g2.dispose();
            }
        }
    }

    /* ========================================================================
       MODERN POP-UP DIALOGS (Java 8 Compliant & Left-Aligned)
       ======================================================================== */

    public static class ModernDialog extends JDialog {
        public enum Type { SUCCESS, INFO, WARNING, ERROR }
        private boolean confirmed = false;

        public static void showMessage(Window owner, String title, String message, Type type) {
            ModernDialog dialog = new ModernDialog(owner, title, message, type, false);
            dialog.setVisible(true);
        }

        public static boolean showConfirm(Window owner, String title, String message) {
            ModernDialog dialog = new ModernDialog(owner, title, message, Type.WARNING, true);
            dialog.setVisible(true);
            return dialog.confirmed;
        }

        private ModernDialog(Window owner, String title, String message, Type type, boolean isConfirm) {
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

                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

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
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            btnPanel.setOpaque(false);

            if (isConfirm) {
                RoundedButton btnCancel = new RoundedButton("Cancel", new Color(148, 163, 184), 6);
                btnCancel.setPreferredSize(new Dimension(90, 36));
                btnCancel.addActionListener(e -> {
                    confirmed = false;
                    dispose();
                });

                RoundedButton btnYes = new RoundedButton("Confirm", new Color(22, 155, 139), 6);
                btnYes.setPreferredSize(new Dimension(100, 36));
                btnYes.addActionListener(e -> {
                    confirmed = true;
                    dispose();
                });

                btnPanel.add(btnCancel);
                btnPanel.add(btnYes);
            } else {
                Color btnBg;
                switch (type) {
                    case SUCCESS:
                        btnBg = new Color(22, 155, 139);
                        break;
                    case ERROR:
                        btnBg = new Color(217, 83, 79);
                        break;
                    case WARNING:
                        btnBg = new Color(243, 156, 18);
                        break;
                    default:
                        btnBg = new Color(15, 108, 172);
                        break;
                }

                RoundedButton btnOk = new RoundedButton("OK", btnBg, 6);
                btnOk.setPreferredSize(new Dimension(90, 36));
                btnOk.addActionListener(e -> dispose());
                btnPanel.add(btnOk);
            }

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

                    g2.setColor(bg);
                    g2.fillOval(0, 0, 46, 46);

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

    public static class EditPatientDialog extends JDialog {
        private boolean saved = false;
        private int patientId;
        private String username;
        private String medicalRecordId;

        private PlaceholderTextField txtFirstName;
        private PlaceholderTextField txtLastName;
        private PlaceholderTextField txtIcPassport;
        private PlaceholderTextField txtContact;
        private PlaceholderTextField txtAddress;

        public EditPatientDialog(Window owner, PatientInfo patient) {
            super(owner, "Update Patient Profile", ModalityType.APPLICATION_MODAL);
            this.patientId = patient.getId();
            this.username = patient.getUsername();
            this.medicalRecordId = patient.getMedicalRecordId();

            setUndecorated(true);
            try {
                setBackground(new Color(0, 0, 0, 0));
            } catch (Exception ignored) {}

            setSize(560, 530);
            setLocationRelativeTo(owner);

            JPanel root = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                    g2.setColor(new Color(203, 213, 225));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            root.setOpaque(false);

            // Header Banner
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(15, 108, 172));
            header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

            JLabel lblTitle = new JLabel("Update Patient Profile");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTitle.setForeground(Color.WHITE);

            JLabel lblSub = new JLabel("Modify contact details and profile information below");
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSub.setForeground(new Color(208, 227, 243));

            JPanel titleBox = new JPanel();
            titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
            titleBox.setOpaque(false);
            titleBox.add(lblTitle);
            titleBox.add(Box.createVerticalStrut(2));
            titleBox.add(lblSub);

            header.add(titleBox, BorderLayout.WEST);
            root.add(header, BorderLayout.NORTH);

            // Center Form Container
            JPanel content = new JPanel(new BorderLayout(0, 16));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

            // Read-Only Summary Pill
            JPanel infoPill = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
            infoPill.setBackground(new Color(241, 245, 249));
            infoPill.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

            JLabel lblUsernamePill = new JLabel("Username: " + username);
            lblUsernamePill.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblUsernamePill.setForeground(new Color(30, 41, 59));

            JLabel lblMRIdPill = new JLabel("Record ID: " + (medicalRecordId != null ? medicalRecordId : "N/A"));
            lblMRIdPill.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblMRIdPill.setForeground(new Color(15, 108, 172));

            infoPill.add(lblUsernamePill);
            infoPill.add(new JLabel("•"));
            infoPill.add(lblMRIdPill);

            content.add(infoPill, BorderLayout.NORTH);

            // Form Grid
            JPanel formGrid = new JPanel(new GridBagLayout());
            formGrid.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 10, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0.5;

            txtFirstName = new PlaceholderTextField("First name");
            txtFirstName.setText(patient.getFirstName());

            txtLastName = new PlaceholderTextField("Last name");
            txtLastName.setText(patient.getLastName());

            txtIcPassport = new PlaceholderTextField("IC / Passport");
            txtIcPassport.setText(patient.getIcPassportNumber());

            txtContact = new PlaceholderTextField("Contact number");
            txtContact.setText(patient.getContactNumber());

            txtAddress = new PlaceholderTextField("Address");
            txtAddress.setText(patient.getAddress());

            gbc.gridx = 0; gbc.gridy = 0;
            formGrid.add(createFieldGroup("First Name", txtFirstName), gbc);

            gbc.gridx = 1; gbc.gridy = 0;
            formGrid.add(createFieldGroup("Last Name", txtLastName), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            formGrid.add(createFieldGroup("IC / Passport Number", txtIcPassport), gbc);

            gbc.gridx = 1; gbc.gridy = 1;
            formGrid.add(createFieldGroup("Contact Number", txtContact), gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
            formGrid.add(createFieldGroup("Home Address", txtAddress), gbc);

            content.add(formGrid, BorderLayout.CENTER);

            // Footer Buttons
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            footer.setOpaque(false);

            RoundedButton btnCancel = new RoundedButton("Cancel", new Color(148, 163, 184), 6);
            btnCancel.setPreferredSize(new Dimension(100, 38));
            btnCancel.addActionListener(e -> dispose());

            RoundedButton btnSave = new RoundedButton("Save Changes", new Color(22, 155, 139), 6);
            btnSave.setPreferredSize(new Dimension(130, 38));
            btnSave.addActionListener(e -> {
                saved = true;
                dispose();
            });

            footer.add(btnCancel);
            footer.add(btnSave);

            content.add(footer, BorderLayout.SOUTH);
            root.add(content, BorderLayout.CENTER);

            this.add(root);
        }

        private JPanel createFieldGroup(String labelText, PlaceholderTextField field) {
            JPanel panel = new JPanel(new BorderLayout(0, 4));
            panel.setOpaque(false);

            JLabel label = new JLabel(labelText);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(new Color(51, 65, 85));

            panel.add(label, BorderLayout.NORTH);
            panel.add(field, BorderLayout.CENTER);
            return panel;
        }

        public boolean isSaved() {
            return saved;
        }

        public PatientInfo getUpdatedPatient() {
            return new PatientInfo(patientId, username,
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    txtIcPassport.getText().trim(),
                    medicalRecordId,
                    txtContact.getText().trim(),
                    txtAddress.getText().trim());
        }
    }
}