package view;

import controller.ExportController;
import domain.SessionContext;
import domain.User;

import javax.swing.*;
import java.util.Map;

public class ExportView {
    private final ExportController exportController;

    public ExportView(ExportController exportController) {
        this.exportController = exportController;
    }

    public void displayExportMenu() {
        User currentUser = SessionContext.getCurrentUser();
        
        if (currentUser == null) {
            JOptionPane.showMessageDialog(null,
                    "Error: No user session found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Only ADMIN users can access export functionality
        if (currentUser.getRole() != User.Role.ADMIN) {
            JOptionPane.showMessageDialog(null,
                    "Access denied. Only ADMIN users can export data.",
                    "Unauthorized",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        while (true) {
            String menu = """
                    ╔═══════════════════════════════════════╗
                    ║         EXPORT DATA - ADMIN           ║
                    ╠═══════════════════════════════════════╣
                    ║  1. Export Books Catalog to CSV       ║
                    ║  2. Export Overdue Loans to CSV       ║
                    ║  3. Return to Main Menu               ║
                    ╚═══════════════════════════════════════╝
                    
                    Select an option:""";

            String input = JOptionPane.showInputDialog(null, menu, "Export Menu", JOptionPane.PLAIN_MESSAGE);

            if (input == null || input.trim().isEmpty()) {
                return; // User cancelled
            }

            switch (input.trim()) {
                case "1" -> exportCatalogView();
                case "2" -> exportOverdueLoansView();
                case "3" -> {
                    return;
                }
                default -> JOptionPane.showMessageDialog(null,
                        "Invalid option. Please select 1-3.",
                        "Invalid Option",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void exportCatalogView() {
        User currentUser = SessionContext.getCurrentUser();
        
        String defaultFilename = "libros_export.csv";
        String filename = JOptionPane.showInputDialog(null,
                "Enter filename for books catalog export:",
                defaultFilename);

        if (filename == null || filename.trim().isEmpty()) {
            return; // User cancelled
        }

        filename = filename.trim();

        Map<String, String> result = exportController.exportBooksToCSV(filename, currentUser.getRole());

        if ("200".equals(result.get("status"))) {
            String message = """
                    ✓ Books catalog exported successfully!
                    
                    File: %s
                    
                    The file has been saved in the project directory.
                    """.formatted(result.get("filePath"));

            JOptionPane.showMessageDialog(null,
                    message,
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error exporting catalog:\n" + result.get("message"),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportOverdueLoansView() {
        User currentUser = SessionContext.getCurrentUser();
        
        String defaultFilename = "prestamos_vencidos.csv";
        String filename = JOptionPane.showInputDialog(null,
                "Enter filename for overdue loans export:",
                defaultFilename);

        if (filename == null || filename.trim().isEmpty()) {
            return; // User cancelled
        }

        filename = filename.trim();

        Map<String, String> result = exportController.exportOverdueLoansToCSV(filename, currentUser.getRole());

        if ("200".equals(result.get("status"))) {
            String message = """
                    ✓ Overdue loans exported successfully!
                    
                    File: %s
                    
                    The file has been saved in the project directory.
                    """.formatted(result.get("filePath"));

            JOptionPane.showMessageDialog(null,
                    message,
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Error exporting overdue loans:\n" + result.get("message"),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
