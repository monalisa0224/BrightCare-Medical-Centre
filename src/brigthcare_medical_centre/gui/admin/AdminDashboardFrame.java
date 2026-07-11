package brigthcare_medical_centre.gui.admin;

import java.awt.BorderLayout;
import javax.swing.*;
import brigthcare_medical_centre.auth.User;
import brigthcare_medical_centre.common.AdminInterface;
import brigthcare_medical_centre.common.ReportInterface;

public class AdminDashboardFrame extends JFrame {

    public AdminDashboardFrame(User admin, AdminInterface adminService, ReportInterface reportService) {
        setTitle("BrightCare Medical Centre - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        ReportPanel reportPanel = new ReportPanel(admin, reportService);
        tabbedPane.addTab("Reports", reportPanel);

        LogViewerPanel logViewerPanel = new LogViewerPanel(adminService);
        tabbedPane.addTab("Audit Logs", logViewerPanel);

        UserManagementPanel userManagementPanel = new UserManagementPanel(adminService);
        tabbedPane.addTab("User Management", userManagementPanel);

        add(tabbedPane, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel("Logged in as: " + admin.getUsername() + " (Admin)");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }
}
