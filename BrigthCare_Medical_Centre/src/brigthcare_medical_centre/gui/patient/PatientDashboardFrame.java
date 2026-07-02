package brigthcare_medical_centre.gui.patient;

import brigthcare_medical_centre.common.PatientInterface;
import brigthcare_medical_centre.util.Constants;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

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
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("Patient Portal  |  " + loggedInUsername, SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setOpaque(true);
        header.setBackground(new Color(41, 128, 185));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(header, BorderLayout.NORTH);

        // Button panel
        JPanel btnPanel = new JPanel(new GridLayout(7, 1, 8, 8));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));

        JButton btnUpdate   = new JButton("Update Personal Information");
        JButton btnProfile  = new JButton("View My Profile");
        JButton btnBook     = new JButton("Book Appointment");
        JButton btnCancel   = new JButton("Cancel Appointment");
        JButton btnSchedule = new JButton("View Appointment Schedule");
        JButton btnHistory  = new JButton("View Appointment History");
        JButton btnAvail    = new JButton("Check Doctor Availability");

        Font btnFont = new Font("Arial", Font.PLAIN, 14);
        for (JButton btn : new JButton[]{btnUpdate, btnProfile, btnBook, btnCancel, btnSchedule, btnHistory, btnAvail}) {
            btn.setFont(btnFont);
            btn.setPreferredSize(new Dimension(200, 45));
            btnPanel.add(btn);
        }

        add(btnPanel, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("BrightCare Medical Centre  |  Distributed Systems", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.ITALIC, 11));
        footer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(footer, BorderLayout.SOUTH);

        // Button actions
        btnUpdate.addActionListener(e -> doUpdateInfo());
        btnBook.addActionListener(e -> doBookAppointment());
        btnCancel.addActionListener(e -> doCancelAppointment());
        btnSchedule.addActionListener(e -> doViewSchedule());
        btnHistory.addActionListener(e -> doViewHistory());
        btnAvail.addActionListener(e -> doCheckAvailability());
        btnProfile.addActionListener(e -> doViewProfile());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void doUpdateInfo() {
        JTextField contactField = new JTextField(15);
        JTextField addressField = new JTextField(15);
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("New Contact Number:"));
        panel.add(contactField);
        panel.add(new JLabel("New Address:"));
        panel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Update Personal Info", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                boolean ok = patientService.updatePersonalInfo(
                        loggedInUsername, contactField.getText(), addressField.getText());
                JOptionPane.showMessageDialog(this,
                        ok ? "Info updated successfully!" : "Update failed. Please try again.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void doBookAppointment() {
    try {
        // Load doctors from server
        List<String[]> doctors = patientService.getDoctors();
        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No doctors available.");
            return;
        }

        // Build doctor dropdown: "Dr. Ahmad - General Practitioner (ID: 1)"
        String[] doctorOptions = new String[doctors.size()];
        for (int i = 0; i < doctors.size(); i++) {
            String[] d = doctors.get(i);
            doctorOptions[i] = d[1] + " - " + d[2] + " (ID: " + d[0] + ")";
        }

        // Step 1 — Select doctor and date
        JComboBox<String> doctorDropdown = new JComboBox<>(doctorOptions);

        // Date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel(
        new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new java.util.Date());

        JPanel step1Panel = new JPanel(new GridLayout(2, 2, 5, 5));
        step1Panel.add(new JLabel("Select Doctor:"));
        step1Panel.add(doctorDropdown);
        step1Panel.add(new JLabel("Select Date:"));
        step1Panel.add(dateSpinner);

        int step1Result = JOptionPane.showConfirmDialog(this, step1Panel,
                "Book Appointment - Step 1: Select Doctor & Date",
                JOptionPane.OK_CANCEL_OPTION);
        if (step1Result != JOptionPane.OK_OPTION) return;

        // Get selected doctor ID
        int selectedIndex = doctorDropdown.getSelectedIndex();
        int doctorId = Integer.parseInt(doctors.get(selectedIndex)[0]);
        String doctorName = doctors.get(selectedIndex)[1];

        // Format selected date
        java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(selectedDate);

        // Step 2 — Fetch available slots
        List<String> slots = patientService.checkDoctorAvailability(doctorId, date);
        if (slots.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No available slots for " + doctorName + " on " + date + ".",
                    "No Slots Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show slot dropdown
        JComboBox<String> slotDropdown = new JComboBox<>(slots.toArray(new String[0]));
        JPanel step2Panel = new JPanel(new GridLayout(3, 2, 5, 5));
        step2Panel.add(new JLabel("Doctor:"));
        step2Panel.add(new JLabel(doctorName));
        step2Panel.add(new JLabel("Date:"));
        step2Panel.add(new JLabel(date));
        step2Panel.add(new JLabel("Select Time Slot:"));
        step2Panel.add(slotDropdown);

        int step2Result = JOptionPane.showConfirmDialog(this, step2Panel,
                "Book Appointment - Step 2: Select Time Slot",
                JOptionPane.OK_CANCEL_OPTION);
        if (step2Result != JOptionPane.OK_OPTION) return;

        String selectedTime = (String) slotDropdown.getSelectedItem();

        // Confirm and book
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm booking?\n\nDoctor: " + doctorName
                + "\nDate: " + date + "\nTime: " + selectedTime,
                "Confirm Appointment", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = patientService.bookAppointment(
                loggedInUsername, doctorId, date, selectedTime);

        JOptionPane.showMessageDialog(this,
                ok ? "✅ Appointment booked!\n\nDoctor: " + doctorName
                   + "\nDate: " + date + "\nTime: " + selectedTime
                   : "❌ Booking failed. Please try again.",
                ok ? "Success" : "Error",
                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void doCancelAppointment() {
        String input = JOptionPane.showInputDialog(this, "Enter Appointment ID to cancel:");
        if (input != null && !input.isEmpty()) {
            try {
                boolean ok = patientService.cancelAppointment(Integer.parseInt(input.trim()));
                JOptionPane.showMessageDialog(this,
                        ok ? "Appointment cancelled successfully!" : "Could not cancel. Please check the ID.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void doViewSchedule() {
        try {
            List<String[]> list = patientService.viewAppointmentSchedules(loggedInUsername);
            showTable("Upcoming Appointments", list);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void doViewHistory() {
        try {
            List<String[]> list = patientService.viewAppointmentHistory(loggedInUsername);
            showTable("Appointment History", list);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void doCheckAvailability() {
        try {
        // Load doctors dropdown
        List<String[]> doctors = patientService.getDoctors();
        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No doctors available.");
            return;
        }
        String[] doctorOptions = new String[doctors.size()];
        for (int i = 0; i < doctors.size(); i++) {
            String[] d = doctors.get(i);
            doctorOptions[i] = d[1] + " - " + d[2] + " (ID: " + d[0] + ")";
        }

        JComboBox<String> doctorDropdown = new JComboBox<>(doctorOptions);

        // Date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel(
        new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(new java.util.Date());

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Select Doctor:"));
        panel.add(doctorDropdown);
        panel.add(new JLabel("Select Date:"));
        panel.add(dateSpinner);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Check Doctor Availability", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        int selectedIndex = doctorDropdown.getSelectedIndex();
        int doctorId = Integer.parseInt(doctors.get(selectedIndex)[0]);
        String doctorName = doctors.get(selectedIndex)[1];

        java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(selectedDate);

        List<String> slots = patientService.checkDoctorAvailability(doctorId, date);
        if (slots.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No available slots for " + doctorName + " on " + date + ".");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Available slots for " + doctorName + " on " + date + ":\n"
                    + String.join("\n", slots),
                    "Doctor Availability", JOptionPane.INFORMATION_MESSAGE);
        }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }

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
            String info = "═══════════════════════════\n"
                        + "       PATIENT PROFILE\n"
                        + "═══════════════════════════\n"
                        + "Username    : " + profile[0] + "\n"
                        + "Contact No  : " + (profile[1] != null ? profile[1] : "Not set") + "\n"
                        + "Address     : " + (profile[2] != null ? profile[2] : "Not set") + "\n"
                        + "Role        : " + profile[3] + "\n"
                        + "═══════════════════════════";
            JOptionPane.showMessageDialog(this, info,
                    "My Profile", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}