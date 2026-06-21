package brigthcare_medical_centre;

import javax.swing.JOptionPane;
import brigthcare_medical_centre.gui.admin.AdminLoginFrame;
import brigthcare_medical_centre.server.RmiServer;

public class BrigthCare_Medical_Centre {

    public static void main(String[] args) {
        String[] options = {"Start Server", "Launch Admin Client"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select mode:",
                "BrightCare Medical Centre",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            System.out.println("Starting BrightCare Medical Centre Server...");
            RmiServer server = new RmiServer();
            server.start();
        } else if (choice == 1) {
            java.awt.EventQueue.invokeLater(() -> new AdminLoginFrame().setVisible(true));
        }
    }
}
