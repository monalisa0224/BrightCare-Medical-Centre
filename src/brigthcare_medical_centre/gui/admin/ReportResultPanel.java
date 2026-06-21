package brigthcare_medical_centre.gui.admin;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import brigthcare_medical_centre.common.ReportInterface;
import brigthcare_medical_centre.report.Report;

public class ReportResultPanel extends JPanel {

    private JTable reportTable;
    private DefaultTableModel tableModel;

    public ReportResultPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Report ID", "Type", "Generated Date", "Parameters"};
        tableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(tableModel);
        add(new JScrollPane(reportTable), BorderLayout.CENTER);
    }

    public void loadReports(List<Report> reports) {
        tableModel.setRowCount(0);
        for (Report report : reports) {
            tableModel.addRow(new Object[]{
                    report.getReportID(),
                    report.getReportType().name(),
                    report.getGeneratedDate(),
                    report.getParameters()
            });
        }
    }
}
