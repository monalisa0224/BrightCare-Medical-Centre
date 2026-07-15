package brigthcare_medical_centre.gui.doctor;

import brigthcare_medical_centre.common.DoctorInterface;
import brigthcare_medical_centre.util.Constants;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DoctorDashboardFrame extends JFrame {

    private DoctorInterface doctorService;
    private int doctorId;
    private String doctorName;
    private String username;
    private JTabbedPane tabbedPane;

    private static final Color TEAL = new Color(0, 102, 102);
    private static final Color LIGHT_TEAL = new Color(224, 242, 241);
    private static final String[] DEFAULT_SLOTS = {"09:00", "10:00", "11:00", "13:00", "14:00"};

    public DoctorDashboardFrame(int doctorId, String doctorName, String username) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.username = username;
        connectToServer();
        buildUI();
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(Constants.RMI_HOST, Constants.RMI_PORT);
            doctorService = (DoctorInterface) registry.lookup(Constants.DOCTOR_SERVICE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to server:\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildUI() {
        setTitle("BrightCare Medical Centre - Doctor Portal");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 550));

        add(createTopBar(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));
        tabbedPane.addTab("Overview", createOverviewPanel());
        tabbedPane.addTab("Pending Appointments", createPendingAppointmentsPanel());
        tabbedPane.addTab("Manage Appointments", createAppointmentManagementPanel());
        tabbedPane.addTab("Consultation Notes", createConsultationNotesPanel());
        tabbedPane.addTab("My Schedule", createSchedulePanel());
        tabbedPane.addTab("Patient History", createPatientHistoryPanel());
        tabbedPane.addTab("Settings", createSettingsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        JLabel footer = new JLabel("BrightCare Medical Centre  |  Distributed Systems", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.ITALIC, 11));
        footer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(footer, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(TEAL);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel nameLabel = new JLabel("Dr. " + doctorName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        topBar.add(nameLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(TEAL);

        JButton profileBtn = new JButton("Profile");
        profileBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        profileBtn.addActionListener(e -> showProfileDialog());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 12));
        logoutBtn.setBackground(new Color(192, 57, 43));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> doLogout());

        rightPanel.add(profileBtn);
        rightPanel.add(logoutBtn);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new DoctorLoginFrame());
        }
    }

    private void showProfileDialog() {
        try {
            String[] profile = doctorService.getDoctorProfile(doctorId);
            if (profile == null) {
                JOptionPane.showMessageDialog(this, "Profile not found.");
                return;
            }
            String info = "=== DOCTOR PROFILE ===\n\n"
                        + "Doctor ID  : " + profile[0] + "\n"
                        + "Name       : " + profile[1] + "\n"
                        + "Specialization : " + (profile[2] != null ? profile[2] : "N/A") + "\n"
                        + "Contact    : " + (profile[3] != null ? profile[3] : "N/A") + "\n"
                        + "Username   : " + profile[4];
            JOptionPane.showMessageDialog(this, info, "My Profile", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            int[] summary = doctorService.getTodaySummary(doctorId);
            String today = new SimpleDateFormat("EEEE, dd MMMM yyyy").format(new Date());

            JPanel summaryPanel = new JPanel(new GridLayout(1, 5, 10, 0));
            summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Today's Summary - " + today),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            summaryPanel.add(createStatCard("Pending", summary[0], new Color(255, 193, 7)));
            summaryPanel.add(createStatCard("Confirmed", summary[1], new Color(46, 204, 113)));
            summaryPanel.add(createStatCard("Completed", summary[2], new Color(52, 152, 219)));
            summaryPanel.add(createStatCard("Cancelled", summary[3], new Color(231, 76, 60)));
            summaryPanel.add(createStatCard("Total", summary[4], Color.DARK_GRAY));

            panel.add(summaryPanel, BorderLayout.NORTH);

            List<String[]> todayAppts = doctorService.getTodayAppointments(doctorId);
            String[] columns = {"Appt ID", "Patient", "Time", "Status"};
            JTable table = createColorCodedTable(todayAppts, columns, 3);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createTitledBorder("Today's Appointments"));
            panel.add(scroll, BorderLayout.CENTER);

            if (todayAppts.isEmpty()) {
                panel.add(new JLabel("No appointments scheduled for today.", SwingConstants.CENTER),
                        BorderLayout.SOUTH);
            }
        } catch (Exception e) {
            panel.add(new JLabel("Error loading overview: " + e.getMessage()), BorderLayout.CENTER);
        }
        return panel;
    }

    private JPanel createStatCard(String label, int value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel valLabel = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        valLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valLabel.setForeground(color);

        JLabel nameLabel = new JLabel(label, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        nameLabel.setForeground(Color.DARK_GRAY);

        card.add(valLabel, BorderLayout.CENTER);
        card.add(nameLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createPendingAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton acceptBtn = new JButton("Accept");
        acceptBtn.setBackground(new Color(46, 204, 113));
        acceptBtn.setForeground(Color.WHITE);
        acceptBtn.setFocusPainted(false);

        JButton rejectBtn = new JButton("Reject");
        rejectBtn.setBackground(new Color(231, 76, 60));
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.setFocusPainted(false);

        JButton refreshBtn = new JButton("Refresh");

        btnPanel.add(acceptBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.NORTH);

        JTable table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Pending Appointments"));
        panel.add(scroll, BorderLayout.CENTER);

        acceptBtn.addActionListener(e -> handleAccept(table));
        rejectBtn.addActionListener(e -> handleReject(table));
        refreshBtn.addActionListener(e -> refreshPendingTable(table));

        refreshPendingTable(table);
        return panel;
    }

    private void handleAccept(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }
        int apptId = Integer.parseInt(table.getValueAt(row, 0).toString());
        try {
            boolean ok = doctorService.acceptAppointment(apptId);
            JOptionPane.showMessageDialog(this,
                    ok ? "Appointment accepted successfully!" : "Failed to accept.");
            if (ok) {
                refreshPendingTable(table);
                refreshCurrentTab();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void handleReject(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Reject this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int apptId = Integer.parseInt(table.getValueAt(row, 0).toString());
        try {
            boolean ok = doctorService.rejectAppointment(apptId);
            JOptionPane.showMessageDialog(this,
                    ok ? "Appointment rejected." : "Failed to reject.");
            if (ok) {
                refreshPendingTable(table);
                refreshCurrentTab();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void refreshPendingTable(JTable table) {
        try {
            List<String[]> data = doctorService.getPendingAppointments(doctorId);
            String[] columns = {"Appt ID", "Patient", "Contact", "Date", "Time", "Status"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            for (String[] row : data) {
                model.addRow(row);
            }
            table.setModel(model);
            applyColorCoding(table, 5);
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createAppointmentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTable table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Accepted and Pending Appointments"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn = new JButton("Cancel Appointment");
        JButton rescheduleBtn = new JButton("Reschedule");

        refreshBtn.addActionListener(e -> refreshManagedAppointments(table));
        cancelBtn.addActionListener(e -> handleDoctorCancel(table));
        rescheduleBtn.addActionListener(e -> handleDoctorReschedule(table));

        buttonPanel.add(refreshBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(rescheduleBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        refreshManagedAppointments(table);
        return panel;
    }

    private void refreshManagedAppointments(JTable table) {
        try {
            List<String[]> appointments = doctorService.getDoctorAppointments(doctorId);
            String[] columns = {"Appt ID", "Patient", "Date", "Time", "Status"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            for (String[] row : appointments) {
                if ("PENDING".equals(row[4]) || "ACCEPTED".equals(row[4])) {
                    model.addRow(row);
                }
            }
            table.setModel(model);
            applyColorCoding(table, 4);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void handleDoctorCancel(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }

        int apptId = Integer.parseInt(table.getValueAt(row, 0).toString());
        String status = table.getValueAt(row, 4).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel the selected " + status.toLowerCase() + " appointment?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean ok = doctorService.cancelAppointmentByDoctor(apptId);
            JOptionPane.showMessageDialog(this,
                    ok ? "Appointment cancelled." : "Unable to cancel the selected appointment.");
            if (ok) {
                refreshManagedAppointments(table);
                refreshCurrentTab();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void handleDoctorReschedule(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }

        int apptId = Integer.parseInt(table.getValueAt(row, 0).toString());
        String currentDate = table.getValueAt(row, 2).toString();
        String currentTime = table.getValueAt(row, 3).toString();

        JTextField dateField = new JTextField(currentDate);
        JComboBox<String> timeCombo = new JComboBox<>(DEFAULT_SLOTS);
        timeCombo.setSelectedItem(currentTime);
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("New Date (yyyy-MM-dd):"));
        form.add(dateField);
        form.add(new JLabel("New Time:"));
        form.add(timeCombo);

        int confirm = JOptionPane.showConfirmDialog(this, form, "Reschedule Appointment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            boolean ok = doctorService.rescheduleAppointment(
                    apptId, dateField.getText().trim(), timeCombo.getSelectedItem().toString());
            JOptionPane.showMessageDialog(this,
                    ok ? "Appointment rescheduled." : "Unable to reschedule. The new slot may be unavailable.");
            if (ok) {
                refreshManagedAppointments(table);
                refreshCurrentTab();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private JPanel createConsultationNotesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton loadBtn = new JButton("Load Appointments");
        JButton viewNotesBtn = new JButton("View Existing Notes");
        selectPanel.add(loadBtn);
        selectPanel.add(viewNotesBtn);

        JComboBox<String> apptCombo = new JComboBox<>();
        apptCombo.setPreferredSize(new Dimension(400, 25));
        selectPanel.add(new JLabel("Select Appointment:"));
        selectPanel.add(apptCombo);

        topPanel.add(selectPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Consultation Notes"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        JTextArea diagnosisArea = new JTextArea(3, 40);
        JTextArea treatmentArea = new JTextArea(3, 40);
        JTextArea prescriptionArea = new JTextArea(3, 40);
        JTextArea notesArea = new JTextArea(3, 40);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weighty = 0;
        formPanel.add(new JLabel("Diagnosis:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(diagnosisArea), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Treatment:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(treatmentArea), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Prescription:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(prescriptionArea), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Additional Notes:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(notesArea), gbc);

        JButton saveBtn = new JButton("Save Consultation Notes");
        saveBtn.setBackground(TEAL);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weighty = 0;
        formPanel.add(saveBtn, gbc);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);

        loadBtn.addActionListener(e -> {
            try {
                List<String[]> appts = doctorService.getDoctorAppointments(doctorId);
                apptCombo.removeAllItems();
                for (String[] a : appts) {
                    if (a[4].equals("ACCEPTED") || a[4].equals("COMPLETED")) {
                        apptCombo.addItem(a[0] + " | " + a[1] + " | " + a[2] + " " + a[3] + " [" + a[4] + "]");
                    }
                }
                if (apptCombo.getItemCount() == 0) {
                    apptCombo.addItem("No accepted/completed appointments");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        viewNotesBtn.addActionListener(e -> {
            if (apptCombo.getItemCount() == 0 || apptCombo.getSelectedItem() == null) return;
            String sel = apptCombo.getSelectedItem().toString();
            int apptId = Integer.parseInt(sel.split(" \\|")[0]);
            try {
                String[] note = doctorService.getConsultationNotes(apptId);
                if (note != null) {
                    diagnosisArea.setText(note[4]);
                    treatmentArea.setText(note[5]);
                    prescriptionArea.setText(note[6]);
                    notesArea.setText(note[7]);
                } else {
                    JOptionPane.showMessageDialog(this, "No notes found for this appointment.");
                    diagnosisArea.setText("");
                    treatmentArea.setText("");
                    prescriptionArea.setText("");
                    notesArea.setText("");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        saveBtn.addActionListener(e -> {
            if (apptCombo.getItemCount() == 0 || apptCombo.getSelectedItem() == null) return;
            String sel = apptCombo.getSelectedItem().toString();
            int apptId = Integer.parseInt(sel.split(" \\|")[0]);
            String patientUser = sel.split(" \\|")[1];
            try {
                boolean ok = doctorService.updateConsultationNotes(
                        apptId, doctorId, patientUser,
                        diagnosisArea.getText(), treatmentArea.getText(),
                        prescriptionArea.getText(), notesArea.getText());
                JOptionPane.showMessageDialog(this,
                        ok ? "Notes saved and appointment marked as completed!" : "Failed to save notes.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton prevWeekBtn = new JButton("< Previous Week");
        JButton nextWeekBtn = new JButton("Next Week >");
        JButton refreshScheduleBtn = new JButton("Refresh");
        JLabel weekLabel = new JLabel("Current Week", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JComboBox<String> slotCombo = new JComboBox<>(DEFAULT_SLOTS);
        JButton addSlotBtn = new JButton("Add Slot");
        JButton removeSlotBtn = new JButton("Remove Slot");

        JPanel scheduleGrid = new JPanel(new GridLayout(6, 6, 3, 3));
        scheduleGrid.setBorder(BorderFactory.createTitledBorder("Weekly Timetable"));

        controlPanel.add(prevWeekBtn);
        controlPanel.add(refreshScheduleBtn);
        controlPanel.add(nextWeekBtn);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(new JLabel("Slot:"));
        controlPanel.add(slotCombo);
        controlPanel.add(addSlotBtn);
        controlPanel.add(removeSlotBtn);

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(weekLabel, BorderLayout.CENTER);
        panel.add(scheduleGrid, BorderLayout.SOUTH);

        final Date[] currentWeekStart = {getWeekStart(new Date())};

        Runnable refreshSchedule = () -> {
            try {
                String startDate = new SimpleDateFormat("yyyy-MM-dd").format(currentWeekStart[0]);
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentWeekStart[0]);
                cal.add(Calendar.DAY_OF_MONTH, 4);
                String endDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

                List<String[]> slots = doctorService.getDoctorTimetable(doctorId, startDate);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat displaySdf = new SimpleDateFormat("EEE dd/MM");
                Calendar c = Calendar.getInstance();
                c.setTime(currentWeekStart[0]);

                weekLabel.setText("Week: " + displaySdf.format(c.getTime()) + " - "
                        + displaySdf.format(cal.getTime()));

                scheduleGrid.removeAll();
                scheduleGrid.setLayout(new GridLayout(6, 6, 3, 3));

                scheduleGrid.add(new JLabel("", SwingConstants.CENTER));
                String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
                for (String day : days) {
                    JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
                    dayLabel.setFont(new Font("Arial", Font.BOLD, 11));
                    scheduleGrid.add(dayLabel);
                }

                for (String slot : DEFAULT_SLOTS) {
                    scheduleGrid.add(new JLabel(slot, SwingConstants.CENTER));
                    Calendar tempCal = Calendar.getInstance();
                    tempCal.setTime(currentWeekStart[0]);
                    for (int d = 0; d < 5; d++) {
                        String dateStr = sdf.format(tempCal.getTime());
                        boolean found = false;
                        for (String[] s : slots) {
                            if (s[0].equals(dateStr) && s[1].equals(slot)) {
                                found = true;
                                boolean avail = Boolean.parseBoolean(s[2]);
                                JLabel cell = new JLabel(avail ? "Available" : "Unavailable", SwingConstants.CENTER);
                                cell.setOpaque(true);
                                cell.setBackground(avail ? new Color(200, 255, 200) : new Color(255, 200, 200));
                                cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                                scheduleGrid.add(cell);
                                break;
                            }
                        }
                        if (!found) {
                            JLabel cell = new JLabel("--", SwingConstants.CENTER);
                            cell.setOpaque(true);
                            cell.setBackground(new Color(240, 240, 240));
                            cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                            scheduleGrid.add(cell);
                        }
                        tempCal.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }

                panel.revalidate();
                panel.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        prevWeekBtn.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentWeekStart[0]);
            cal.add(Calendar.DAY_OF_MONTH, -7);
            currentWeekStart[0] = cal.getTime();
            refreshSchedule.run();
        });

        nextWeekBtn.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentWeekStart[0]);
            cal.add(Calendar.DAY_OF_MONTH, 7);
            currentWeekStart[0] = cal.getTime();
            refreshSchedule.run();
        });

        refreshScheduleBtn.addActionListener(e -> refreshSchedule.run());

        addSlotBtn.addActionListener(e -> {
            String slot = (String) slotCombo.getSelectedItem();
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentWeekStart[0]);
                int updated = 0;
                StringBuilder blockedDates = new StringBuilder();
                for (int d = 0; d < 5; d++) {
                    String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                    if (doctorService.updateDoctorSchedule(doctorId, dateStr, slot, true)) {
                        updated++;
                    } else {
                        if (blockedDates.length() > 0) {
                            blockedDates.append(", ");
                        }
                        blockedDates.append(dateStr);
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                if (blockedDates.length() == 0) {
                    JOptionPane.showMessageDialog(this, "Slot added for the week.");
                } else if (updated > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Slot updated for part of the week.\nBlocked dates: " + blockedDates);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No slots were updated.\nBlocked dates: " + blockedDates);
                }
                refreshSchedule.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        removeSlotBtn.addActionListener(e -> {
            String slot = (String) slotCombo.getSelectedItem();
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentWeekStart[0]);
                int updated = 0;
                StringBuilder blockedDates = new StringBuilder();
                for (int d = 0; d < 5; d++) {
                    String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                    if (doctorService.updateDoctorSchedule(doctorId, dateStr, slot, false)) {
                        updated++;
                    } else {
                        if (blockedDates.length() > 0) {
                            blockedDates.append(", ");
                        }
                        blockedDates.append(dateStr);
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                if (blockedDates.length() == 0) {
                    JOptionPane.showMessageDialog(this, "Slot removed for the week.");
                } else if (updated > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Slots updated for part of the week.\nBooked or locked dates: " + blockedDates);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No slots were updated.\nBooked or locked dates: " + blockedDates);
                }
                refreshSchedule.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        refreshSchedule.run();
        return panel;
    }

    private JPanel createPatientHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JTextField patientField = new JTextField(20);
        JButton searchBtn = new JButton("Search History");
        searchPanel.add(new JLabel("Patient Username:"));
        searchPanel.add(patientField);
        searchPanel.add(searchBtn);

        panel.add(searchPanel, BorderLayout.NORTH);

        JTable table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Patient Medical History"));
        panel.add(scroll, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> {
            String patientUser = patientField.getText().trim();
            if (patientUser.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a patient username.");
                return;
            }
            try {
                List<String[]> history = doctorService.getPatientHistory(patientUser);
                String[] columns = {"Appt ID", "Date", "Time", "Status", "Diagnosis", "Treatment", "Prescription", "Notes"};
                DefaultTableModel model = new DefaultTableModel(columns, 0);
                for (String[] row : history) {
                    model.addRow(row);
                }
                table.setModel(model);
                applyColorCoding(table, 3);
                if (history.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No history found for this patient.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel passwordPanel = new JPanel(new GridBagLayout());
        passwordPanel.setBorder(BorderFactory.createTitledBorder("Change Password"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JPasswordField currentPw = new JPasswordField(15);
        JPasswordField newPw = new JPasswordField(15);
        JPasswordField confirmPw = new JPasswordField(15);
        JButton changePwBtn = new JButton("Change Password");
        changePwBtn.setBackground(TEAL);
        changePwBtn.setForeground(Color.WHITE);
        changePwBtn.setFocusPainted(false);

        gbc.gridx = 0; gbc.gridy = 0;
        passwordPanel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        passwordPanel.add(currentPw, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        passwordPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        passwordPanel.add(newPw, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        passwordPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        passwordPanel.add(confirmPw, gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        passwordPanel.add(changePwBtn, gbc);

        changePwBtn.addActionListener(e -> {
            String current = new String(currentPw.getPassword());
            String newPass = new String(newPw.getPassword());
            String confirm = new String(confirmPw.getPassword());
            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.");
                return;
            }
            if (newPass.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.");
                return;
            }
            try {
                boolean ok = doctorService.changePassword(doctorId, current, newPass);
                JOptionPane.showMessageDialog(this,
                        ok ? "Password changed successfully!" : "Current password is incorrect.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        JPanel contactPanel = new JPanel(new GridBagLayout());
        contactPanel.setBorder(BorderFactory.createTitledBorder("Update Contact Info"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.insets = new Insets(5, 5, 5, 5);

        try {
            String[] profile = doctorService.getDoctorProfile(doctorId);
            String currentContact = (profile != null && profile[3] != null) ? profile[3] : "";

            JTextField contactField = new JTextField(currentContact, 15);
            JTextField nameField = new JTextField(profile != null ? profile[1] : "", 15);
            JTextField specField = new JTextField(profile != null ? profile[2] : "", 15);

            JButton updateBtn = new JButton("Update Profile");
            updateBtn.setBackground(TEAL);
            updateBtn.setForeground(Color.WHITE);
            updateBtn.setFocusPainted(false);

            gbc2.gridx = 0; gbc2.gridy = 0;
            contactPanel.add(new JLabel("Doctor Name:"), gbc2);
            gbc2.gridx = 1;
            contactPanel.add(nameField, gbc2);
            gbc2.gridx = 0; gbc2.gridy = 1;
            contactPanel.add(new JLabel("Specialization:"), gbc2);
            gbc2.gridx = 1;
            contactPanel.add(specField, gbc2);
            gbc2.gridx = 0; gbc2.gridy = 2;
            contactPanel.add(new JLabel("Contact Number:"), gbc2);
            gbc2.gridx = 1;
            contactPanel.add(contactField, gbc2);
            gbc2.gridx = 1; gbc2.gridy = 3;
            contactPanel.add(updateBtn, gbc2);

            updateBtn.addActionListener(e -> {
                try {
                    String newName = nameField.getText().trim();
                    String newSpec = specField.getText().trim();
                    String newContact = contactField.getText().trim();
                    boolean profOk = doctorService.updateDoctorProfile(doctorId, newName, newSpec);
                    boolean contactOk = doctorService.updateContactInfo(doctorId, newContact);
                    if (profOk || contactOk) {
                        JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                        String[] updated = doctorService.getDoctorProfile(doctorId);
                        if (updated != null) {
                            doctorName = updated[1];
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });
        } catch (Exception e) {
            contactPanel.add(new JLabel("Error loading profile: " + e.getMessage()));
        }

        panel.add(passwordPanel);
        panel.add(contactPanel);
        return panel;
    }

    private JTable createColorCodedTable(List<String[]> data, String[] columns, int statusCol) {
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (String[] row : data) {
            model.addRow(row);
        }
        JTable table = new JTable(model);
        table.setRowHeight(24);
        applyColorCoding(table, statusCol);
        return table;
    }

    private void applyColorCoding(JTable table, int statusCol) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (isSelected) return c;
                String status = tbl.getValueAt(row, statusCol) != null
                        ? tbl.getValueAt(row, statusCol).toString() : "";
                switch (status.toUpperCase()) {
                    case "PENDING":   c.setBackground(new Color(255, 253, 200)); break;
                    case "ACCEPTED":
                    case "CONFIRMED": c.setBackground(new Color(200, 255, 200)); break;
                    case "COMPLETED": c.setBackground(new Color(200, 220, 255)); break;
                    case "REJECTED":
                    case "CANCELLED": c.setBackground(new Color(255, 200, 200)); break;
                    default:          c.setBackground(Color.WHITE);
                }
                return c;
            }
        });
    }

    private void refreshCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        tabbedPane.setComponentAt(0, createOverviewPanel());
        tabbedPane.setComponentAt(1, createPendingAppointmentsPanel());
        tabbedPane.setComponentAt(2, createAppointmentManagementPanel());
        tabbedPane.setComponentAt(3, createConsultationNotesPanel());
        tabbedPane.setComponentAt(4, createSchedulePanel());
        tabbedPane.setComponentAt(5, createPatientHistoryPanel());
        tabbedPane.setComponentAt(6, createSettingsPanel());
        tabbedPane.setSelectedIndex(selectedIndex);
    }

    private Date getWeekStart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
