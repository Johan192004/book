package view;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import controller.LoanController;
import domain.SessionContext;
import domain.User;

import java.util.HashMap;

public class LoanView {
    private final LoanController loanController;

    public LoanView(LoanController loanController) {
        this.loanController = loanController;
    }

    public void displayMenu() {
        if (SessionContext.getCurrentUser() == null) {
            System.out.println("No user is currently logged in.");
            return;
        } else if (SessionContext.getCurrentUser().getRole() == User.Role.ADMIN) {
            displayLoanAdmin();
        } else if (SessionContext.getCurrentUser().getRole() == User.Role.ASSISTANT) {
            displayLoanAssistant();
        } else {
            System.out.println("Unknown role. Access denied.");
        }
    }

    private void displayLoanAdmin() {
        String option = "";
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(null,
                    "Loan Management - Admin\n" +
                            "1. Register Loan\n" +
                            "2. Mark Return\n" +
                            "3. Delete Loan\n" +
                            "4. View All Loans\n" +
                            "5. View Loan by ID\n" +
                            "6. View Loans by Member\n" +
                            "7. View Loans by Book\n" +
                            "8. View Loans by Status\n" +
                            "9. Exit\n" +
                            "Select an option:");
            if (option == null) {
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        registerLoanView();
                        break;
                    case "2":
                        markReturnView();
                        break;
                    case "3":
                        deleteLoanView();
                        break;
                    case "4":
                        viewAllLoansView();
                        break;
                    case "5":
                        viewLoanByIdView();
                        break;
                    case "6":
                        viewLoansByMemberView();
                        break;
                    case "7":
                        viewLoansByBookView();
                        break;
                    case "8":
                        viewLoansByStatusView();
                        break;
                    case "9":
                        exit = true;
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
                }
            }
        } while (!exit);
    }

    private void displayLoanAssistant() {
        String option = "";
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(null,
                    "Loan Management - Assistant\n" +
                            "1. Register Loan\n" +
                            "2. Mark Return\n" +
                            "3. View All Loans\n" +
                            "4. View Loan by ID\n" +
                            "5. View Loans by Member\n" +
                            "6. View Loans by Book\n" +
                            "7. View Loans by Status\n" +
                            "8. Exit\n" +
                            "Select an option:");
            if (option == null) {
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        registerLoanView();
                        break;
                    case "2":
                        markReturnView();
                        break;
                    case "3":
                        viewAllLoansView();
                        break;
                    case "4":
                        viewLoanByIdView();
                        break;
                    case "5":
                        viewLoansByMemberView();
                        break;
                    case "6":
                        viewLoansByBookView();
                        break;
                    case "7":
                        viewLoansByStatusView();
                        break;
                    case "8":
                        exit = true;
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
                }
            }
        } while (!exit);
    }

    /**
     * Register a new loan
     */
    private void registerLoanView() {
        String memberId = getRequiredInput("Enter Member ID:", "Register Loan");
        if (memberId == null) {
            JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String isbn = getRequiredInput("Enter Book ISBN:", "Register Loan");
        if (isbn == null) {
            JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User.Role userRole = SessionContext.getCurrentUser().getRole();
        HashMap<String, String> response = loanController.registerLoan(memberId, isbn, userRole);

        String status = response.get("status");
        String message = response.get("message");

        if ("201".equals(status)) {
            String details = "Loan registered successfully!\n\n" +
                    "Loan ID: " + response.get("loanId") + "\n" +
                    "Due Date: " + response.get("dueDate");
            showScrollableMessage("Success", details);
        } else {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mark a loan as returned
     */
    private void markReturnView() {
        String loanId = getRequiredInput("Enter Loan ID to mark as returned:", "Mark Return");
        if (loanId == null) {
            JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User.Role userRole = SessionContext.getCurrentUser().getRole();
        HashMap<String, String> response = loanController.markReturn(loanId, userRole);

        String status = response.get("status");
        String message = response.get("message");

        if ("200".equals(status)) {
            String details = "Loan marked as returned!\n\n" +
                    "Loan ID: " + response.get("loanId") + "\n" +
                    "Return Date: " + response.get("returnDate") + "\n" +
                    "Fine Amount: $" + response.get("fineAmount");
            
            if (response.get("warning") != null) {
                details += "\n\n⚠️ " + response.get("warning");
            }
            
            showScrollableMessage("Success", details);
        } else {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete a loan (ADMIN only)
     */
    private void deleteLoanView() {
        String loanId = getRequiredInput("Enter Loan ID to delete:", "Delete Loan");
        if (loanId == null) {
            JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete this loan?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            User.Role userRole = SessionContext.getCurrentUser().getRole();
            HashMap<String, String> response = loanController.deleteLoan(loanId, userRole);

            String status = response.get("status");
            String message = response.get("message");

            if ("200".equals(status)) {
                JOptionPane.showMessageDialog(null, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * View all loans
     */
    private void viewAllLoansView() {
        User.Role userRole = SessionContext.getCurrentUser().getRole();
        HashMap<String, String> response = loanController.getAllLoans(userRole);

        String status = response.get("status");
        String message = response.get("message");

        if ("200".equals(status)) {
            String data = response.get("data");
            showScrollableMessage("All Loans", data);
        } else {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * View loan by ID
     */
    private void viewLoanByIdView() {
        String loanId = getRequiredInput("Enter Loan ID:", "View Loan");
        if (loanId == null) {
            JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User.Role userRole = SessionContext.getCurrentUser().getRole();
        HashMap<String, String> response = loanController.findLoanById(loanId, userRole);

        String status = response.get("status");
        String message = response.get("message");

        if ("200".equals(status)) {
            String data = response.get("data");
            showScrollableMessage("Loan Details", data);
        } else {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * View loans by member ID
     */
    private void viewLoansByMemberView() {
        String memberId = getRequiredInput("Enter Member ID:", "View Loans by Member");
        if (memberId == null) {
            JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User.Role userRole = SessionContext.getCurrentUser().getRole();
        HashMap<String, String> response = loanController.findLoansByMemberId(memberId, userRole);

        String status = response.get("status");
        String message = response.get("message");

        if ("200".equals(status)) {
            String data = response.get("data");
            showScrollableMessage("Loans by Member", data);
        } else {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * View loans by book ISBN
     */
    private void viewLoansByBookView() {
        String isbn = getRequiredInput("Enter Book ISBN:", "View Loans by Book");
        if (isbn == null) {
            JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User.Role userRole = SessionContext.getCurrentUser().getRole();
        HashMap<String, String> response = loanController.findLoansByIsbn(isbn, userRole);

        String status = response.get("status");
        String message = response.get("message");

        if ("200".equals(status)) {
            String data = response.get("data");
            showScrollableMessage("Loans by Book", data);
        } else {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * View loans by status
     */
    private void viewLoansByStatusView() {
        String[] options = {"BORROWED", "RETURNED", "OVERDUE"};
        String status = (String) JOptionPane.showInputDialog(null,
                "Select Loan Status:",
                "Filter by Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (status == null) return;

        User.Role userRole = SessionContext.getCurrentUser().getRole();
        HashMap<String, String> response = loanController.findLoansByStatus(status, userRole);

        String responseStatus = response.get("status");
        String message = response.get("message");

        if ("200".equals(responseStatus)) {
            String data = response.get("data");
            showScrollableMessage("Loans by Status: " + status, data);
        } else {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show a scrollable message dialog
     */
    private void showScrollableMessage(String title, String message) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(800, 400));
        JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Helper method to get required input from user
     * Returns null if user cancels, otherwise keeps asking until valid input is provided
     */
    private String getRequiredInput(String message, String title) {
        String input = null;
        boolean validInput = false;

        while (!validInput) {
            input = JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
            
            // User cancelled (clicked X or Cancel button)
            if (input == null) {
                return null;
            }
            
            // Check if input is empty or only whitespace
            if (input.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                    null,
                    "This field cannot be empty. Please enter a valid value or cancel.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
                );
            } else {
                validInput = true;
            }
        }

        return input.trim();
    }
}
