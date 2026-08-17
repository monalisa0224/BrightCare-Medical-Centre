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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoctorDashboardFrame extends JFrame {

    private DoctorInterface doctorService;
    private int doctorId;
    private String doctorName;
    private String username;
    private JTabbedPane tabbedPane;

    private static final Color PRIMARY       = new Color(53, 47, 68);    // #352F44
    private static final Color PRIMARY_DARK  = new Color(40, 35, 52);    // darker #352F44
    private static final Color PRIMARY_LIGHT = new Color(185, 180, 199); // #B9B4C7
    private static final Color SURFACE       = new Color(255, 248, 231); // #FFF8E7
    private static final Color CARD_BG       = Color.WHITE;
    private static final Color TEXT_PRIMARY  = new Color(53, 47, 68);    // #352F44
    private static final Color TEXT_SECONDARY = new Color(92, 84, 112);  // #5C5470
    private static final Color TEXT_MUTED    = new Color(185, 180, 199); // #B9B4C7
    private static final Color BORDER        = new Color(185, 180, 199); // #B9B4C7
    private static final Color SUCCESS       = new Color(67, 160, 71);
    private static final Color DANGER        = new Color(229, 57, 53);
    private static final Color WARNING       = new Color(255, 160, 0);
    private static final Color INFO          = new Color(92, 84, 112);   // #5C5470
    private static final Color ACCENT        = new Color(185, 180, 199); // #B9B4C7

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

    // =====================================================================
    //  HELPER METHODS - UI utilities
    // =====================================================================

    private JButton makeButton(String text, Color bg, boolean bold, boolean hoverable) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (hoverable) {
            Color darker = bg.darker().darker();
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btn.setBackground(darker); }
                @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
            });
        }
        return btn;
    }

    private JButton makeSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(new Color(236, 239, 241));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(224, 228, 230)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(236, 239, 241)); }
        });
        return btn;
    }

    private JPanel makeSectionHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(TEXT_PRIMARY);
        header.add(lbl, BorderLayout.WEST);
        return header;
    }

    private JPanel makeCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        return card;
    }

    private JPanel makeAccentBar(Color color) {
        JPanel bar = new JPanel();
        bar.setBackground(color);
        bar.setPreferredSize(new Dimension(0, 6));
        return bar;
    }

    private JPanel makeSectionHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(SURFACE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(subLabel);
        return header;
    }

    private void styleTableHeader(JTable table) {
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    }

    private void styleTable(JTable table, int statusCol) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        styleTableHeader(table);
        applyColorCoding(table, statusCol);
    }

    // =====================================================================
    //  BUILD UI
    // =====================================================================

    private void buildUI() {
        setTitle("BrightCare Medical Centre - Doctor Portal");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int frameWidth = Math.max(1050, (int) (screenSize.width * 0.58));
        int frameHeight = Math.max(720, (int) (screenSize.height * 0.72));
        setSize(frameWidth, frameHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(850, 580));
        getContentPane().setBackground(SURFACE);

        add(createTopBar(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(CARD_BG);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        String[] tabTitles = {
            "Overview", "Pending", "Manage",
            "Consultation", "Schedule", "History", "Settings"
        };
        JPanel[] tabPanels = {
            createOverviewPanel(), createPendingAppointmentsPanel(),
            createAppointmentManagementPanel(), createConsultationNotesPanel(),
            createSchedulePanel(), createPatientHistoryPanel(), createSettingsPanel()
        };
        for (int i = 0; i < tabTitles.length; i++) {
            tabbedPane.addTab(tabTitles[i], tabPanels[i]);
        }

        // Custom tab components with compact padding
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setTabComponentAt(i, new JLabel(tabTitles[i]) {{
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            }});
        }

        add(tabbedPane, BorderLayout.CENTER);

        add(createFooter(), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // =====================================================================
    //  TOP BAR
    // =====================================================================

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PRIMARY);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Left side: main header + sub-header
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(PRIMARY);

        JLabel mainHeader = new JLabel("Doctor Portal");
        mainHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mainHeader.setForeground(Color.WHITE);
        mainHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subHeader = new JLabel("Nice to see you, Dr. " + doctorName + "!");
        subHeader.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subHeader.setForeground(PRIMARY_LIGHT);
        subHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(mainHeader);
        leftPanel.add(Box.createVerticalStrut(2));
        leftPanel.add(subHeader);
        topBar.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setBackground(PRIMARY);

        JButton profileBtn = new JButton("Profile");
        profileBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        profileBtn.setForeground(Color.WHITE);
        profileBtn.setBackground(PRIMARY);
        profileBtn.setBorderPainted(false);
        profileBtn.setFocusPainted(false);
        profileBtn.setOpaque(true);
        profileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { profileBtn.setForeground(PRIMARY_LIGHT); }
            @Override public void mouseExited(MouseEvent e)  { profileBtn.setForeground(Color.WHITE); }
        });
        profileBtn.addActionListener(e -> showProfileDialog());

        JButton logoutBtn = makeButton("Logout", DANGER, true, true);
        logoutBtn.addActionListener(e -> doLogout());

        rightPanel.add(profileBtn);
        rightPanel.add(Box.createHorizontalStrut(4));
        rightPanel.add(logoutBtn);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    // =====================================================================
    //  FOOTER
    // =====================================================================

    private JPanel createFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(CARD_BG);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JLabel footer = new JLabel("BrightCare Medical Centre  |  Distributed Systems", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footer.setForeground(TEXT_MUTED);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        footerPanel.add(footer, BorderLayout.CENTER);
        return footerPanel;
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

    // =====================================================================
    //  PROFILE DIALOG
    // =====================================================================

    private void showProfileDialog() {
        try {
            String[] profile = doctorService.getDoctorProfile(doctorId);
            if (profile == null) {
                JOptionPane.showMessageDialog(this, "Profile not found.");
                return;
            }

            JDialog dialog = new JDialog(this, "My Profile", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setSize(420, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setResizable(false);
            dialog.setLayout(new BorderLayout());

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(PRIMARY);
            header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            JLabel headerTitle = new JLabel("My Profile");
            headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            headerTitle.setForeground(Color.WHITE);
            header.add(headerTitle, BorderLayout.WEST);
            dialog.add(header, BorderLayout.NORTH);

            // Content
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(CARD_BG);
            content.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

            // Avatar circle
            JPanel avatarPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int size = 64;
                    int x = (getWidth() - size) / 2;
                    int y = 0;
                    g2.setColor(PRIMARY_LIGHT);
                    g2.fillOval(x, y, size, size);
                    String initial = (profile[1] != null && !profile[1].isEmpty())
                            ? profile[1].substring(0, 1).toUpperCase() : "D";
                    g2.setColor(PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (size - fm.stringWidth(initial)) / 2;
                    int ty = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(initial, tx, ty);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(0, 72);
                }
            };
            avatarPanel.setOpaque(false);
            content.add(avatarPanel);
            content.add(Box.createVerticalStrut(4));

            // Doctor name
            JLabel nameLabel = new JLabel("Dr. " + profile[1], SwingConstants.CENTER);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            nameLabel.setForeground(TEXT_PRIMARY);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(nameLabel);

            // Specialization
            String spec = (profile[2] != null && !profile[2].isEmpty()) ? profile[2] : "General Practitioner";
            JLabel specLabel = new JLabel(spec, SwingConstants.CENTER);
            specLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            specLabel.setForeground(TEXT_SECONDARY);
            specLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(specLabel);

            content.add(Box.createVerticalStrut(20));

            // Separator
            JSeparator sep = new JSeparator();
            sep.setForeground(BORDER);
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            content.add(sep);
            content.add(Box.createVerticalStrut(16));

            // Info rows
            String[][] rows = {
                    {"Doctor ID", profile[0]},
                    {"Full Name", profile[1]},
                    {"Specialization", profile[2] != null ? profile[2] : "N/A"},
                    {"Contact Number", profile[3] != null ? profile[3] : "N/A"},
                    {"Username", profile[4]}
            };

            for (String[] row : rows) {
                JPanel rowPanel = new JPanel(new BorderLayout());
                rowPanel.setBackground(CARD_BG);
                rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                rowPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

                JLabel lblKey = new JLabel(row[0]);
                lblKey.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblKey.setForeground(TEXT_SECONDARY);
                rowPanel.add(lblKey, BorderLayout.WEST);

                JLabel lblVal = new JLabel(row[1] != null ? row[1] : "N/A");
                lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lblVal.setForeground(TEXT_PRIMARY);
                lblVal.setHorizontalAlignment(SwingConstants.RIGHT);
                rowPanel.add(lblVal, BorderLayout.EAST);

                content.add(rowPanel);
                content.add(Box.createVerticalStrut(2));
            }

            dialog.add(content, BorderLayout.CENTER);

            // Footer with close button
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
            footer.setBackground(SURFACE);
            footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
            JButton closeBtn = makeButton("Close", PRIMARY, true, true);
            closeBtn.setPreferredSize(new Dimension(120, 36));
            closeBtn.addActionListener(e -> dialog.dispose());
            footer.add(closeBtn);
            dialog.add(footer, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // =====================================================================
    //  OVERVIEW TAB
    // =====================================================================

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Section header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(SURFACE);
        JLabel sectionTitle = new JLabel("Today's Summary");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(TEXT_PRIMARY);
        headerRow.add(sectionTitle, BorderLayout.WEST);
        JLabel dateLabel = new JLabel(new SimpleDateFormat("EEEE, dd MMMM yyyy").format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(TEXT_SECONDARY);
        headerRow.add(dateLabel, BorderLayout.EAST);
        panel.add(headerRow, BorderLayout.NORTH);

        // Center: stat cards + table
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(SURFACE);

        try {
            int[] summary = doctorService.getTodaySummary(doctorId);

            // Stat cards
            JPanel summaryPanel = new JPanel(new GridLayout(1, 5, 14, 0));
            summaryPanel.setBackground(SURFACE);
            summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            summaryPanel.setPreferredSize(new Dimension(0, 100));
            summaryPanel.add(createStatCard("Pending", summary[0], WARNING));
            summaryPanel.add(createStatCard("Confirmed", summary[1], SUCCESS));
            summaryPanel.add(createStatCard("Completed", summary[2], INFO));
            summaryPanel.add(createStatCard("Cancelled", summary[3], DANGER));
            summaryPanel.add(createStatCard("Total", summary[4], TEXT_PRIMARY));
            centerPanel.add(summaryPanel);
            centerPanel.add(Box.createVerticalStrut(16));

            // Today's appointments table
            JPanel tableSection = new JPanel(new BorderLayout(0, 8));
            tableSection.setBackground(SURFACE);

            JLabel tableTitle = new JLabel("Today's Appointments");
            tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            tableTitle.setForeground(TEXT_PRIMARY);
            tableSection.add(tableTitle, BorderLayout.NORTH);

            List<String[]> todayAppts = doctorService.getTodayAppointments(doctorId);
            String[] columns = {"Appt ID", "Patient", "Time", "Status"};
            JTable table = createColorCodedTable(todayAppts, columns, 3);
            styleTable(table, 3);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
            scroll.getViewport().setBackground(CARD_BG);
            tableSection.add(scroll, BorderLayout.CENTER);

            if (todayAppts.isEmpty()) {
                JLabel emptyLabel = new JLabel("No appointments scheduled for today.", SwingConstants.CENTER);
                emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                emptyLabel.setForeground(TEXT_MUTED);
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
                tableSection.add(emptyLabel, BorderLayout.SOUTH);
            }

            centerPanel.add(tableSection);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading summary: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            errorLabel.setForeground(DANGER);
            centerPanel.add(errorLabel);
        }

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String label, int value, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel accentBar = makeAccentBar(color);
        card.add(accentBar, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(CARD_BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));

        JLabel valLabel = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valLabel.setForeground(color);

        JLabel nameLabel = new JLabel(label, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(TEXT_SECONDARY);

        body.add(valLabel, BorderLayout.CENTER);
        body.add(nameLabel, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // =====================================================================
    //  PENDING APPOINTMENTS TAB
    // =====================================================================

    private JPanel createPendingAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sectionHeader = makeSectionHeader("Pending Appointments",
                "Review and accept or reject incoming patient appointment requests.");
        panel.add(sectionHeader, BorderLayout.NORTH);

        // Top bar with Refresh only
        JPanel btnCard = makeCard();
        btnCard.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnCard.setPreferredSize(new Dimension(0, 48));

        JButton refreshBtn = makeSecondaryButton("Refresh");
        btnCard.add(refreshBtn);
        panel.add(btnCard, BorderLayout.NORTH);

        // Section header + table
        JPanel tableSection = new JPanel(new BorderLayout(0, 8));
        tableSection.setBackground(SURFACE);

        JLabel tableTitle = new JLabel("Pending Appointments");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(TEXT_PRIMARY);
        tableSection.add(tableTitle, BorderLayout.NORTH);

        JTable table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD_BG);
        tableSection.add(scroll, BorderLayout.CENTER);

        panel.add(tableSection, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> refreshPendingTable(table));

        refreshPendingTable(table);
        return panel;
    }

    private void refreshPendingTable(JTable table) {
        try {
            List<String[]> data = doctorService.getPendingAppointments(doctorId);
            String[] columns = {"Appt ID", "Patient", "Contact", "Date", "Time", "Status", "Action"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 6;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 6) return Object.class;
                    return Object.class;
                }
            };
            for (String[] row : data) {
                model.addRow(new Object[]{row[0], row[1], row[2], row[3], row[4], row[5], "Actions"});
            }
            table.setModel(model);
            styleTable(table, 5);
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(6).setPreferredWidth(180);
            table.getColumnModel().getColumn(6).setMinWidth(180);
            table.getColumnModel().getColumn(6).setMaxWidth(220);

            table.getColumnModel().getColumn(6).setCellRenderer(new PendingActionRenderer(table));
            table.getColumnModel().getColumn(6).setCellEditor(new PendingActionEditor(table));
            table.setRowHeight(46);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PendingActionRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton acceptBtn;
        private final JButton rejectBtn;

        PendingActionRenderer(JTable table) {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
            setOpaque(true);

            acceptBtn = new JButton("Accept");
            acceptBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            acceptBtn.setBackground(SUCCESS);
            acceptBtn.setForeground(Color.WHITE);
            acceptBtn.setFocusPainted(false);
            acceptBtn.setBorderPainted(false);
            acceptBtn.setOpaque(true);
            acceptBtn.setPreferredSize(new Dimension(80, 32));
            acceptBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            rejectBtn = new JButton("Reject");
            rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            rejectBtn.setBackground(DANGER);
            rejectBtn.setForeground(Color.WHITE);
            rejectBtn.setFocusPainted(false);
            rejectBtn.setBorderPainted(false);
            rejectBtn.setOpaque(true);
            rejectBtn.setPreferredSize(new Dimension(80, 32));
            rejectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            add(acceptBtn);
            add(rejectBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? tbl.getSelectionBackground() : (row % 2 == 0 ? CARD_BG : SURFACE));
            return this;
        }
    }

    private class PendingActionEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel;
        private final JButton acceptBtn;
        private final JButton rejectBtn;
        private final JTable table;
        private int editingRow;

        PendingActionEditor(JTable table) {
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            panel.setBackground(CARD_BG);

            acceptBtn = new JButton("Accept");
            acceptBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            acceptBtn.setBackground(SUCCESS);
            acceptBtn.setForeground(Color.WHITE);
            acceptBtn.setFocusPainted(false);
            acceptBtn.setBorderPainted(false);
            acceptBtn.setOpaque(true);
            acceptBtn.setPreferredSize(new Dimension(80, 32));
            acceptBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            acceptBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    acceptBtn.setBackground(new Color(46, 125, 50));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    acceptBtn.setBackground(SUCCESS);
                }
            });
            acceptBtn.addActionListener(e -> {
                int apptId = Integer.parseInt(table.getValueAt(editingRow, 0).toString());
                try {
                    boolean ok = doctorService.acceptAppointment(apptId);
                    JOptionPane.showMessageDialog(DoctorDashboardFrame.this,
                            ok ? "Appointment accepted successfully!" : "Failed to accept.");
                    if (ok) {
                        refreshPendingTable(table);
                        refreshCurrentTab();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DoctorDashboardFrame.this, "Error: " + ex.getMessage());
                }
                fireEditingStopped();
            });

            rejectBtn = new JButton("Reject");
            rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            rejectBtn.setBackground(DANGER);
            rejectBtn.setForeground(Color.WHITE);
            rejectBtn.setFocusPainted(false);
            rejectBtn.setBorderPainted(false);
            rejectBtn.setOpaque(true);
            rejectBtn.setPreferredSize(new Dimension(80, 32));
            rejectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            rejectBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    rejectBtn.setBackground(new Color(198, 40, 40));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    rejectBtn.setBackground(DANGER);
                }
            });
            rejectBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(DoctorDashboardFrame.this,
                        "Reject this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    fireEditingStopped();
                    return;
                }
                int apptId = Integer.parseInt(table.getValueAt(editingRow, 0).toString());
                try {
                    boolean ok = doctorService.rejectAppointment(apptId);
                    JOptionPane.showMessageDialog(DoctorDashboardFrame.this,
                            ok ? "Appointment rejected." : "Failed to reject.");
                    if (ok) {
                        refreshPendingTable(table);
                        refreshCurrentTab();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DoctorDashboardFrame.this, "Error: " + ex.getMessage());
                }
                fireEditingStopped();
            });

            panel.add(acceptBtn);
            panel.add(rejectBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSelected, int row, int column) {
            editingRow = row;
            panel.setBackground(row % 2 == 0 ? CARD_BG : SURFACE);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }

    // =====================================================================
    //  MANAGE APPOINTMENTS TAB
    // =====================================================================

    private JPanel createAppointmentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sectionHeader = makeSectionHeader("Manage Appointments",
                "Cancel or reschedule your accepted appointments using the action buttons below.");
        panel.add(sectionHeader, BorderLayout.NORTH);

        // Top bar with Refresh only
        JPanel btnCard = makeCard();
        btnCard.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnCard.setPreferredSize(new Dimension(0, 48));

        JButton refreshBtn = makeSecondaryButton("Refresh");
        btnCard.add(refreshBtn);
        panel.add(btnCard, BorderLayout.NORTH);

        // Table
        JPanel tableSection = new JPanel(new BorderLayout(0, 8));
        tableSection.setBackground(SURFACE);

        JLabel tableTitle = new JLabel("Accepted and Pending Appointments");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(TEXT_PRIMARY);
        tableSection.add(tableTitle, BorderLayout.NORTH);

        JTable table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD_BG);
        tableSection.add(scroll, BorderLayout.CENTER);

        panel.add(tableSection, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> refreshManagedAppointments(table));

        refreshManagedAppointments(table);
        return panel;
    }

    private void refreshManagedAppointments(JTable table) {
        try {
            List<String[]> appointments = doctorService.getDoctorAppointments(doctorId);
            String[] columns = {"Appt ID", "Patient", "Date", "Time", "Status", "Action"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 5;
                }
            };
            for (String[] row : appointments) {
                if ("PENDING".equals(row[4]) || "ACCEPTED".equals(row[4])) {
                    model.addRow(new Object[]{row[0], row[1], row[2], row[3], row[4], "Actions"});
                }
            }
            table.setModel(model);
            styleTable(table, 4);
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(5).setPreferredWidth(220);
            table.getColumnModel().getColumn(5).setMinWidth(220);
            table.getColumnModel().getColumn(5).setMaxWidth(260);

            table.getColumnModel().getColumn(5).setCellRenderer(new ManageActionRenderer(table));
            table.getColumnModel().getColumn(5).setCellEditor(new ManageActionEditor(table));
            table.setRowHeight(50);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private class ManageActionRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton cancelBtn;
        private final JButton rescheduleBtn;

        ManageActionRenderer(JTable table) {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
            setOpaque(true);

            cancelBtn = new JButton("Cancel");
            cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            cancelBtn.setBackground(DANGER);
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setBorderPainted(false);
            cancelBtn.setOpaque(true);
            cancelBtn.setPreferredSize(new Dimension(80, 34));
            cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            rescheduleBtn = new JButton("Reschedule");
            rescheduleBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            rescheduleBtn.setBackground(PRIMARY);
            rescheduleBtn.setForeground(Color.WHITE);
            rescheduleBtn.setFocusPainted(false);
            rescheduleBtn.setBorderPainted(false);
            rescheduleBtn.setOpaque(true);
            rescheduleBtn.setPreferredSize(new Dimension(110, 34));
            rescheduleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            add(cancelBtn);
            add(rescheduleBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? tbl.getSelectionBackground() : (row % 2 == 0 ? CARD_BG : SURFACE));
            return this;
        }
    }

    private class ManageActionEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel;
        private final JButton cancelBtn;
        private final JButton rescheduleBtn;
        private final JTable table;
        private int editingRow;

        ManageActionEditor(JTable table) {
            this.table = table;
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            panel.setBackground(CARD_BG);

            cancelBtn = new JButton("Cancel");
            cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            cancelBtn.setBackground(DANGER);
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setBorderPainted(false);
            cancelBtn.setOpaque(true);
            cancelBtn.setPreferredSize(new Dimension(72, 30));
            cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cancelBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    cancelBtn.setBackground(new Color(198, 40, 40));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    cancelBtn.setBackground(DANGER);
                }
            });
            cancelBtn.addActionListener(e -> {
                int apptId = Integer.parseInt(table.getValueAt(editingRow, 0).toString());
                String status = table.getValueAt(editingRow, 4).toString();
                int confirm = JOptionPane.showConfirmDialog(DoctorDashboardFrame.this,
                        "Cancel the selected " + status.toLowerCase() + " appointment?",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    fireEditingStopped();
                    return;
                }
                try {
                    boolean ok = doctorService.cancelAppointmentByDoctor(apptId);
                    JOptionPane.showMessageDialog(DoctorDashboardFrame.this,
                            ok ? "Appointment cancelled." : "Unable to cancel the selected appointment.");
                    if (ok) {
                        refreshManagedAppointments(table);
                        refreshCurrentTab();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DoctorDashboardFrame.this, "Error: " + ex.getMessage());
                }
                fireEditingStopped();
            });

            rescheduleBtn = new JButton("Reschedule");
            rescheduleBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            rescheduleBtn.setBackground(PRIMARY);
            rescheduleBtn.setForeground(Color.WHITE);
            rescheduleBtn.setFocusPainted(false);
            rescheduleBtn.setBorderPainted(false);
            rescheduleBtn.setOpaque(true);
            rescheduleBtn.setPreferredSize(new Dimension(110, 34));
            rescheduleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            rescheduleBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    rescheduleBtn.setBackground(PRIMARY_DARK);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    rescheduleBtn.setBackground(PRIMARY);
                }
            });
            rescheduleBtn.addActionListener(e -> {
                int apptId = Integer.parseInt(table.getValueAt(editingRow, 0).toString());
                String currentDate = table.getValueAt(editingRow, 2).toString();
                String currentTime = table.getValueAt(editingRow, 3).toString();
                showRescheduleCalendar(apptId, currentDate, currentTime);
                fireEditingStopped();
            });

            panel.add(cancelBtn);
            panel.add(rescheduleBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable tbl, Object value,
                boolean isSelected, int row, int column) {
            editingRow = row;
            panel.setBackground(row % 2 == 0 ? CARD_BG : SURFACE);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }

    // =====================================================================
    //  CONSULTATION NOTES TAB
    // =====================================================================

    private JPanel createConsultationNotesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sectionHeader = makeSectionHeader("Consultation Notes",
                "Load an appointment and document diagnosis, treatment, and prescription details.");
        panel.add(sectionHeader, BorderLayout.NORTH);

        // Selector bar
        JPanel selectorCard = makeCard();
        selectorCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        selectorCard.setPreferredSize(new Dimension(0, 52));

        JButton loadBtn = makeSecondaryButton("Load Appointments");
        JLabel selLabel = new JLabel("  Select Appointment:  ");
        selLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selLabel.setForeground(TEXT_PRIMARY);

        JComboBox<String> apptCombo = new JComboBox<>();
        apptCombo.setPreferredSize(new Dimension(480, 32));
        apptCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        selectorCard.add(loadBtn);
        selectorCard.add(selLabel);
        selectorCard.add(apptCombo);
        panel.add(selectorCard, BorderLayout.NORTH);

        // Form card
        JPanel formCard = new JPanel(new BorderLayout(0, 12));
        formCard.setBackground(CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));

        JLabel formTitle = new JLabel("Consultation Notes");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(TEXT_PRIMARY);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1.0;

        JTextArea diagnosisArea = new JTextArea(3, 30);
        JTextArea treatmentArea = new JTextArea(3, 30);
        JTextArea prescriptionArea = new JTextArea(3, 30);
        JTextArea notesArea = new JTextArea(3, 30);
        Font textFont = new Font("Segoe UI", Font.PLAIN, 13);
        diagnosisArea.setFont(textFont);
        treatmentArea.setFont(textFont);
        prescriptionArea.setFont(textFont);
        notesArea.setFont(textFont);

        String[] labels = {"Diagnosis:", "Treatment:", "Prescription:", "Additional Notes:"};
        JTextArea[] areas = {diagnosisArea, treatmentArea, prescriptionArea, notesArea};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weighty = 1.0; gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(TEXT_PRIMARY);
            fieldsPanel.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
            JScrollPane sp = new JScrollPane(areas[i]);
            sp.setPreferredSize(new Dimension(400, 80));
            fieldsPanel.add(sp, gbc);
        }

        JButton saveBtn = makeButton("Save Consultation Notes", PRIMARY, true, true);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weighty = 0; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(12, 4, 4, 4);
        fieldsPanel.add(saveBtn, gbc);

        formCard.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(formCard, BorderLayout.CENTER);

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

        saveBtn.addActionListener(e -> {
            if (apptCombo.getItemCount() == 0 || apptCombo.getSelectedItem() == null) return;
            String sel = apptCombo.getSelectedItem().toString().trim();
            if (sel.equals("No accepted/completed appointments")) return;
            int apptId = Integer.parseInt(sel.split(" \\| ")[0].trim());
            String patientUser = sel.split(" \\| ")[1].trim();
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

    // =====================================================================
    //  MY SCHEDULE TAB
    // =====================================================================

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sectionHeader = makeSectionHeader("My Schedule",
                "Browse your weekly timetable and navigate between weeks using the controls.");
        panel.add(sectionHeader, BorderLayout.NORTH);

        // Control bar
        JPanel controlCard = makeCard();
        controlCard.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 8));
        controlCard.setPreferredSize(new Dimension(0, 50));

        JButton prevWeekBtn = makeSecondaryButton("< Previous");
        prevWeekBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton nextWeekBtn = makeSecondaryButton("Next >");
        nextWeekBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton refreshBtn = makeSecondaryButton("Refresh");
        JLabel weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        weekLabel.setForeground(PRIMARY);

        controlCard.add(prevWeekBtn);
        controlCard.add(weekLabel);
        controlCard.add(nextWeekBtn);
        controlCard.add(refreshBtn);
        panel.add(controlCard, BorderLayout.NORTH);

        // Schedule grid
        JPanel gridSection = new JPanel(new BorderLayout(0, 8));
        gridSection.setBackground(SURFACE);

        JPanel gridHeader = new JPanel(new BorderLayout());
        gridHeader.setBackground(SURFACE);
        JLabel gridTitle = new JLabel("Weekly Timetable");
        gridTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gridTitle.setForeground(TEXT_PRIMARY);
        JLabel gridHint = new JLabel("Click cells to toggle availability");
        gridHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gridHint.setForeground(TEXT_SECONDARY);
        gridHeader.add(gridTitle, BorderLayout.WEST);
        gridHeader.add(gridHint, BorderLayout.EAST);
        gridSection.add(gridHeader, BorderLayout.NORTH);

        JPanel scheduleGrid = new JPanel();
        gridSection.add(scheduleGrid, BorderLayout.CENTER);
        panel.add(gridSection, BorderLayout.CENTER);

        final Date[] currentWeekStart = {getWeekStart(new Date())};
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat displaySdf = new SimpleDateFormat("EEE dd/MM");
        final String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        final Date today = new Date();

        final Runnable[] refreshHolder = new Runnable[1];
        refreshHolder[0] = () -> {
            try {
                String startDate = sdf.format(currentWeekStart[0]);
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentWeekStart[0]);
                cal.add(Calendar.DAY_OF_MONTH, 4);

                List<String[]> slots = doctorService.getDoctorTimetable(doctorId, startDate);
                Calendar c = Calendar.getInstance();
                c.setTime(currentWeekStart[0]);
                weekLabel.setText(displaySdf.format(c.getTime()) + "  —  "
                        + displaySdf.format(cal.getTime()));

                scheduleGrid.removeAll();
                scheduleGrid.setLayout(new GridLayout(6, 6, 2, 2));
                scheduleGrid.setBackground(CARD_BG);

                // Header row
                JPanel cornerCell = new JPanel();
                cornerCell.setBackground(PRIMARY);
                cornerCell.add(new JLabel("Day / Time"));
                JLabel cornerLbl = (JLabel) cornerCell.getComponent(0);
                cornerLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                cornerLbl.setForeground(Color.WHITE);
                scheduleGrid.add(cornerCell);

                for (String slot : Constants.DEFAULT_SLOTS) {
                    JPanel slotHeader = new JPanel();
                    slotHeader.setBackground(PRIMARY);
                    JLabel sl = new JLabel(slot, SwingConstants.CENTER);
                    sl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    sl.setForeground(Color.WHITE);
                    slotHeader.add(sl);
                    scheduleGrid.add(slotHeader);
                }

                Calendar dayCal = Calendar.getInstance();
                dayCal.setTime(currentWeekStart[0]);

                for (int d = 0; d < 5; d++) {
                    String dateStr = sdf.format(dayCal.getTime());
                    Calendar todayCal = Calendar.getInstance();
                    todayCal.setTime(today);
                    Calendar dateCal = Calendar.getInstance();
                    dateCal.setTime(dayCal.getTime());
                    boolean isPast = dateCal.before(todayCal)
                            && !sdf.format(dateCal.getTime()).equals(sdf.format(todayCal.getTime()));

                    JPanel dayCell = new JPanel();
                    dayCell.setBackground(isPast ? new Color(245, 245, 245) : new Color(232, 245, 233));
                    dayCell.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                    JLabel dayLabel = new JLabel(displaySdf.format(dayCal.getTime()),
                            SwingConstants.CENTER);
                    dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    dayLabel.setForeground(isPast ? TEXT_MUTED : TEXT_PRIMARY);
                    dayCell.add(dayLabel);
                    scheduleGrid.add(dayCell);

                    for (int si = 0; si < Constants.DEFAULT_SLOTS.length; si++) {
                        final String fSlot = Constants.DEFAULT_SLOTS[si];
                        final String fDateStr = dateStr;

                        boolean found = false;
                        boolean isAvailable = true;
                        for (String[] s : slots) {
                            if (s[0].equals(fDateStr) && s[1].equals(fSlot)) {
                                found = true;
                                isAvailable = Boolean.parseBoolean(s[2]);
                                break;
                            }
                        }

                        JButton cellBtn = new JButton();
                        cellBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                        cellBtn.setFocusPainted(false);
                        cellBtn.setBorderPainted(false);
                        cellBtn.setOpaque(true);
                        cellBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                        if (isPast) {
                            cellBtn.setText("--");
                            cellBtn.setEnabled(false);
                            cellBtn.setBackground(new Color(245, 245, 245));
                            cellBtn.setForeground(TEXT_MUTED);
                        } else if (found && !isAvailable) {
                            cellBtn.setText("Unavailable");
                            cellBtn.setBackground(new Color(255, 235, 238));
                            cellBtn.setForeground(DANGER);
                            cellBtn.addActionListener(ev -> {
                                try {
                                    boolean ok = doctorService.updateDoctorSchedule(doctorId, fDateStr, fSlot, true);
                                    if (ok) refreshHolder[0].run();
                                    else JOptionPane.showMessageDialog(panel, "Cannot enable this slot.");
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
                                }
                            });
                        } else {
                            cellBtn.setText("Available");
                            cellBtn.setBackground(new Color(232, 245, 233));
                            cellBtn.setForeground(SUCCESS);
                            cellBtn.addActionListener(ev -> {
                                try {
                                    boolean ok = doctorService.updateDoctorSchedule(doctorId, fDateStr, fSlot, false);
                                    if (ok) refreshHolder[0].run();
                                    else JOptionPane.showMessageDialog(panel,
                                            "Cannot disable: slot has active appointments.");
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
                                }
                            });
                        }

                        cellBtn.setBorder(BorderFactory.createLineBorder(BORDER));
                        scheduleGrid.add(cellBtn);
                    }
                    dayCal.add(Calendar.DAY_OF_MONTH, 1);
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
            refreshHolder[0].run();
        });

        nextWeekBtn.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentWeekStart[0]);
            cal.add(Calendar.DAY_OF_MONTH, 7);
            currentWeekStart[0] = cal.getTime();
            refreshHolder[0].run();
        });

        refreshBtn.addActionListener(e -> refreshHolder[0].run());
        refreshHolder[0].run();
        return panel;
    }

    // =====================================================================
    //  PATIENT HISTORY TAB
    // =====================================================================

    private JPanel createPatientHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sectionHeader = makeSectionHeader("Patient History",
                "Load patients and click a row to view their past consultation records.");
        panel.add(sectionHeader, BorderLayout.NORTH);

        // Button bar
        JPanel btnCard = makeCard();
        btnCard.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnCard.setPreferredSize(new Dimension(0, 48));

        JButton loadPatientsBtn = makeSecondaryButton("Load Patients");
        JButton viewConsultBtn = makeButton("View Consultation", PRIMARY, true, true);

        btnCard.add(loadPatientsBtn);
        btnCard.add(viewConsultBtn);
        panel.add(btnCard, BorderLayout.NORTH);

        // Split pane
        JTable patientTable = new JTable(new DefaultTableModel(
                new Object[]{"Patient", "Consultations", "Last Visit"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTableHeader(patientTable);
        patientTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        patientTable.setRowHeight(34);
        patientTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        patientTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        patientTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        JScrollPane patientScroll = new JScrollPane(patientTable);
        patientScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        patientScroll.getViewport().setBackground(CARD_BG);

        // Patient section with header
        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setBackground(SURFACE);
        JLabel leftTitle = new JLabel("Patients");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        leftTitle.setForeground(TEXT_PRIMARY);
        leftPanel.add(leftTitle, BorderLayout.NORTH);
        leftPanel.add(patientScroll, BorderLayout.CENTER);

        JTable historyTable = new JTable();
        historyTable.setRowHeight(34);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        styleTableHeader(historyTable);
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        historyScroll.getViewport().setBackground(CARD_BG);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setBackground(SURFACE);
        JLabel rightTitle = new JLabel("Medical History");
        rightTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rightTitle.setForeground(TEXT_PRIMARY);
        rightPanel.add(rightTitle, BorderLayout.NORTH);
        rightPanel.add(historyScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerLocation(360);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        panel.add(splitPane, BorderLayout.CENTER);

        loadPatientsBtn.addActionListener(e -> loadPatientsTable(patientTable));

        patientTable.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = patientTable.getSelectedRow();
            if (row >= 0) {
                Object val = patientTable.getValueAt(row, 0);
                if (val != null && !val.toString().equals("No previous patients")) {
                    fillHistoryForPatient(val.toString(), historyTable);
                }
            }
        });

        viewConsultBtn.addActionListener(e -> {
            int row = patientTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a patient from the table first.");
                return;
            }
            Object val = patientTable.getValueAt(row, 0);
            if (val == null || val.toString().isEmpty()
                    || val.toString().equals("No previous patients")) {
                JOptionPane.showMessageDialog(this, "Please select a patient from the table first.");
                return;
            }
            showConsultationDialog(val.toString());
        });

        return panel;
    }

    private void fillHistoryForPatient(String patientUser, JTable table) {
        if (patientUser == null || patientUser.isEmpty()
                || patientUser.equals("No previous patients")) {
            table.setModel(new DefaultTableModel());
            return;
        }
        try {
            List<String[]> history = doctorService.getPatientHistory(patientUser);
            String[] columns = {"Appt ID", "Date", "Time", "Status", "Diagnosis",
                    "Treatment", "Prescription", "Notes"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            for (String[] row : history) {
                model.addRow(row);
            }
            table.setModel(model);
            styleTable(table, 3);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // =====================================================================
    //  CONSULTATION DIALOG
    // =====================================================================

    private void showConsultationDialog(String patientUser) {
        try {
            List<String[]> notes = doctorService.getConsultationNotesByPatient(patientUser);
            if (notes == null || notes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No consultation notes found for patient: " + patientUser);
                return;
            }

            JDialog dialog = new JDialog(this, "Consultation Records - " + patientUser, true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setSize(840, 600);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Header
            JPanel banner = new JPanel(new BorderLayout());
            banner.setBackground(PRIMARY);
            banner.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
            JLabel header = new JLabel("Consultation Records  —  " + patientUser
                    + "   (" + notes.size() + " record" + (notes.size() == 1 ? "" : "s") + ")");
            header.setFont(new Font("Segoe UI", Font.BOLD, 15));
            header.setForeground(Color.WHITE);
            banner.add(header, BorderLayout.WEST);
            dialog.add(banner, BorderLayout.NORTH);

            // Table
            String[] columns = {"No.", "Date", "Time", "Diagnosis", "Treatment", "Prescription", "Notes"};
            DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            int seq = 0;
            for (String[] n : notes) {
                tableModel.addRow(new Object[]{
                        ++seq,
                        n[2] != null ? n[2] : "N/A",
                        n[3] != null ? n[3] : "N/A",
                        n[5] != null ? n[5] : "N/A",
                        n[6] != null ? n[6] : "N/A",
                        n[7] != null ? n[7] : "N/A",
                        n[8] != null ? n[8] : "N/A"
                });
            }

            JTable notesTable = new JTable(tableModel);
            notesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            notesTable.setRowHeight(32);
            notesTable.setShowGrid(false);
            notesTable.setIntercellSpacing(new Dimension(0, 0));
            notesTable.setSelectionBackground(PRIMARY_LIGHT);
            notesTable.setSelectionForeground(TEXT_PRIMARY);
            styleTableHeader(notesTable);
            notesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                    if (!isSelected) {
                        c.setBackground(row % 2 == 0 ? CARD_BG : SURFACE);
                    }
                    return c;
                }
            });
            notesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane notesScroll = new JScrollPane(notesTable);
            notesScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
            notesScroll.getViewport().setBackground(CARD_BG);

            // Detail area
            JTextArea detailArea = new JTextArea(8, 60);
            detailArea.setEditable(false);
            detailArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            detailArea.setLineWrap(true);
            detailArea.setWrapStyleWord(true);
            detailArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            detailArea.setBackground(CARD_BG);
            JScrollPane detailScroll = new JScrollPane(detailArea);
            detailScroll.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)));

            // Detail section header
            JPanel detailSection = new JPanel(new BorderLayout(0, 6));
            detailSection.setBackground(SURFACE);
            JLabel detailTitle = new JLabel("Selected Consultation");
            detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            detailTitle.setForeground(TEXT_PRIMARY);
            detailSection.add(detailTitle, BorderLayout.NORTH);
            detailSection.add(detailScroll, BorderLayout.CENTER);

            // Content area
            JPanel content = new JPanel(new BorderLayout(0, 10));
            content.setBackground(SURFACE);
            content.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            content.add(notesScroll, BorderLayout.CENTER);
            content.add(detailSection, BorderLayout.SOUTH);
            dialog.add(content, BorderLayout.CENTER);

            // Buttons
            JButton closeBtn = makeButton("Close", PRIMARY, true, true);
            closeBtn.setPreferredSize(new Dimension(100, 34));
            closeBtn.addActionListener(e -> dialog.dispose());

            JButton refreshBtn = makeSecondaryButton("Refresh");
            refreshBtn.setPreferredSize(new Dimension(100, 34));
            refreshBtn.addActionListener(e -> {
                dialog.dispose();
                showConsultationDialog(patientUser);
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            buttonPanel.setBackground(SURFACE);
            buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
            buttonPanel.add(refreshBtn);
            buttonPanel.add(closeBtn);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            Runnable updateDetail = () -> {
                int row = notesTable.getSelectedRow();
                if (row < 0) return;
                String[] n = notes.get(row);
                StringBuilder sb = new StringBuilder();
                sb.append("Record           : #").append(row + 1)
                        .append("   (Appointment ID: ").append(n[1]).append(")\n");
                sb.append("Appointment Date : ").append(n[2])
                        .append("  ").append(n[3] != null ? n[3] : "").append("\n");
                if (n[4] != null && !n[4].isEmpty()) {
                    sb.append("Consulted Date   : ").append(n[4]).append("\n");
                }
                sb.append("\nDiagnosis     :\n").append(emptyNull(n[5])).append("\n");
                sb.append("\nTreatment     :\n").append(emptyNull(n[6])).append("\n");
                sb.append("\nPrescription  :\n").append(emptyNull(n[7])).append("\n");
                sb.append("\nNotes         :\n").append(emptyNull(n[8])).append("\n");
                detailArea.setText(sb.toString());
                detailArea.setCaretPosition(0);
            };

            notesTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) updateDetail.run();
            });
            notesTable.getSelectionModel().setSelectionInterval(0, 0);

            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private String emptyNull(String s) {
        return (s == null || s.isEmpty()) ? "(not provided)" : s;
    }

    // =====================================================================
    //  SETTINGS TAB
    // =====================================================================

    private void loadPatientsTable(JTable patientTable) {
        DefaultTableModel model = (DefaultTableModel) patientTable.getModel();
        model.setRowCount(0);
        try {
            List<String[]> patients = doctorService.getDistinctPatientsForDoctor(doctorId);
            if (patients == null || patients.isEmpty()) {
                model.addRow(new Object[]{"No previous patients", "", ""});
                return;
            }
            for (String[] p : patients) {
                String uname = p[0];
                int count = 0;
                String lastVisit = "";
                List<String[]> notes = doctorService.getConsultationNotesByPatient(uname);
                if (notes != null && !notes.isEmpty()) {
                    count = notes.size();
                    lastVisit = notes.get(0)[2] + " " + (notes.get(0)[3] != null ? notes.get(0)[3] : "");
                }
                model.addRow(new Object[]{uname, count, lastVisit});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private JPanel createSettingsPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout(0, 12));
        outerPanel.setBackground(SURFACE);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel sectionHeader = makeSectionHeader("Settings",
                "Update your password or log out of your account securely.");
        outerPanel.add(sectionHeader, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setBackground(SURFACE);

        // --- Change Password Card ---
        JPanel passwordCard = new JPanel(new BorderLayout(0, 12));
        passwordCard.setBackground(CARD_BG);
        passwordCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel pwAccent = makeAccentBar(PRIMARY);
        passwordCard.add(pwAccent, BorderLayout.NORTH);

        JPanel pwBody = new JPanel();
        pwBody.setLayout(new BoxLayout(pwBody, BoxLayout.Y_AXIS));
        pwBody.setBackground(CARD_BG);
        pwBody.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));

        JLabel pwTitle = new JLabel("Change Password");
        pwTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pwTitle.setForeground(TEXT_PRIMARY);
        pwTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwBody.add(pwTitle);

        JLabel pwSub = new JLabel("Keep your account secure");
        pwSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pwSub.setForeground(TEXT_SECONDARY);
        pwSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwBody.add(pwSub);
        pwBody.add(Box.createVerticalStrut(16));

        JPasswordField currentPw = new JPasswordField(20);
        JPasswordField newPw = new JPasswordField(20);
        JPasswordField confirmPw = new JPasswordField(20);
        Font pwFont = new Font("Segoe UI", Font.PLAIN, 13);
        currentPw.setFont(pwFont);
        newPw.setFont(pwFont);
        confirmPw.setFont(pwFont);

        String[] pwLabels = {"Current Password:", "New Password:", "Confirm Password:"};
        JPasswordField[] pwFields = {currentPw, newPw, confirmPw};

        for (int i = 0; i < pwLabels.length; i++) {
            JLabel lbl = new JLabel(pwLabels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(TEXT_SECONDARY);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            pwBody.add(lbl);
            pwBody.add(Box.createVerticalStrut(4));
            pwFields[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            pwFields[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            pwBody.add(pwFields[i]);
            pwBody.add(Box.createVerticalStrut(10));
        }

        JButton changePwBtn = makeButton("Change Password", PRIMARY, true, true);
        changePwBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        changePwBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwBody.add(changePwBtn);

        passwordCard.add(pwBody, BorderLayout.CENTER);

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
                if (ok) {
                    currentPw.setText("");
                    newPw.setText("");
                    confirmPw.setText("");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // --- Update Profile Card ---
        JPanel profileCard = new JPanel(new BorderLayout(0, 12));
        profileCard.setBackground(CARD_BG);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel profAccent = makeAccentBar(INFO);
        profileCard.add(profAccent, BorderLayout.NORTH);

        JPanel profBody = new JPanel();
        profBody.setLayout(new BoxLayout(profBody, BoxLayout.Y_AXIS));
        profBody.setBackground(CARD_BG);
        profBody.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));

        JLabel profTitle = new JLabel("Update Profile");
        profTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        profTitle.setForeground(TEXT_PRIMARY);
        profTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        profBody.add(profTitle);

        JLabel profSub = new JLabel("Manage your personal information");
        profSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        profSub.setForeground(TEXT_SECONDARY);
        profSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        profBody.add(profSub);
        profBody.add(Box.createVerticalStrut(16));

        try {
            String[] profile = doctorService.getDoctorProfile(doctorId);
            String currentContact = (profile != null && profile[3] != null) ? profile[3] : "";

            JTextField nameField = new JTextField(profile != null ? profile[1] : "", 20);
            JTextField specField = new JTextField(profile != null ? profile[2] : "", 20);
            JTextField contactField = new JTextField(currentContact, 20);
            Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
            nameField.setFont(fieldFont);
            specField.setFont(fieldFont);
            contactField.setFont(fieldFont);

            String[] profLabels = {"Doctor Name:", "Specialization:", "Contact Number:"};
            JTextField[] profFields = {nameField, specField, contactField};

            for (int i = 0; i < profLabels.length; i++) {
                JLabel lbl = new JLabel(profLabels[i]);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(TEXT_SECONDARY);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                profBody.add(lbl);
                profBody.add(Box.createVerticalStrut(4));
                profFields[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                profFields[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                profBody.add(profFields[i]);
                profBody.add(Box.createVerticalStrut(10));
            }

            JButton updateBtn = makeButton("Update Profile", PRIMARY, true, true);
            updateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            updateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            profBody.add(updateBtn);

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
            profBody.add(new JLabel("Error loading profile: " + e.getMessage()));
        }

        profileCard.add(profBody, BorderLayout.CENTER);

        panel.add(passwordCard);
        panel.add(profileCard);
        outerPanel.add(panel, BorderLayout.CENTER);
        return outerPanel;
    }

    // =====================================================================
    //  TABLE UTILITIES
    // =====================================================================

    private JTable createColorCodedTable(List<String[]> data, String[] columns, int statusCol) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (String[] row : data) {
            model.addRow(row);
        }
        JTable table = new JTable(model);
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
                    case "PENDING":
                        c.setBackground(new Color(255, 248, 225));
                        break;
                    case "ACCEPTED":
                    case "CONFIRMED":
                        c.setBackground(new Color(232, 245, 233));
                        break;
                    case "COMPLETED":
                        c.setBackground(new Color(227, 242, 253));
                        break;
                    case "REJECTED":
                    case "CANCELLED":
                        c.setBackground(new Color(255, 235, 238));
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                }
                return c;
            }
        });
    }

    // =====================================================================
    //  RESCHEDULE CALENDAR DIALOG
    // =====================================================================

    private void showRescheduleCalendar(int apptId, String currentDate, String currentTime) {
        JDialog dialog = new JDialog(this, "Reschedule Appointment", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // Today's date for comparison
        final String todayStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        final String nowTimeStr = new SimpleDateFormat("HH:mm").format(new Date());

        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel headerLeft = new JPanel(new GridLayout(2, 1));
        headerLeft.setBackground(PRIMARY);
        JLabel titleLabel = new JLabel("Select New Date & Time");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        JLabel subTitleLabel = new JLabel("Current: " + currentDate + "  " + currentTime);
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subTitleLabel.setForeground(PRIMARY_LIGHT);
        headerLeft.add(titleLabel);
        headerLeft.add(subTitleLabel);
        header.add(headerLeft, BorderLayout.WEST);

        // Week navigation
        JPanel weekNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        weekNav.setBackground(PRIMARY);
        JButton prevBtn = new JButton("< Prev Week");
        prevBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        prevBtn.setBackground(PRIMARY_DARK);
        prevBtn.setForeground(Color.WHITE);
        prevBtn.setFocusPainted(false);
        prevBtn.setBorderPainted(false);
        prevBtn.setOpaque(true);
        prevBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton nextBtn = new JButton("Next Week >");
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nextBtn.setBackground(PRIMARY_DARK);
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setFocusPainted(false);
        nextBtn.setBorderPainted(false);
        nextBtn.setOpaque(true);
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        weekLabel.setForeground(Color.WHITE);
        weekLabel.setPreferredSize(new Dimension(180, 20));

        weekNav.add(prevBtn);
        weekNav.add(weekLabel);
        weekNav.add(nextBtn);
        header.add(weekNav, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);

        // ── SELECTED STATE ──
        final String[] selectedDate = {currentDate};
        final String[] selectedTime = {currentTime};

        // ── LEGEND BAR ──
        JPanel legendBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        legendBar.setBackground(SURFACE);
        legendBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        legendBar.add(new JLabel("Select a future date and available time slot.") {{
            setFont(new Font("Segoe UI", Font.ITALIC, 12));
            setForeground(TEXT_SECONDARY);
        }});

        // ── TIME SLOT PANEL ──
        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setBackground(SURFACE);
        timePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JLabel timeHeader = new JLabel("  Select Time Slot:");
        timeHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        timeHeader.setForeground(TEXT_PRIMARY);
        timeHeader.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        timePanel.add(timeHeader, BorderLayout.NORTH);

        JPanel slotRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        slotRow.setBackground(SURFACE);

        String[] timeSlots = Constants.DEFAULT_SLOTS;
        final ButtonGroup slotGroup = new ButtonGroup();
        final JToggleButton[] allSlotBtns = new JToggleButton[timeSlots.length];

        for (int s = 0; s < timeSlots.length; s++) {
            final String slotTime = timeSlots[s];
            JToggleButton slotBtn = new JToggleButton(slotTime);
            slotBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            slotBtn.setBackground(Color.WHITE);
            slotBtn.setForeground(TEXT_PRIMARY);
            slotBtn.setFocusPainted(false);
            slotBtn.setOpaque(true);
            slotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            slotBtn.setPreferredSize(new Dimension(90, 36));
            slotBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));

            if (slotTime.equals(currentTime)) {
                slotBtn.setSelected(true);
                slotBtn.setBackground(PRIMARY);
                slotBtn.setForeground(Color.WHITE);
                slotBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 1),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            }

            final JToggleButton thisBtn = slotBtn;
            slotBtn.addActionListener(ev -> {
                selectedTime[0] = slotTime;
                for (int i = 0; i < allSlotBtns.length; i++) {
                    if (allSlotBtns[i] != null && allSlotBtns[i].isEnabled()) {
                        allSlotBtns[i].setBackground(Color.WHITE);
                        allSlotBtns[i].setForeground(TEXT_PRIMARY);
                        allSlotBtns[i].setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER, 1),
                                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                    }
                }
                thisBtn.setBackground(PRIMARY);
                thisBtn.setForeground(Color.WHITE);
                thisBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 1),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            });

            slotGroup.add(slotBtn);
            slotRow.add(slotBtn);
            allSlotBtns[s] = slotBtn;
        }

        timePanel.add(slotRow, BorderLayout.CENTER);

        // ── CONFIRM BUTTONS ──
        JPanel confirmPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        confirmPanel.setBackground(SURFACE);
        confirmPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton cancelDialogBtn = new JButton("Cancel");
        cancelDialogBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelDialogBtn.setBackground(new Color(224, 224, 224));
        cancelDialogBtn.setForeground(TEXT_PRIMARY);
        cancelDialogBtn.setFocusPainted(false);
        cancelDialogBtn.setBorderPainted(false);
        cancelDialogBtn.setOpaque(true);
        cancelDialogBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelDialogBtn.addActionListener(ev -> dialog.dispose());

        JButton confirmBtn = new JButton("Confirm Reschedule");
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmBtn.setBackground(PRIMARY);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setOpaque(true);
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        confirmBtn.addActionListener(ev -> {
            if (selectedDate[0] == null || selectedDate[0].isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select a date.");
                return;
            }
            // Validate not in the past
            try {
                String nowDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String nowTimeStr2 = new SimpleDateFormat("HH:mm").format(new Date());
                if (selectedDate[0].equals(nowDateStr)) {
                    // Same day — check time
                    if (selectedTime[0].compareTo(nowTimeStr2) <= 0) {
                        JOptionPane.showMessageDialog(dialog,
                                "Cannot reschedule to a time that has already passed today.",
                                "Invalid Time", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } else if (selectedDate[0].compareTo(nowDateStr) < 0) {
                    JOptionPane.showMessageDialog(dialog,
                            "Cannot reschedule to a past date.",
                            "Invalid Date", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (Exception ex) { /* proceed if validation fails */ }

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Reschedule appointment to:\n\n"
                            + "Date: " + selectedDate[0] + "\n"
                            + "Time: " + selectedTime[0] + "\n\n"
                            + "Proceed?",
                    "Confirm Reschedule", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                boolean ok = doctorService.rescheduleAppointment(
                        apptId, selectedDate[0], selectedTime[0]);
                JOptionPane.showMessageDialog(dialog,
                        ok ? "Appointment rescheduled successfully."
                                : "Unable to reschedule. The new slot may be unavailable.");
                if (ok) {
                    dialog.dispose();
                    refreshCurrentTab();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        confirmPanel.add(cancelDialogBtn);
        confirmPanel.add(confirmBtn);

        timePanel.add(confirmPanel, BorderLayout.SOUTH);

        // South panel: legend + time slots + confirm
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(legendBar, BorderLayout.NORTH);
        southPanel.add(timePanel, BorderLayout.CENTER);

        // ── CALENDAR PANEL ──
        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setBackground(Color.WHITE);

        String[] dayNames = {"MON", "TUE", "WED", "THU", "FRI"};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dispSdf = new SimpleDateFormat("dd MMM");

        final Calendar[] weekStart = {Calendar.getInstance()};
        weekStart[0].set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        final Runnable[] buildCalendar = {null};
        buildCalendar[0] = new Runnable() {
            @Override
            public void run() {
                calendarPanel.removeAll();

                // Dates for this week
                String[] dates = new String[5];
                String[] dispDates = new String[5];
                Calendar temp = (Calendar) weekStart[0].clone();
                for (int d = 0; d < 5; d++) {
                    dates[d] = sdf.format(temp.getTime());
                    dispDates[d] = dispSdf.format(temp.getTime());
                    temp.add(Calendar.DAY_OF_MONTH, 1);
                }

                weekLabel.setText(dispDates[0] + " — " + dispDates[4]);

                // Disable Prev Week if previous week's last day (Fri) is before today
                Calendar prevWeekCheck = (Calendar) weekStart[0].clone();
                prevWeekCheck.add(Calendar.DAY_OF_MONTH, -1);
                prevBtn.setEnabled(prevWeekCheck.getTime().after(new Date())
                        || sdf.format(prevWeekCheck.getTime()).equals(todayStr));

                // Day columns
                JPanel daysRow = new JPanel(new GridLayout(1, 5, 8, 0));
                daysRow.setBackground(Color.WHITE);
                daysRow.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

                for (int d = 0; d < 5; d++) {
                    final String date = dates[d];
                    boolean isPast = date.compareTo(todayStr) < 0;
                    boolean isToday = date.equals(todayStr);
                    boolean isCurrentDate = date.equals(selectedDate[0]);

                    final JPanel dayCard = new JPanel(new BorderLayout(0, 6));
                    dayCard.setBackground(Color.WHITE);

                    if (isPast) {
                        // Grey out past dates
                        dayCard.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
                    } else if (isCurrentDate) {
                        dayCard.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(PRIMARY, 2),
                                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
                    } else {
                        dayCard.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER, 1),
                                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
                    }

                    // Day header
                    JPanel dayHeader = new JPanel(new GridLayout(2, 1));
                    if (isPast) {
                        dayHeader.setBackground(new Color(200, 200, 200));
                    } else if (isToday) {
                        dayHeader.setBackground(SUCCESS);
                    } else if (isCurrentDate) {
                        dayHeader.setBackground(PRIMARY);
                    } else {
                        dayHeader.setBackground(PRIMARY_DARK);
                    }
                    dayHeader.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

                    JLabel dayNameLbl = new JLabel(dayNames[d], SwingConstants.CENTER);
                    dayNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    dayNameLbl.setForeground(Color.WHITE);

                    JLabel dateLbl = new JLabel(dispDates[d], SwingConstants.CENTER);
                    dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    if (isPast) {
                        dateLbl.setForeground(new Color(160, 160, 160));
                    } else if (isToday) {
                        dateLbl.setForeground(new Color(220, 255, 220));
                    } else {
                        dateLbl.setForeground(new Color(176, 190, 197));
                    }

                    dayHeader.add(dayNameLbl);
                    dayHeader.add(dateLbl);
                    dayCard.add(dayHeader, BorderLayout.NORTH);

                    // Date selector button
                    JButton selectBtn;
                    if (isPast) {
                        selectBtn = new JButton("Past");
                        selectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        selectBtn.setBackground(new Color(220, 220, 220));
                        selectBtn.setForeground(new Color(160, 160, 160));
                        selectBtn.setFocusPainted(false);
                        selectBtn.setBorderPainted(false);
                        selectBtn.setOpaque(true);
                        selectBtn.setEnabled(false);
                        selectBtn.setCursor(Cursor.getDefaultCursor());
                        selectBtn.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
                    } else if (isToday) {
                        selectBtn = new JButton(isCurrentDate ? "SELECTED" : "Today");
                        selectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        selectBtn.setBackground(isCurrentDate ? SUCCESS : new Color(232, 245, 233));
                        selectBtn.setForeground(isCurrentDate ? Color.WHITE : SUCCESS);
                        selectBtn.setFocusPainted(false);
                        selectBtn.setBorderPainted(false);
                        selectBtn.setOpaque(true);
                        selectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        selectBtn.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
                    } else {
                        selectBtn = new JButton(isCurrentDate ? "SELECTED" : "Select Date");
                        selectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        selectBtn.setBackground(isCurrentDate ? SUCCESS : new Color(224, 224, 224));
                        selectBtn.setForeground(Color.WHITE);
                        selectBtn.setFocusPainted(false);
                        selectBtn.setBorderPainted(false);
                        selectBtn.setOpaque(true);
                        selectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        selectBtn.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
                    }

                    if (!isPast) {
                        selectBtn.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                if (!date.equals(selectedDate[0])) {
                                    selectBtn.setBackground(PRIMARY_LIGHT);
                                }
                            }
                            @Override
                            public void mouseExited(MouseEvent e) {
                                if (!date.equals(selectedDate[0])) {
                                    String nowDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                                    if (date.equals(nowDate)) {
                                        selectBtn.setBackground(new Color(232, 245, 233));
                                    } else {
                                        selectBtn.setBackground(new Color(224, 224, 224));
                                    }
                                }
                            }
                        });

                        selectBtn.addActionListener(ev -> {
                            selectedDate[0] = date;
                            buildCalendar[0].run();
                            // Update time slot availability
                            updateSlotAvailability(date, allSlotBtns, timeSlots);
                        });
                    }

                    dayCard.add(selectBtn, BorderLayout.CENTER);
                    daysRow.add(dayCard);
                }

                calendarPanel.add(daysRow, BorderLayout.CENTER);
                calendarPanel.revalidate();
                calendarPanel.repaint();
            }
        };

        prevBtn.addActionListener(e -> {
            weekStart[0].add(Calendar.DAY_OF_MONTH, -7);
            buildCalendar[0].run();
        });
        nextBtn.addActionListener(e -> {
            weekStart[0].add(Calendar.DAY_OF_MONTH, 7);
            buildCalendar[0].run();
        });

        buildCalendar[0].run();
        // Initial slot availability check
        updateSlotAvailability(selectedDate[0], allSlotBtns, timeSlots);

        dialog.add(calendarPanel, BorderLayout.CENTER);
        dialog.add(southPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateSlotAvailability(String date, JToggleButton[] allSlotBtns, String[] timeSlots) {
        String nowDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String nowTimeStr = new SimpleDateFormat("HH:mm").format(new Date());
        boolean isToday = date.equals(nowDateStr);

        try {
            List<String> available = doctorService.getAvailableSlots(doctorId, date);
            for (int i = 0; i < allSlotBtns.length; i++) {
                if (allSlotBtns[i] != null) {
                    String slotTime = timeSlots[i];
                    boolean isBooked = !available.contains(slotTime);
                    boolean isPastSlot = isToday && slotTime.compareTo(nowTimeStr) <= 0;
                    boolean isDisabled = isBooked || isPastSlot;

                    allSlotBtns[i].setEnabled(!isDisabled);
                    if (isDisabled) {
                        allSlotBtns[i].setBackground(new Color(240, 240, 240));
                        allSlotBtns[i].setForeground(new Color(180, 180, 180));
                        allSlotBtns[i].setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                        allSlotBtns[i].setToolTipText(isPastSlot ? "Time has passed" : "Slot unavailable");
                    } else {
                        allSlotBtns[i].setBackground(Color.WHITE);
                        allSlotBtns[i].setForeground(TEXT_PRIMARY);
                        allSlotBtns[i].setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER, 1),
                                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                        allSlotBtns[i].setToolTipText(null);
                    }
                }
            }
        } catch (Exception ex) {
            // If we can't check availability, just grey out past slots
            for (int i = 0; i < allSlotBtns.length; i++) {
                if (allSlotBtns[i] != null) {
                    String slotTime = timeSlots[i];
                    boolean isPastSlot = isToday && slotTime.compareTo(nowTimeStr) <= 0;
                    allSlotBtns[i].setEnabled(!isPastSlot);
                    if (isPastSlot) {
                        allSlotBtns[i].setBackground(new Color(240, 240, 240));
                        allSlotBtns[i].setForeground(new Color(180, 180, 180));
                        allSlotBtns[i].setToolTipText("Time has passed");
                    }
                }
            }
        }
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
