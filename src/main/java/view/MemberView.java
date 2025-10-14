package view;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import controller.MemberController;
import domain.SessionContext;
import domain.User;
import domain.User.Role;

import java.util.HashMap;

public class MemberView {
    private final MemberController memberController;

    public MemberView(MemberController memberController) {
        this.memberController = memberController;
    }

    public void displayMenu() {
        if(SessionContext.getCurrentUser() == null) {
            System.out.println("No user is currently logged in.");
            return;
        } else if (SessionContext.getCurrentUser().getRole() == User.Role.ADMIN) {
            displayMemberAdmin();
        } else if (SessionContext.getCurrentUser().getRole() == User.Role.ASSISTANT) {
            displayMemberAssistant();
        } else {
            System.out.println("Unknown role. Access denied.");
        } 
    }

    private void displayMemberAdmin() {
        String option = "";
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(null,
                    "Member Management - Admin\n" +
                            "1. Add Member\n" +
                            "2. Update Member\n" +
                            "3. Delete Member\n" +
                            "4. View Members\n" +
                            "5. Search Member\n" +
                            "6. Exit\n" +
                            "Select an option:");
            if (option == null) {
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        addMemberView();
                        break;
                    case "2":
                        updateMemberView();
                        break;
                    case "3":
                        deleteMemberView();
                        break;
                    case "4":
                        viewAllMembersView();
                        break;
                    case "5":
                        searchMemberView();
                        break;
                    case "6":
                        exit = true;
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
                }
            }
        } while (!exit);
    }

    private void displayMemberAssistant() {
        String option = "";
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(null,
                    "Member Management - Assistant\n" +
                            "1. Add Member\n" +
                            "2. Update Member\n" +
                            "3. View Members\n" +
                            "4. Search Member\n" +
                            "5. Exit\n" +
                            "Select an option:");
            if (option == null) {
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        addMemberView();
                        break;
                    case "2":
                        updateMemberView();
                        break;
                    case "3":
                        viewAllMembersView();
                        break;
                    case "4":
                        searchMemberView();
                        break;
                    case "5":
                        exit = true;
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
                }
            }
        } while (!exit);
    }

    /**
     * Add Member View - Collects member data and creates a new member
     */
    private void addMemberView() {
        try {
            // Get member name
            String name = getRequiredInput("Enter member name:", "Add Member");
            if (name == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Get member email
            String email = getRequiredInput("Enter member email:", "Add Member");
            if (email == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Get member phone
            String phone = getRequiredInput("Enter member phone:", "Add Member");
            if (phone == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Call controller to create member
            Role currentUserRole = SessionContext.getCurrentUser().getRole();
            HashMap<String, String> response = memberController.createMember(name, email, phone, currentUserRole);

            // Handle response
            String status = response.get("status");
            String message = response.get("message");

            if (status.equals("201")) {
                // Success - Created
                String memberInfo = String.format(
                    "Member created successfully!\n\n" +
                    "ID: %s\n" +
                    "Name: %s\n" +
                    "Email: %s\n" +
                    "Phone: %s",
                    response.get("id"),
                    response.get("name"),
                    response.get("email"),
                    response.get("phone")
                );
                JOptionPane.showMessageDialog(null, memberInfo, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Any error
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Update Member View - Updates an existing member
     */
    private void updateMemberView() {
        try {
            // Get member ID
            String idStr = getRequiredInput("Enter member ID to update:", "Update Member");
            if (idStr == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int memberId;
            try {
                memberId = Integer.parseInt(idStr);
                if (memberId <= 0) {
                    JOptionPane.showMessageDialog(null, "Member ID must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid ID. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get new member name
            String name = getRequiredInput("Enter new member name:", "Update Member");
            if (name == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Get new member email
            String email = getRequiredInput("Enter new member email:", "Update Member");
            if (email == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Get new member phone
            String phone = getRequiredInput("Enter new member phone:", "Update Member");
            if (phone == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Call controller to update member
            User.Role currentUserRole = SessionContext.getCurrentUser().getRole();
            HashMap<String, String> response = memberController.updateMember(memberId, name, email, phone, currentUserRole);

            // Handle response
            String status = response.get("status");
            String message = response.get("message");

            if (status.equals("200")) {
                // Success - OK
                String memberInfo = String.format(
                    "Member updated successfully!\n\n" +
                    "ID: %s\n" +
                    "Name: %s\n" +
                    "Email: %s\n" +
                    "Phone: %s",
                    response.get("id"),
                    response.get("name"),
                    response.get("email"),
                    response.get("phone")
                );
                JOptionPane.showMessageDialog(null, memberInfo, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Any error
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete Member View - Deletes a member (ADMIN only)
     */
    private void deleteMemberView() {
        try {
            // Get member ID
            String idStr = getRequiredInput("Enter member ID to delete:", "Delete Member");
            if (idStr == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int memberId;
            try {
                memberId = Integer.parseInt(idStr);
                if (memberId <= 0) {
                    JOptionPane.showMessageDialog(null, "Member ID must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid ID. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete member with ID " + memberId + "?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, "Deletion cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Call controller to delete member
            User.Role currentUserRole = SessionContext.getCurrentUser().getRole();
            HashMap<String, String> response = memberController.deleteMember(memberId, currentUserRole);

            // Handle response
            String status = response.get("status");
            String message = response.get("message");

            if (status.equals("200")) {
                // Success - OK (usando 200 en lugar de 204 por compatibilidad con la implementación actual)
                JOptionPane.showMessageDialog(null, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Any error
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * View All Members - To be implemented
     */
    private void viewAllMembersView() {
        try {
            User.Role currentUserRole = SessionContext.getCurrentUser().getRole();
            HashMap<String, String> response = memberController.getAllMembers(currentUserRole);

            String status = response.get("status");
            String message = response.get("message");

            if (status.equals("200")) {
                // Success - message contains the formatted table
                // Create a text area for better display with monospaced font
                JTextArea textArea = new JTextArea(message);
                textArea.setEditable(false);
                textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
                
                // Add scroll pane
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new java.awt.Dimension(900, 400));
                
                JOptionPane.showMessageDialog(null, scrollPane, "All Members", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Any error
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Search Member - To be implemented
     */
    private void searchMemberView() {
        try {
            // Get member ID
            String idStr = getRequiredInput("Enter member ID to search:", "Search Member");
            if (idStr == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int memberId;
            try {
                memberId = Integer.parseInt(idStr);
                if (memberId <= 0) {
                    JOptionPane.showMessageDialog(null, "Member ID must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid ID. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User.Role currentUserRole = SessionContext.getCurrentUser().getRole();
            HashMap<String, String> response = memberController.findMemberById(memberId, currentUserRole);

            String status = response.get("status");
            String message = response.get("message");

            if (status.equals("200")) {
                // Success - message contains the formatted member details
                JOptionPane.showMessageDialog(null, message, "Member Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Any error
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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