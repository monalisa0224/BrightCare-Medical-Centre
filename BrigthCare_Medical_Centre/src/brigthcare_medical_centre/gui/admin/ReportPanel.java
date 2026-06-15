package brigthcare_medical_centre.gui.admin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.common.ReportInterface;
import brigthcare_medical_centre.report.ReportType;

public class ReportPanel extends JPanel {

    private final User admin;
    private final ReportInterface reportService;
    private JComboBox<String> reportTypeCombo;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    public ReportPanel(User admin, ReportInterface reportService) {
        this.admin = admin;
        this.reportService = reportService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controlPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Report Parameters"));

        controlPanel.add(new JLabel("Report Type:"));
        reportTypeCombo = new JComboBox<>(new String[]{"Monthly Appointments", "Doctor Consultations", "Patient Visits"});
        controlPanel.add(reportTypeCombo);

        controlPanel.add(new JLabel("Start Date (yyyy-MM-dd):"));
        startDateField = new JTextField();
        controlPanel.add(startDateField);

        controlPanel.add(new JLabel("End Date (yyyy-MM-dd):"));
        endDateField = new JTextField();
        controlPanel.add(endDateField);

        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(generateButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Data"};
        tableModel = new DefaultTableModel(columns, 0);
        resultTable = new JTable(tableModel);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);
    }

    private void generateReport() {
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter start and end dates.");
            return;
        }

        try {
            int selectedIndex = reportTypeCombo.getSelectedIndex();
            ReportType type;
            String[] columns;

            switch (selectedIndex) {
                case 0:
                    type = ReportType.MONTHLY_APPOINTMENTS;
                    columns = new String[]{"Appointment ID", "Patient", "Doctor", "Date/Time", "Status"};
                    break;
                case 1:
                    type = ReportType.DOCTOR_CONSULTATIONS;
                    columns = new String[]{"Doctor ID", "Doctor Name", "Total Consultations"};
                    break;
                case 2:
                    type = ReportType.PATIENT_VISITS;
                    columns = new String[]{"Patient ID", "Patient Name", "Total Visits", "First Visit", "Last Visit"};
                    break;
                default:
                    return;
            }

            List<String[]> data = reportService.getReportData(type, startDate, endDate);
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            for (String[] row : data) {
                tableModel.addRow(row);
            }

            reportService.generateReport(admin.getUserID(), type, startDate, endDate);
            JOptionPane.showMessageDialog(this, "Report generated successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
