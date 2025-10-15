package view;

import java.util.HashMap;

import javax.swing.JOptionPane;

import controller.AuthController;
import domain.SessionContext;
import domain.User;

public class MainView {
    private final AuthController authController;
    private final MemberView memberView;
    private final CatalogView catalogView;
    private final UserView userView;
    private final LoanView loanView;
    private final ExportView exportView;

    public MainView(AuthController authController, MemberView memberView, CatalogView catalogView, UserView userView, LoanView loanView, ExportView exportView) {
        this.authController = authController;
        this.memberView = memberView;
        this.catalogView = catalogView;
        this.userView = userView;
        this.loanView = loanView;
        this.exportView = exportView;
    }

    public void showMenu(){
        String option;
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(
                """
                Library Management System
                1. Login
                2. Exit
                        """
            );
            if(option == null){
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        displayLogin();
                    case "2":
                        exit = true;
                        break;
                    
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
                }
            }
        } while (!exit);
    }

    public void displayLogin() {
        String userName;
        boolean passUser = false;
        do {
            userName = JOptionPane.showInputDialog("Enter your username:");
            if (userName == null) {
                return; // User cancelled
            }
            if(userName.length() >= 3){
                passUser = true;
            } else {
                JOptionPane.showMessageDialog(null, "Username must be at least 3 characters long. Please try again.");
            }
        } while (!passUser);

        String password;
        boolean passPassword = false;
        do {
            password = JOptionPane.showInputDialog("Enter your password:");
            if (password == null) {
                return; // User cancelled
            }
            if (password.length() >= 6) {
                passPassword = true;
            } else {
                JOptionPane.showMessageDialog(null, "Password must be at least 6 characters long. Please try again.");
            }
        } while (!passPassword);

        HashMap<String, String> infoReceived = authController.login(userName, password);

        if(infoReceived.get("status").equals("200")) {
            JOptionPane.showMessageDialog(null, "Login successful! Welcome, " + infoReceived.get("name"));
            SessionContext.setCurrentUser(new User(
                    infoReceived.get("id") != null ? Integer.parseInt(infoReceived.get("id")) : null,
                    infoReceived.get("name"),
                    infoReceived.get("userName"),
                    infoReceived.get("role") != null ? User.Role.valueOf(infoReceived.get("role")) : null
            ));
            if (SessionContext.getCurrentUser().getRole() == User.Role.ADMIN) {
                displayAdminMenu();
            } else if (SessionContext.getCurrentUser().getRole() == User.Role.ASSISTANT) {
                displayAssistantMenu();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Login failed: " + infoReceived.get("message"));
        }

        // if(SessionContext.getCurrentUser() != null) {
        //     // Proceed to the next view based on role
        //     if (SessionContext.getCurrentUser().getRole() == User.Role.ADMIN) {
        //         JOptionPane.showMessageDialog(null, "Admin Dashboard - To be implemented");
        //     } else if (SessionContext.getCurrentUser().getRole() == User.Role.ASSISTANT) {
        //         JOptionPane.showMessageDialog(null, "Assistant Dashboard - To be implemented");
        //     }
        // }


    }

    public void displayAdminMenu() {
        String option;
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(
                """
                Admin Menu
                1. Manage Members
                2. Manage Books
                3. Manage Users
                4. Manage Loans
                5. Export Data
                6. Logout
                        """
            );
            if(option == null){
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        // Call Member Management View
                        memberView.displayMenu();
                        break;
                    case "2":
                        // Call Book Management View
                        catalogView.displayMenu();
                        break;
                    case "3":
                        // Call User Management View
                        userView.displayMenu();
                        break;
                    case "4":
                        // Call Loan Management View
                        loanView.displayMenu();
                        break;
                    case "5":
                        // Call Export View
                        exportView.displayExportMenu();
                        break;
                    case "6":
                        SessionContext.setCurrentUser(null);
                        JOptionPane.showMessageDialog(null, "Logged out successfully.");
                        exit = true;
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
                }
            }
        } while (!exit);
    }

    public void displayAssistantMenu() {
        String option;
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(
                """
                Assistant Menu
                1. Manage Members
                2. Manage Books
                3. Manage Loans
                4. Logout
                        """
            );
            if(option == null){
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        // Call Member Management View
                        memberView.displayMenu();
                        break;
                    case "2":
                        // Call Book Management View
                        catalogView.displayMenu();
                        break;
                    case "3":
                        // Call Loan Management View
                        loanView.displayMenu();
                        break;
                    case "4":
                        SessionContext.setCurrentUser(null);
                        JOptionPane.showMessageDialog(null, "Logged out successfully.");
                        exit = true;
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid option. Please try again.");
                }
            }
        } while (!exit);
    }
}
