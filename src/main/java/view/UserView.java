package view;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import controller.UserController;
import domain.SessionContext;
import domain.User;

import java.util.HashMap;

public class UserView {
    private final UserController userController;

    public UserView(UserController userController) {
        this.userController = userController;
    }

    public void displayMenu() {
        if(SessionContext.getCurrentUser() == null) {
            System.out.println("No user is currently logged in.");
            return;
        } else if (SessionContext.getCurrentUser().getRole() == User.Role.ADMIN) {
            displayUserAdmin();
        } else {
            JOptionPane.showMessageDialog(null, "Only ADMIN users can manage users.", "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayUserAdmin() {
        String option = "";
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(null,
                    "User Management - Admin\n" +
                            "1. Add User\n" +
                            "2. Update User\n" +
                            "3. Delete User\n" +
                            "4. View All Users\n" +
                            "5. Search User by ID\n" +
                            "6. Search User by Username\n" +
                            "7. Exit\n" +
                            "Select an option:");
            if (option == null) {
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        addUserView();
                        break;
                    case "2":
                        updateUserView();
                        break;
                    case "3":
                        deleteUserView();
                        break;
                    case "4":
                        viewAllUsersView();
                        break;
                    case "5":
                        searchUserByIdView();
                        break;
                    case "6":
                        searchUserByUsernameView();
                        break;
                    case "7":
                        exit = true;
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
                }
            }
        } while (!exit);
    }

    private void addUserView() {
        try {
            String name = getRequiredInput("Enter Name:");
            String username = getRequiredInput("Enter Username:");
            String password = getRequiredInput("Enter Password (min 6 characters):");

            // Information message about default values
            JOptionPane.showMessageDialog(null, 
                "New users are created with:\n" +
                "- Role: ASSISTANT\n" +
                "- Status: ACTIVE\n" +
                "- Created Date: Today",
                "Default Values", 
                JOptionPane.INFORMATION_MESSAGE);

            HashMap<String, String> response = userController.createUser(
                    name, username, password,
                    SessionContext.getCurrentUser().getRole()
            );

            if (response.get("status").equals("201")) {
                JOptionPane.showMessageDialog(null, response.get("message"));
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUserView() {
        try {
            String idStr = getRequiredInput("Enter User ID to update:");
            int id = Integer.parseInt(idStr);
            
            String name = getRequiredInput("Enter new Name:");
            String username = getRequiredInput("Enter new Username:");
            String password = getRequiredInput("Enter new Password (min 6 characters):");
            
            // Select role
            String role = selectRole();
            if (role == null) {
                return; // User cancelled
            }
            
            String isActive = getRequiredInput("Is Active? (true/false):");

            HashMap<String, String> response = userController.updateUser(
                    id, name, username, password, role, isActive,
                    SessionContext.getCurrentUser().getRole()
            );

            if (response.get("status").equals("200")) {
                JOptionPane.showMessageDialog(null, response.get("message"));
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid ID format. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUserView() {
        try {
            String idStr = getRequiredInput("Enter User ID to delete:");
            int id = Integer.parseInt(idStr);

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this user?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                HashMap<String, String> response = userController.deleteUser(
                        id, SessionContext.getCurrentUser().getRole()
                );

                if (response.get("status").equals("200")) {
                    JOptionPane.showMessageDialog(null, response.get("message"));
                } else {
                    JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid ID format. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAllUsersView() {
        try {
            HashMap<String, String> response = userController.getAllUsers(
                    SessionContext.getCurrentUser().getRole()
            );

            if (response.get("status").equals("200")) {
                // Display table in scrollable text area
                JTextArea textArea = new JTextArea(response.get("message"));
                textArea.setEditable(false);
                textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new java.awt.Dimension(800, 400));

                JOptionPane.showMessageDialog(null, scrollPane, "All Users", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchUserByIdView() {
        try {
            String idStr = getRequiredInput("Enter User ID to search:");
            int id = Integer.parseInt(idStr);

            HashMap<String, String> response = userController.findUserById(
                    id, SessionContext.getCurrentUser().getRole()
            );

            if (response.get("status").equals("200")) {
                // Display user details in scrollable text area
                JTextArea textArea = new JTextArea(response.get("message"));
                textArea.setEditable(false);
                textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));

                JOptionPane.showMessageDialog(null, scrollPane, "User Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid ID format. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchUserByUsernameView() {
        try {
            String username = getRequiredInput("Enter Username to search:");

            HashMap<String, String> response = userController.findUserByUsername(
                    username, SessionContext.getCurrentUser().getRole()
            );

            if (response.get("status").equals("200")) {
                // Display user details in scrollable text area
                JTextArea textArea = new JTextArea(response.get("message"));
                textArea.setEditable(false);
                textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));

                JOptionPane.showMessageDialog(null, scrollPane, "User Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Helper method to select role from numbered menu
     * @return Role name or null if cancelled
     */
    private String selectRole() {
        String option = JOptionPane.showInputDialog(null,
                "Select Role:\n" +
                        "1. ADMIN\n" +
                        "2. ASSISTANT\n" +
                        "Enter option (1-2):");
        
        if (option == null) {
            return null; // User cancelled
        }
        
        switch (option.trim()) {
            case "1":
                return "ADMIN";
            case "2":
                return "ASSISTANT";
            default:
                JOptionPane.showMessageDialog(null, "Invalid option. Please select 1 or 2.", "Error", JOptionPane.ERROR_MESSAGE);
                return selectRole(); // Recursive call for retry
        }
    }

    /**
     * Helper method to get required input from user with validation loop
     */
    private String getRequiredInput(String message) {
        String input = null;
        while (input == null || input.trim().isEmpty()) {
            input = JOptionPane.showInputDialog(null, message);
            if (input == null) {
                throw new RuntimeException("Operation cancelled by user");
            }
            if (input.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "This field is required. Please enter a value.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return input.trim();
    }
}
