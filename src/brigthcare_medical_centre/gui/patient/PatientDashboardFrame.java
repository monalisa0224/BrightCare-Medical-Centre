package brigthcare_medical_centre.gui.patient;

import brigthcare_medical_centre.common.PatientInterface;
import brigthcare_medical_centre.util.Constants;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class PatientDashboardFrame extends JFrame {

    private PatientInterface patientService;
    private String loggedInUsername;

    public PatientDashboardFrame(String username) {
        this.loggedInUsername = username;
        connectToServer();
        buildUI();
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(
                    Constants.RMI_HOST, Constants.RMI_PORT);
            patientService = (PatientInterface) registry.lookup(Constants.PATIENT_SERVICE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to server:\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

private void buildUI() {
    setTitle("BrightCare Medical Centre - Patient Portal");
    setSize(750, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    getContentPane().setBackground(new Color(245, 247, 250));

    // ── HEADER ──
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(new Color(41, 128, 185));
    headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

    JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    headerLeft.setBackground(new Color(41, 128, 185));
    JLabel headerTitle = new JLabel("Patient Portal  |  " + loggedInUsername);
    headerTitle.setFont(new Font("Times New Roman", Font.BOLD, 18));
    headerTitle.setForeground(Color.WHITE);
    headerLeft.add(headerTitle);
    headerPanel.add(headerLeft, BorderLayout.WEST);

    JButton btnLogout = new JButton("Logout");
    btnLogout.setFont(new Font("Times New Roman", Font.BOLD, 12));
    btnLogout.setBackground(new Color(192, 57, 43));
    btnLogout.setForeground(Color.WHITE);
    btnLogout.setFocusPainted(false);
    btnLogout.setBorderPainted(false);
    btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnLogout.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new PatientLoginFrame();
        }
    });
    headerPanel.add(btnLogout, BorderLayout.EAST);
    add(headerPanel, BorderLayout.NORTH);

    // ── WELCOME BAR ──
    JPanel welcomeBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
    welcomeBar.setBackground(new Color(214, 234, 248));
    welcomeBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(174, 214, 241)));
    JLabel welcomeLabel = new JLabel("Welcome back, " + loggedInUsername + "!   How are you today?");
    welcomeLabel.setFont(new Font("Times New Roman", Font.PLAIN, 13));
    welcomeLabel.setForeground(new Color(31, 97, 141));
    welcomeBar.add(welcomeLabel);

    // ── CARD GRID ──
    JPanel cardGrid = new JPanel(new GridLayout(3, 2, 15, 15));
    cardGrid.setBackground(new Color(245, 247, 250));
    cardGrid.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

    JPanel card1 = createCard("Update Personal Info",
        "Edit your contact number and address",
        new Color(52, 152, 219), e -> doUpdateInfo());

    JPanel card2 = createCard("View My Profile",
        "See your registered personal details",
        new Color(52, 152, 219), e -> doViewProfile());

    JPanel card3 = createCard("Book Appointment",
        "Schedule a new doctor appointment",
        new Color(39, 174, 96), e -> doBookAppointment());

    JPanel card4 = createCard("Cancel Appointment",
        "Cancel a confirmed appointment",
        new Color(192, 57, 43), e -> doCancelAppointment());

    JPanel card5 = createCard("View Appointment Schedule",
        "See your pending & upcoming appointments",
        new Color(41, 128, 185), e -> doViewSchedule());

    JPanel card6 = createCard("View Appointment History",
        "View past, cancelled & completed records",
        new Color(41, 128, 185), e -> doViewHistory());

    cardGrid.add(card1);
    cardGrid.add(card2);
    cardGrid.add(card3);
    cardGrid.add(card4);
    cardGrid.add(card5);
    cardGrid.add(card6);

    // ── BOTTOM FULL WIDTH CARD ──
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setBackground(new Color(245, 247, 250));
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 25));

    JPanel card7 = createCard("Check Doctor Availability",
        "Find available time slots for any doctor on a selected date",
        new Color(142, 68, 173), e -> doCheckAvailability());
    bottomPanel.add(card7, BorderLayout.CENTER);

    // ── ASSEMBLE ──
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(new Color(245, 247, 250));
    mainPanel.add(welcomeBar, BorderLayout.NORTH);
    mainPanel.add(cardGrid, BorderLayout.CENTER);
    mainPanel.add(bottomPanel, BorderLayout.SOUTH);

    add(mainPanel, BorderLayout.CENTER);
    setLocationRelativeTo(null);
    setVisible(true);
}

private JPanel createCard(String title, String description, Color accentColor,
                           java.awt.event.ActionListener action) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
        BorderFactory.createEmptyBorder(0, 0, 10, 0)
    ));
    card.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Colored top accent bar
    JPanel accentBar = new JPanel();
    accentBar.setBackground(accentColor);
    accentBar.setPreferredSize(new Dimension(0, 6));
    card.add(accentBar, BorderLayout.NORTH);

    // Text content
    JPanel textPanel = new JPanel(new GridLayout(2, 1, 2, 2));
    textPanel.setBackground(Color.WHITE);
    textPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
    titleLabel.setForeground(new Color(44, 62, 80));

    JLabel descLabel = new JLabel(description);
    descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    descLabel.setForeground(new Color(127, 140, 141));

    textPanel.add(titleLabel);
    textPanel.add(descLabel);
    card.add(textPanel, BorderLayout.CENTER);

    // Click action on whole card
    card.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            action.actionPerformed(null);
        }
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            card.setBackground(new Color(245, 247, 250));
            textPanel.setBackground(new Color(245, 247, 250));
        }
        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            card.setBackground(Color.WHITE);
            textPanel.setBackground(Color.WHITE);
        }
    });

    return card;
}
    
    private void doUpdateInfo() {
    try {
        String[] profile = patientService.getPatientProfile(loggedInUsername);
        String currentContact = (profile != null && profile[1] != null) ? profile[1] : "";
        String currentAddress = (profile != null && profile[2] != null) ? profile[2] : "";

        JDialog dialog = new JDialog(this, "Update Personal Info", true);
        dialog.setSize(460, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // ── HEADER ──
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(52, 152, 219));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("Update Personal Information");
        title.setFont(new Font("Times New Roman", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Edit your contact number and address below");
        subtitle.setFont(new Font("Times New Roman", Font.ITALIC, 12));
        subtitle.setForeground(new Color(200, 230, 255));
        header.add(title);
        header.add(subtitle);
        dialog.add(header, BorderLayout.NORTH);

        // ── FORM ──
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        // Contact field
        JLabel contactLabel = new JLabel("Contact Number");
        contactLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));
        contactLabel.setForeground(new Color(44, 62, 80));

        JTextField contactField = new JTextField(currentContact);
        contactField.setFont(new Font("Times New Roman", Font.PLAIN, 13));        
        contactField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(12, 10, 12, 10)  // ← 12 top/bottom instead of 8
        ));

        JLabel contactHint = new JLabel("e.g. 0123456789 (7-15 digits)");
        contactHint.setFont(new Font("Times New Roman", Font.ITALIC, 11));
        contactHint.setForeground(Color.GRAY);

        // Address field
        JLabel addressLabel = new JLabel("Address");
        addressLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));
        addressLabel.setForeground(new Color(44, 62, 80));

        JTextField addressField = new JTextField(currentAddress);
        addressField.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        addressField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(12, 10, 12, 10)  // ← same padding
        ));


        JLabel addressHint = new JLabel("e.g. Kuala Lumpur, Bukit Jalil");
        addressHint.setFont(new Font("Times New Roman", Font.ITALIC, 11));
        addressHint.setForeground(Color.GRAY);

        // Layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(contactLabel, gbc);
        gbc.gridy = 1;
        formPanel.add(contactField, gbc);
        gbc.gridy = 2;
        formPanel.add(contactHint, gbc);
        gbc.gridy = 3;
        formPanel.add(Box.createVerticalStrut(15), gbc); 
        gbc.gridy = 4;
        formPanel.add(addressLabel, gbc);
        gbc.gridy = 5;
        formPanel.add(addressField, gbc);
        gbc.gridy = 6;
        formPanel.add(addressHint, gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

        // ── FOOTER ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton cancelBtn = makeDialogBtn("Cancel", new Color(150, 150, 150));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = makeDialogBtn("Save Changes", new Color(39, 174, 96));
        saveBtn.setPreferredSize(new Dimension(120, 35));
        saveBtn.addActionListener(e -> {
            String newContact = contactField.getText().trim();
            String newAddress = addressField.getText().trim();

            // Validate
            if (!newContact.isEmpty() && !newContact.matches("[0-9+\\-\\s]+")) {
                contactField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(192, 57, 43), 2),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                JOptionPane.showMessageDialog(dialog,
                    "Invalid contact number!\nOnly numbers, +, - and spaces allowed.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String digitsOnly = newContact.replaceAll("[^0-9]", "");
            if (!newContact.isEmpty() && (digitsOnly.length() < 7 || digitsOnly.length() > 15)) {
                contactField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(192, 57, 43), 2),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                JOptionPane.showMessageDialog(dialog,
                    "Contact number must be 7-15 digits.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newContact.isEmpty()) newContact = currentContact;
            if (newAddress.isEmpty()) newAddress = currentAddress;

            try {
                boolean ok = patientService.updatePersonalInfo(
                    loggedInUsername, newContact, newAddress);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog,
                        "Profile updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Update failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        footer.add(cancelBtn);
        footer.add(saveBtn);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

private void doBookAppointment() {
    try {
        List<String[]> doctors = patientService.getDoctors();
        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No doctors available.");
            return;
        }

        // ── CUSTOM DIALOG ──
        JDialog dialog = new JDialog(this, "Book New Appointment", true);
        dialog.setSize(540, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(39, 174, 96));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel headerLabel = new JLabel("Book New Appointment");
        headerLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        header.add(headerLabel, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        // ── FORM ──
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Doctor dropdown
        String[] doctorOptions = new String[doctors.size()];
        for (int i = 0; i < doctors.size(); i++) {
            String[] d = doctors.get(i);
            doctorOptions[i] = d[1] + " — " + d[2];
        }
        JComboBox<String> doctorDropdown = new JComboBox<>(doctorOptions);
        doctorDropdown.setFont(new Font("Times New Roman", Font.PLAIN, 13));

        // Date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel(
            new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(new Font("Times New Roman", Font.PLAIN, 13));

        JLabel doctorLabel = new JLabel("Select Doctor:");
        doctorLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));
        JLabel dateLabel = new JLabel("Select Date:");
        dateLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(doctorLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(doctorDropdown, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formPanel.add(dateLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(dateSpinner, gbc);

        // Search button
        JButton searchBtn = new JButton("Search Available Slots");
        searchBtn.setFont(new Font("Times New Roman", Font.BOLD, 13));
        searchBtn.setBackground(new Color(41, 128, 185));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(searchBtn, gbc);

        // ── SLOTS AREA ──
        JPanel slotsPanel = new JPanel(new BorderLayout());
        slotsPanel.setBackground(Color.WHITE);
        slotsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 25, 8, 25)
        ));

        JLabel slotsTitle = new JLabel("Available Time Slots — click a slot to select:");
        slotsTitle.setFont(new Font("Times New Roman", Font.BOLD, 13));
        slotsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        slotsPanel.add(slotsTitle, BorderLayout.NORTH);

        JPanel slotButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        slotButtonPanel.setBackground(Color.WHITE);
        JLabel hintLabel = new JLabel("Click 'Search Available Slots' above to begin.");
        hintLabel.setFont(new Font("Times New Roman", Font.ITALIC, 12));
        hintLabel.setForeground(Color.GRAY);
        slotButtonPanel.add(hintLabel);
        slotsPanel.add(slotButtonPanel, BorderLayout.CENTER);

        // ── BOTTOM ──
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));

        JLabel selectedSlotLabel = new JLabel("No slot selected.");
        selectedSlotLabel.setFont(new Font("Times New Roman", Font.ITALIC, 12));
        selectedSlotLabel.setForeground(Color.GRAY);

        JButton confirmBtn = new JButton("Confirm Booking ›");
        confirmBtn.setFont(new Font("Times New Roman", Font.BOLD, 13));
        confirmBtn.setBackground(new Color(39, 174, 96));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmBtn.setEnabled(false);
        confirmBtn.setOpaque(true);
        confirmBtn.setBackground(new Color(180, 180, 180));  // grey when disabled
        confirmBtn.setForeground(new Color(80, 80, 80));     // dark grey text
        bottomPanel.add(confirmBtn, BorderLayout.EAST);

        // ── ASSEMBLE ──
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(slotsPanel, BorderLayout.CENTER);
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        // ── STATE ──
        final String[] selectedTime = {null};
        final String[] selectedDate = {null};

        // ── SEARCH ACTION ──
        searchBtn.addActionListener(e -> {
            int idx = doctorDropdown.getSelectedIndex();
            int doctorId = Integer.parseInt(doctors.get(idx)[0]);
            java.util.Date sd = (java.util.Date) dateSpinner.getValue();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            selectedDate[0] = sdf.format(sd);

            try {
                List<String> slots = patientService.checkDoctorAvailability(doctorId, selectedDate[0]);
                slotButtonPanel.removeAll();
                selectedTime[0] = null;
                selectedSlotLabel.setText("No slot selected.");
                selectedSlotLabel.setForeground(Color.GRAY);
                confirmBtn.setEnabled(false);

                if (slots.isEmpty()) {
                    JLabel empty = new JLabel("No available slots on " + selectedDate[0] + ". Try another date.");
                    empty.setFont(new Font("Times New Roman", Font.ITALIC, 12));
                    empty.setForeground(new Color(192, 57, 43));
                    slotButtonPanel.add(empty);
                } else {
                    for (String slot : slots) {
                        JButton slotBtn = new JButton(slot);
                        slotBtn.setFont(new Font("Times New Roman", Font.BOLD, 13));
                        slotBtn.setBackground(new Color(200, 255, 200));
                        slotBtn.setForeground(new Color(39, 174, 96));
                        slotBtn.setFocusPainted(false);
                        slotBtn.setBorder(BorderFactory.createLineBorder(new Color(39, 174, 96)));
                        slotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        slotBtn.setPreferredSize(new Dimension(85, 38));

                        slotBtn.addActionListener(ev -> {
                            // Reset all
                            for (java.awt.Component c : slotButtonPanel.getComponents()) {
                                if (c instanceof JButton) {
                                    c.setBackground(new Color(200, 255, 200));
                                    ((JButton) c).setForeground(new Color(39, 174, 96));
                                }
                            }
                            // Highlight selected
                            slotBtn.setBackground(new Color(39, 174, 96));
                            slotBtn.setForeground(Color.WHITE);
                            selectedTime[0] = slot;
                            selectedSlotLabel.setText(
                                "Selected:  " + slot + "  on  " + selectedDate[0]);
                            selectedSlotLabel.setForeground(new Color(39, 174, 96));
                            selectedSlotLabel.setFont(
                                new Font("Times New Roman", Font.BOLD, 12));
                            confirmBtn.setEnabled(true);
                            confirmBtn.setBackground(new Color(39, 174, 96));  // green when active
                            confirmBtn.setForeground(Color.WHITE);
                        });
                        slotButtonPanel.add(slotBtn);
                    }
                }
                slotButtonPanel.revalidate();
                slotButtonPanel.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        // ── CONFIRM ACTION ──
        confirmBtn.addActionListener(e -> {
            int idx = doctorDropdown.getSelectedIndex();
            int doctorId = Integer.parseInt(doctors.get(idx)[0]);
            String doctorName = doctors.get(idx)[1];

            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Confirm your booking?\n\n"
                + "Doctor : " + doctorName + "\n"
                + "Date   : " + selectedDate[0] + "\n"
                + "Time   : " + selectedTime[0] + "\n\n"
                + "Status will be PENDING until doctor approves.",
                "Confirm Booking", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                boolean ok = patientService.bookAppointment(
                    loggedInUsername, doctorId, selectedDate[0], selectedTime[0]);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog,
                        "Appointment booked successfully!\n\n"
                        + "Doctor : " + doctorName + "\n"
                        + "Date   : " + selectedDate[0] + "\n"
                        + "Time   : " + selectedTime[0] + "\n"
                        + "Status : PENDING (awaiting doctor approval)",
                        "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Booking failed. Slot may no longer be available.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}
    private void doCancelAppointment() {
    try {
        List<String[]> allAppointments = patientService.viewAppointmentSchedules(loggedInUsername);

        // Filter ACCEPTED only
        List<String[]> cancelable = new ArrayList<>();
        boolean hasPending = false;
        for (String[] a : allAppointments) {
            if ("ACCEPTED".equalsIgnoreCase(a[4])) cancelable.add(a);
            else if ("PENDING".equalsIgnoreCase(a[4])) hasPending = true;
        }

        // ── MAIN DIALOG ──
        JDialog dialog = new JDialog(this, "Cancel Appointment", true);
        dialog.setSize(560, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(192, 57, 43));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel headerLabel = new JLabel("Cancel Appointment");
        headerLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        JLabel subLabel = new JLabel("Only confirmed (ACCEPTED) appointments can be cancelled");
        subLabel.setFont(new Font("Times New Roman", Font.ITALIC, 11));
        subLabel.setForeground(new Color(255, 200, 200));
        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setBackground(new Color(192, 57, 43));
        headerText.add(headerLabel);
        headerText.add(subLabel);
        header.add(headerText, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        // ── CONTENT AREA ──
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ── EMPTY / PENDING ONLY STATE ──
        if (cancelable.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridLayout(hasPending ? 3 : 2, 1, 0, 10));
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));

            JLabel iconLabel = new JLabel("No confirmed appointments to cancel.", SwingConstants.CENTER);
            iconLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
            iconLabel.setForeground(new Color(44, 62, 80));
            emptyPanel.add(iconLabel);

            if (hasPending) {
                JPanel pendingNote = new JPanel(new BorderLayout());
                pendingNote.setBackground(new Color(255, 248, 220));
                pendingNote.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 165, 0), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                JLabel pendingLabel = new JLabel(
                    "<html><b>Note:</b> You have PENDING appointments awaiting doctor approval.<br>"
                    + "These cannot be cancelled until the doctor accepts them.</html>");
                pendingLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12));
                pendingLabel.setForeground(new Color(120, 80, 0));
                pendingNote.add(pendingLabel, BorderLayout.CENTER);
                emptyPanel.add(pendingNote);
            }

            contentPanel.add(emptyPanel, BorderLayout.CENTER);

            JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            closePanel.setBackground(Color.WHITE);
            JButton closeBtn = makeDialogBtn("Close", new Color(52, 73, 94));
            closeBtn.addActionListener(e -> dialog.dispose());
            closePanel.add(closeBtn);
            contentPanel.add(closePanel, BorderLayout.SOUTH);
            dialog.add(contentPanel, BorderLayout.CENTER);
            dialog.setVisible(true);
            return;
        }

        // ── APPOINTMENT CARDS ──
        JPanel cardsWrapper = new JPanel();
        cardsWrapper.setLayout(new BoxLayout(cardsWrapper, BoxLayout.Y_AXIS));
        cardsWrapper.setBackground(Color.WHITE);

        for (String[] appt : cancelable) {
            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setBackground(Color.WHITE);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(192, 57, 43)),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
                )
            ));

            // Left — appointment info
            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 2, 2));
            infoPanel.setBackground(Color.WHITE);

            JLabel doctorLabel = new JLabel("Dr. " + appt[1]);
            doctorLabel.setFont(new Font("Times New Roman", Font.BOLD, 14));
            doctorLabel.setForeground(new Color(44, 62, 80));

            JPanel detailsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            detailsRow.setBackground(Color.WHITE);

            JLabel dateLabel = makeInfoChip("Date: " + appt[2], new Color(41, 128, 185));
            JLabel timeLabel = makeInfoChip("Time: " + appt[3], new Color(39, 174, 96));
            JLabel statusLabel = makeInfoChip("ACCEPTED", new Color(39, 174, 96));

            detailsRow.add(dateLabel);
            detailsRow.add(Box.createHorizontalStrut(8));
            detailsRow.add(timeLabel);
            detailsRow.add(Box.createHorizontalStrut(8));
            detailsRow.add(statusLabel);

            infoPanel.add(doctorLabel);
            infoPanel.add(detailsRow);
            card.add(infoPanel, BorderLayout.CENTER);

            // Right — cancel button
            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.setFont(new Font("Times New Roman", Font.BOLD, 12));
            cancelBtn.setBackground(new Color(192, 57, 43));
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setBorderPainted(false);
            cancelBtn.setOpaque(true);
            cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cancelBtn.setPreferredSize(new Dimension(90, 40));

            // Hover effect
            cancelBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    cancelBtn.setBackground(new Color(150, 40, 30));
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    cancelBtn.setBackground(new Color(192, 57, 43));
                }
            });

            cancelBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                    "<html>Are you sure you want to cancel?<br><br>"
                    + "<b>Doctor:</b> " + appt[1] + "<br>"
                    + "<b>Date:</b>   " + appt[2] + "<br>"
                    + "<b>Time:</b>   " + appt[3] + "</html>",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;

                try {
                    int apptId = Integer.parseInt(appt[0]);
                    boolean ok = patientService.cancelAppointment(apptId);
                    if (ok) {
                        JOptionPane.showMessageDialog(dialog,
                            "Appointment cancelled successfully!",
                            "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                            "Failed to cancel. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            card.add(cancelBtn, BorderLayout.EAST);

            cardsWrapper.add(card);
            cardsWrapper.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(cardsWrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scroll, BorderLayout.CENTER);

        // ── FOOTER ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        JButton closeBtn = makeDialogBtn("Close", new Color(52, 73, 94));
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

// ── ADD THESE HELPER METHODS ──
private JLabel makeInfoChip(String text, Color color) {
    JLabel chip = new JLabel("  " + text + "  ");
    chip.setFont(new Font("Times New Roman", Font.BOLD, 11));
    chip.setForeground(color);
    chip.setBackground(new Color(
        Math.min(color.getRed() + 180, 255),
        Math.min(color.getGreen() + 180, 255),
        Math.min(color.getBlue() + 180, 255)));
    chip.setOpaque(true);
    chip.setBorder(BorderFactory.createLineBorder(color, 1));
    return chip;
}

    private JButton makeDialogBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Times New Roman", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 35));
        return btn;
}


    
    private void doViewSchedule() {
    try {
        List<String[]> list = patientService.viewAppointmentSchedules(loggedInUsername);

        // ── OUTER DIALOG PANEL ──
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ── TOP SUMMARY BAR ──
        int pendingCount = 0, acceptedCount = 0;
        for (String[] a : list) {
            if ("PENDING".equalsIgnoreCase(a[4])) pendingCount++;
            else if ("ACCEPTED".equalsIgnoreCase(a[4])) acceptedCount++;
        }

        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        summaryBar.setBackground(new Color(240, 248, 255));
        summaryBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(174, 214, 241), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel totalLabel = new JLabel("Total: " + list.size() + " appointment(s)");
        totalLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));
        totalLabel.setForeground(new Color(31, 97, 141));

        JLabel pendingBadge = makeBadge("● PENDING: " + pendingCount, new Color(255, 165, 0));
        JLabel acceptedBadge = makeBadge("● ACCEPTED: " + acceptedCount, new Color(39, 174, 96));

        summaryBar.add(totalLabel);
        summaryBar.add(Box.createHorizontalStrut(10));
        summaryBar.add(pendingBadge);
        summaryBar.add(acceptedBadge);
        mainPanel.add(summaryBar, BorderLayout.NORTH);

        // ── EMPTY STATE ──
        if (list.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setPreferredSize(new Dimension(600, 150));
            JLabel emptyLabel = new JLabel("No upcoming appointments found.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Times New Roman", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            mainPanel.add(emptyPanel, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this, mainPanel,
                "My Appointment Schedule", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        // ── STYLED TABLE ──
        String[] columns = {"  #", "  Doctor", "  Date", "  Time", "  Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        int rowNum = 1;
        for (String[] a : list) {
            model.addRow(new Object[]{
                "  " + rowNum++,
                "  " + a[1],
                "  " + a[2],
                "  " + a[3],
                "  " + a[4]
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(214, 234, 248));

        // Header style
        table.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(41, 128, 185));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);

        // Row color coding
        table.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)));

                String status = t.getValueAt(row, 4).toString().trim();

                if (isSelected) {
                    setBackground(new Color(214, 234, 248));
                    setForeground(new Color(31, 97, 141));
                } else if (row % 2 == 0) {
                    setBackground(Color.WHITE);
                } else {
                    setBackground(new Color(249, 249, 249));
                }

                // Status column special coloring
                if (col == 4) {
                    setFont(new Font("Times New Roman", Font.BOLD, 12));
                    if (status.equalsIgnoreCase("PENDING")) {
                        setForeground(new Color(180, 100, 0));
                        setBackground(new Color(255, 248, 225));
                    } else if (status.equalsIgnoreCase("ACCEPTED")) {
                        setForeground(new Color(39, 174, 96));
                        setBackground(new Color(232, 255, 240));
                    }
                } else {
                    setFont(new Font("Times New Roman", Font.PLAIN, 13));
                    setForeground(new Color(44, 62, 80));
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(620, 200));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainPanel.add(scroll, BorderLayout.CENTER);

        // ── LEGEND ──
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        legend.setBackground(Color.WHITE);
        legend.add(new JLabel("Legend:") {{
            setFont(new Font("Times New Roman", Font.BOLD, 11));
            setForeground(Color.GRAY);
        }});
        legend.add(makeBadge("PENDING = Awaiting doctor approval", new Color(180, 100, 0)));
        legend.add(makeBadge("ACCEPTED = Confirmed", new Color(39, 174, 96)));
        mainPanel.add(legend, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, mainPanel,
            "My Appointment Schedule", JOptionPane.PLAIN_MESSAGE);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

// Helper — add this after doViewSchedule()
private JLabel makeBadge(String text, Color color) {
    JLabel badge = new JLabel(text);
    badge.setFont(new Font("Times New Roman", Font.BOLD, 11));
    badge.setForeground(color);
    return badge;
}

    
    private void doViewHistory() {
    try {
        List<String[]> list = patientService.viewAppointmentHistory(loggedInUsername);

        // ── OUTER PANEL ──
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ── SUMMARY BAR ──
        int completedCount = 0, cancelledCount = 0, rejectedCount = 0;
        for (String[] a : list) {
            switch (a[4].toUpperCase()) {
                case "COMPLETED": completedCount++; break;
                case "CANCELLED": cancelledCount++; break;
                case "REJECTED":  rejectedCount++;  break;
            }
        }

        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        summaryBar.setBackground(new Color(245, 245, 245));
        summaryBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel totalLabel = new JLabel("Total Records: " + list.size());
        totalLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));
        totalLabel.setForeground(new Color(44, 62, 80));

        summaryBar.add(totalLabel);
        summaryBar.add(Box.createHorizontalStrut(10));
        summaryBar.add(makeBadge("● COMPLETED: " + completedCount, new Color(52, 152, 219)));
        summaryBar.add(makeBadge("● CANCELLED: " + cancelledCount, new Color(150, 150, 150)));
        summaryBar.add(makeBadge("● REJECTED: "  + rejectedCount,  new Color(192, 57, 43)));
        mainPanel.add(summaryBar, BorderLayout.NORTH);

        // ── EMPTY STATE ──
        if (list.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setPreferredSize(new Dimension(600, 150));
            JLabel emptyLabel = new JLabel("No appointment history found.", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Times New Roman", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            mainPanel.add(emptyPanel, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this, mainPanel,
                "Appointment History", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        // ── TABLE ──
        String[] columns = {"  #", "  Doctor", "  Date", "  Time", "  Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        int rowNum = 1;
        for (String[] a : list) {
            model.addRow(new Object[]{
                "  " + rowNum++,
                "  " + a[1],
                "  " + a[2],
                "  " + a[3],
                "  " + a[4]
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(214, 234, 248));
        table.setEnabled(false);

        // Header
        table.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);

        // Row color coding
        table.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createMatteBorder(
                    0, 0, 1, 0, new Color(235, 235, 235)));

                String status = t.getValueAt(row, 4).toString().trim();

                // Alternating rows
                if (row % 2 == 0) setBackground(Color.WHITE);
                else setBackground(new Color(249, 249, 249));
                setForeground(new Color(44, 62, 80));

                // Status column coloring
                if (col == 4) {
                    setFont(new Font("Times New Roman", Font.BOLD, 12));
                    switch (status.toUpperCase()) {
                        case "COMPLETED":
                            setForeground(new Color(41, 128, 185));
                            setBackground(new Color(235, 245, 255));
                            break;
                        case "CANCELLED":
                            setForeground(new Color(100, 100, 100));
                            setBackground(new Color(245, 245, 245));
                            break;
                        case "REJECTED":
                            setForeground(new Color(192, 57, 43));
                            setBackground(new Color(255, 240, 240));
                            break;
                        default:
                            setForeground(new Color(44, 62, 80));
                    }
                } else {
                    setFont(new Font("Times New Roman", Font.PLAIN, 13));
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(620, 220));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        mainPanel.add(scroll, BorderLayout.CENTER);

        // ── LEGEND ──
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        legend.setBackground(Color.WHITE);
        legend.setBorder(BorderFactory.createMatteBorder(
            1, 0, 0, 0, new Color(220, 220, 220)));
        legend.add(new JLabel("Legend:") {{
            setFont(new Font("Times New Roman", Font.BOLD, 11));
            setForeground(Color.GRAY);
        }});
        legend.add(makeBadge("COMPLETED = Consultation done",   new Color(52, 152, 219)));
        legend.add(makeBadge("CANCELLED = Cancelled by patient", new Color(100, 100, 100)));
        legend.add(makeBadge("REJECTED = Declined by doctor",   new Color(192, 57, 43)));
        mainPanel.add(legend, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, mainPanel,
            "Appointment History", JOptionPane.PLAIN_MESSAGE);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

    private void doCheckAvailability() {
    try {
        List<String[]> doctors = patientService.getDoctors();
        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No doctors available.");
            return;
        }

        // ── STEP 1: SELECT DOCTOR ──
        String[] doctorOptions = new String[doctors.size()];
        for (int i = 0; i < doctors.size(); i++) {
            String[] d = doctors.get(i);
            doctorOptions[i] = d[1] + " — " + d[2];
        }
        JComboBox<String> doctorDropdown = new JComboBox<>(doctorOptions);
        doctorDropdown.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        JPanel selectPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        selectPanel.add(new JLabel("Select Doctor:") {{
            setFont(new Font("Times New Roman", Font.BOLD, 13));
        }});
        selectPanel.add(doctorDropdown);

        int step1 = JOptionPane.showConfirmDialog(this, selectPanel,
            "Check Doctor Availability", JOptionPane.OK_CANCEL_OPTION);
        if (step1 != JOptionPane.OK_OPTION) return;

        int selectedIndex = doctorDropdown.getSelectedIndex();
        int doctorId      = Integer.parseInt(doctors.get(selectedIndex)[0]);
        String doctorName = doctors.get(selectedIndex)[1];
        String doctorSpec = doctors.get(selectedIndex)[2];

        // ── MAIN DIALOG ──
        JDialog dialog = new JDialog(this, "Doctor Availability", true);
        dialog.setSize(720, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel headerLeft = new JPanel(new GridLayout(2, 1));
        headerLeft.setBackground(new Color(41, 128, 185));
        JLabel nameLabel = new JLabel(doctorName);
        nameLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        JLabel specLabel = new JLabel(doctorSpec);
        specLabel.setFont(new Font("Times New Roman", Font.ITALIC, 13));
        specLabel.setForeground(new Color(200, 230, 255));
        headerLeft.add(nameLabel);
        headerLeft.add(specLabel);
        header.add(headerLeft, BorderLayout.WEST);

        // Week navigation
        JPanel weekNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        weekNav.setBackground(new Color(41, 128, 185));
        JButton prevBtn = makeNavBtn("< Prev Week");
        JButton nextBtn = makeNavBtn("Next Week >");
        JLabel weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));
        weekLabel.setForeground(Color.WHITE);
        weekNav.add(prevBtn);
        weekNav.add(weekLabel);
        weekNav.add(nextBtn);
        header.add(weekNav, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);

        // ── LEGEND BAR ──
        JPanel legendBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        legendBar.setBackground(new Color(245, 247, 250));
        legendBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        legendBar.add(new JLabel("Click any available slot to book instantly") {{
            setFont(new Font("Times New Roman", Font.ITALIC, 12));
            setForeground(new Color(100, 100, 100));
        }});
        legendBar.add(Box.createHorizontalStrut(20));
        legendBar.add(makeLegendChip("Available", new Color(39, 174, 96), new Color(220, 255, 220)));
        legendBar.add(makeLegendChip("Booked / Unavailable", new Color(150, 150, 150), new Color(240, 240, 240)));
        dialog.add(legendBar, BorderLayout.CENTER);

        // ── DAY CARDS PANEL ──
        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setBackground(Color.WHITE);

        String[] timeSlots = {"09:00", "10:00", "11:00", "13:00", "14:00"};
        String[] dayNames  = {"MON", "TUE", "WED", "THU", "FRI"};
        java.text.SimpleDateFormat sdf     = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.text.SimpleDateFormat dispSdf = new java.text.SimpleDateFormat("dd MMM");

        final java.util.Calendar[] weekStart = {java.util.Calendar.getInstance()};
        weekStart[0].set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);

        // Inner class to build calendar
        Runnable buildCalendar = new Runnable() {
            @Override
            public void run() {
                calendarPanel.removeAll();

                // Dates for this week
                String[] dates   = new String[5];
                String[] dispDates = new String[5];
                java.util.Calendar temp = (java.util.Calendar) weekStart[0].clone();
                for (int d = 0; d < 5; d++) {
                    dates[d]    = sdf.format(temp.getTime());
                    dispDates[d] = dispSdf.format(temp.getTime());
                    temp.add(java.util.Calendar.DAY_OF_MONTH, 1);
                }

                // Update week label
                weekLabel.setText(dispDates[0] + " — " + dispDates[4]);

                // Day columns
                JPanel daysRow = new JPanel(new GridLayout(1, 5, 8, 0));
                daysRow.setBackground(Color.WHITE);
                daysRow.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

                for (int d = 0; d < 5; d++) {
                    final String date = dates[d];
                    JPanel dayCard = new JPanel(new BorderLayout(0, 6));
                    dayCard.setBackground(Color.WHITE);
                    dayCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(0, 0, 8, 0)
                    ));

                    // Day header
                    JPanel dayHeader = new JPanel(new GridLayout(2, 1));
                    dayHeader.setBackground(new Color(52, 73, 94));
                    dayHeader.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
                    JLabel dayNameLbl = new JLabel(dayNames[d], SwingConstants.CENTER);
                    dayNameLbl.setFont(new Font("Times New Roman", Font.BOLD, 13));
                    dayNameLbl.setForeground(Color.WHITE);
                    JLabel dateLbl = new JLabel(dispDates[d], SwingConstants.CENTER);
                    dateLbl.setFont(new Font("Times New Roman", Font.PLAIN, 11));
                    dateLbl.setForeground(new Color(180, 200, 220));
                    dayHeader.add(dayNameLbl);
                    dayHeader.add(dateLbl);
                    dayCard.add(dayHeader, BorderLayout.NORTH);

                    // Slots
                    JPanel slotCol = new JPanel(new GridLayout(timeSlots.length, 1, 0, 5));
                    slotCol.setBackground(Color.WHITE);
                    slotCol.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

                    try {
                        List<String> available = patientService.checkDoctorAvailability(doctorId, date);
                        for (String slot : timeSlots) {
                            if (available.contains(slot)) {
                                // Available — clickable green chip
                                JButton slotChip = new JButton(slot);
                                slotChip.setFont(new Font("Times New Roman", Font.BOLD, 12));
                                slotChip.setBackground(new Color(39, 174, 96));
                                slotChip.setForeground(Color.WHITE);
                                slotChip.setFocusPainted(false);
                                slotChip.setBorderPainted(false);
                                slotChip.setOpaque(true);
                                slotChip.setCursor(new Cursor(Cursor.HAND_CURSOR));

                                // Hover effect
                                slotChip.addMouseListener(new java.awt.event.MouseAdapter() {
                                    @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                                        slotChip.setBackground(new Color(30, 140, 75));
                                    }
                                    @Override public void mouseExited(java.awt.event.MouseEvent e) {
                                        slotChip.setBackground(new Color(39, 174, 96));
                                    }
                                });

                                // Click → straight to booking!
                                slotChip.addActionListener(ev -> {
                                    int confirm = JOptionPane.showConfirmDialog(dialog,
                                        "Book this appointment?\n\n"
                                        + "Doctor : " + doctorName + "\n"
                                        + "Date   : " + date + "\n"
                                        + "Time   : " + slot + "\n\n"
                                        + "Status will be PENDING until doctor approves.",
                                        "Confirm Booking",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE);
                                    if (confirm != JOptionPane.YES_OPTION) return;
                                    try {
                                        boolean ok = patientService.bookAppointment(
                                            loggedInUsername, doctorId, date, slot);
                                        if (ok) {
                                            JOptionPane.showMessageDialog(dialog,
                                                "Appointment booked!\n\n"
                                                + "Doctor : " + doctorName + "\n"
                                                + "Date   : " + date + "\n"
                                                + "Time   : " + slot + "\n"
                                                + "Status : PENDING",
                                                "Success", JOptionPane.INFORMATION_MESSAGE);
                                            dialog.dispose();
                                        } else {
                                            JOptionPane.showMessageDialog(dialog,
                                                "Booking failed. Slot may be taken.",
                                                "Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                                    }
                                });
                                slotCol.add(slotChip);
                            } else {
                                // Unavailable — grey label
                                JLabel greySlot = new JLabel(slot, SwingConstants.CENTER);
                                greySlot.setFont(new Font("Times New Roman", Font.PLAIN, 12));
                                greySlot.setForeground(new Color(180, 180, 180));
                                greySlot.setOpaque(true);
                                greySlot.setBackground(new Color(245, 245, 245));
                                greySlot.setBorder(BorderFactory.createLineBorder(
                                    new Color(220, 220, 220)));
                                slotCol.add(greySlot);
                            }
                        }
                    } catch (Exception ex) {
                        slotCol.add(new JLabel("Error", SwingConstants.CENTER));
                    }

                    dayCard.add(slotCol, BorderLayout.CENTER);
                    daysRow.add(dayCard);
                }

                calendarPanel.add(daysRow, BorderLayout.CENTER);
                calendarPanel.revalidate();
                calendarPanel.repaint();
            }
        };

        // Week nav actions
        prevBtn.addActionListener(e -> {
            weekStart[0].add(java.util.Calendar.DAY_OF_MONTH, -7);
            buildCalendar.run();
        });
        nextBtn.addActionListener(e -> {
            weekStart[0].add(java.util.Calendar.DAY_OF_MONTH, 7);
            buildCalendar.run();
        });

        buildCalendar.run();

        // ── CLOSE BUTTON ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Times New Roman", Font.BOLD, 12));
        closeBtn.setBackground(new Color(52, 73, 94));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(legendBar, BorderLayout.NORTH);
        southPanel.add(footer, BorderLayout.SOUTH);

        dialog.add(calendarPanel, BorderLayout.CENTER);
        dialog.add(southPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

// ── HELPER METHODS ──
private JButton makeNavBtn(String text) {
    JButton btn = new JButton(text);
    btn.setFont(new Font("Times New Roman", Font.BOLD, 11));
    btn.setBackground(new Color(31, 97, 141));   // ← solid dark blue instead of transparent
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);                  // ← remove border completely
    btn.setOpaque(true);                          // ← add this
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // ← padding only
    return btn;
}

private JLabel makeLegendChip(String text, Color fg, Color bg) {
    JLabel chip = new JLabel("  " + text + "  ");
    chip.setFont(new Font("Times New Roman", Font.BOLD, 11));
    chip.setForeground(fg);
    chip.setBackground(bg);
    chip.setOpaque(true);
    chip.setBorder(BorderFactory.createLineBorder(fg, 1));
    return chip;
}

    // Reusable table popup for appointments
    private void showTable(String title, List<String[]> data) {
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found.");
            return;
        }
        String[] columns = {"ID", "Doctor", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (String[] row : data) {
            model.addRow(row);
        }
        JTable table = new JTable(model);
        table.setEnabled(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(580, 250));
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.PLAIN_MESSAGE);
    }
    
    private void doViewProfile() {
    try {
        String[] profile = patientService.getPatientProfile(loggedInUsername);
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Profile not found.");
            return;
        }

        JDialog dialog = new JDialog(this, "My Profile", true);
        dialog.setSize(480, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setBackground(new Color(41, 128, 185));
        JLabel title = new JLabel("Patient Profile");
        title.setFont(new Font("Times New Roman", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Your personal account information");
        subtitle.setFont(new Font("Times New Roman", Font.ITALIC, 12));
        subtitle.setForeground(new Color(200, 230, 255));
        headerText.add(title);
        headerText.add(subtitle);
        header.add(headerText, BorderLayout.WEST);

        // Avatar circle
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillOval(5, 5, 50, 50);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Times New Roman", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String initial = profile[0].substring(0, 1).toUpperCase();
                int x = 5 + (50 - fm.stringWidth(initial)) / 2;
                int y = 5 + (50 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initial, x, y);
            }
        };
        avatarPanel.setBackground(new Color(41, 128, 185));
        avatarPanel.setPreferredSize(new Dimension(65, 65));
        header.add(avatarPanel, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);

        // ── PROFILE FIELDS ──
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        fieldsPanel.add(makeProfileRow("Username",   profile[0], "👤", new Color(41, 128, 185)));
        fieldsPanel.add(makeProfileRow("Contact No", profile[1] != null ? profile[1] : "Not set",
            "📞", new Color(39, 174, 96)));
        fieldsPanel.add(makeProfileRow("Address",    profile[2] != null ? profile[2] : "Not set",
            "📍", new Color(142, 68, 173)));
        fieldsPanel.add(makeProfileRow("Role",       profile[3], "🔖", new Color(230, 126, 34)));
        dialog.add(fieldsPanel, BorderLayout.CENTER);

        // ── FOOTER ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        JButton closeBtn = makeDialogBtn("Close", new Color(41, 128, 185));
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

    // Helper for profile rows
    private JPanel makeProfileRow(String label, String value, String icon, Color color) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(Color.WHITE);      
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
            BorderFactory.createEmptyBorder(15, 5, 15, 5)  // ← more vertical padding
        ));

        // Left accent bar
        JPanel accent = new JPanel();
        accent.setBackground(color);
        accent.setPreferredSize(new Dimension(4, 0));
        row.add(accent, BorderLayout.WEST);

        // Label + value
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel labelLbl = new JLabel(label.toUpperCase());
        labelLbl.setFont(new Font("Times New Roman", Font.PLAIN, 10));
        labelLbl.setForeground(new Color(150, 150, 150));

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Times New Roman", Font.BOLD, 14));
        valueLbl.setForeground(new Color(44, 62, 80));

        textPanel.add(labelLbl);
        textPanel.add(valueLbl);
        row.add(textPanel, BorderLayout.CENTER);

        return row;
    }
}