package view;

import java.util.HashMap;

import javax.swing.JOptionPane;

import controller.AuthController;
import domain.SessionContext;
import domain.User;

public class MainView {
    private final AuthController authController;

    public MainView(AuthController authController) {
        this.authController = authController;
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
                        // Call Member Management View
                        JOptionPane.showMessageDialog(null, "Member Management - To be implemented");
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
        } else {
            JOptionPane.showMessageDialog(null, "Login failed: " + infoReceived.get("message"));
        }


    }
}
