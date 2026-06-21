package brigthcare_medical_centre.gui.admin;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import brigthcare_medical_centre.common.AdminInterface;

public class LogViewerPanel extends JPanel {

    private final AdminInterface adminService;
    private JTable logTable;
    private DefaultTableModel tableModel;

    public LogViewerPanel(AdminInterface adminService) {
        this.adminService = adminService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Log ID", "User ID", "Username", "Action", "Timestamp", "Details"};
        tableModel = new DefaultTableModel(columns, 0);
        logTable = new JTable(tableModel);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        add(new JScrollPane(logTable), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadLogs());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadLogs();
    }

    private void loadLogs() {
        try {
            List<String[]> logs = adminService.viewLogs();
            tableModel.setRowCount(0);
            for (String[] row : logs) {
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading logs: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
