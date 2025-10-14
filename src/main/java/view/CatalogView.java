package view;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import controller.CatalogController;
import domain.SessionContext;
import domain.User;

import java.util.HashMap;

public class CatalogView {
    private final CatalogController catalogController;

    public CatalogView(CatalogController catalogController) {
        this.catalogController = catalogController;
    }

    public void displayMenu() {
        if(SessionContext.getCurrentUser() == null) {
            System.out.println("No user is currently logged in.");
            return;
        } else if (SessionContext.getCurrentUser().getRole() == User.Role.ADMIN) {
            displayCatalogAdmin();
        } else if (SessionContext.getCurrentUser().getRole() == User.Role.ASSISTANT) {
            displayCatalogAssistant();
        } else {
            System.out.println("Unknown role. Access denied.");
        }
    }

    private void displayCatalogAdmin() {
        String option = "";
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(null,
                    "Catalog Management - Admin\n" +
                            "1. Add Book\n" +
                            "2. Update Book\n" +
                            "3. Delete Book\n" +
                            "4. View All Books\n" +
                            "5. Search Book by ISBN\n" +
                            "6. Filter Books\n" +
                            "7. Exit\n" +
                            "Select an option:");
            if (option == null) {
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        addBookView();
                        break;
                    case "2":
                        updateBookViewAdmin();
                        break;
                    case "3":
                        deleteBookView();
                        break;
                    case "4":
                        viewAllBooksView();
                        break;
                    case "5":
                        searchBookView();
                        break;
                    case "6":
                        filterBooksView();
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

    private void displayCatalogAssistant() {
        String option = "";
        boolean exit = false;
        do {
            option = JOptionPane.showInputDialog(null,
                    "Catalog Management - Assistant\n" +
                            "1. Update Book (Limited)\n" +
                            "2. View All Books\n" +
                            "3. Search Book by ISBN\n" +
                            "4. Filter Books\n" +
                            "5. Exit\n" +
                            "Select an option:");
            if (option == null) {
                exit = true;
            } else {
                switch (option) {
                    case "1":
                        updateBookViewAssistant();
                        break;
                    case "2":
                        viewAllBooksView();
                        break;
                    case "3":
                        searchBookView();
                        break;
                    case "4":
                        filterBooksView();
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

    private void addBookView() {
        try {
            String isbn = getRequiredInput("Enter ISBN:");
            String title = getRequiredInput("Enter Title:");
            String author = getRequiredInput("Enter Author:");
            
            // Show numbered category menu
            String category = selectCategory();
            if (category == null) {
                return; // User cancelled
            }
            
            String quantity = getRequiredInput("Enter Quantity:");
            String price = getRequiredInput("Enter Price:");

            HashMap<String, String> response = catalogController.createBook(
                    isbn, title, author, category, quantity, price, 
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

    private void updateBookViewAdmin() {
        try {
            String isbn = getRequiredInput("Enter ISBN of book to update:");
            String title = getRequiredInput("Enter new Title:");
            String author = getRequiredInput("Enter new Author:");
            
            // Show numbered category menu
            String category = selectCategory();
            if (category == null) {
                return; // User cancelled
            }
            
            String quantity = getRequiredInput("Enter new Total Quantity:");
            String available = getRequiredInput("Enter new Available Quantity (for lending):");
            String price = getRequiredInput("Enter new Price:");
            String isActive = getRequiredInput("Is Active? (true/false):");

            HashMap<String, String> response = catalogController.updateBook(
                    isbn, title, author, category, quantity, available, price, isActive,
                    SessionContext.getCurrentUser().getRole()
            );

            if (response.get("status").equals("200")) {
                JOptionPane.showMessageDialog(null, response.get("message"));
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBookViewAssistant() {
        try {
            String isbn = getRequiredInput("Enter ISBN of book to update:");
            String quantity = getRequiredInput("Enter new Total Quantity:");
            String available = getRequiredInput("Enter new Available Quantity (for lending):");
            String price = getRequiredInput("Enter new Price:");

            HashMap<String, String> response = catalogController.updateBook(
                    isbn, null, null, null, quantity, available, price, null,
                    SessionContext.getCurrentUser().getRole()
            );

            if (response.get("status").equals("200")) {
                JOptionPane.showMessageDialog(null, response.get("message"));
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBookView() {
        try {
            String isbn = getRequiredInput("Enter ISBN of book to delete:");

            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete this book?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                HashMap<String, String> response = catalogController.deleteBook(
                        isbn, SessionContext.getCurrentUser().getRole()
                );

                if (response.get("status").equals("200")) {
                    JOptionPane.showMessageDialog(null, response.get("message"));
                } else {
                    JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAllBooksView() {
        try {
            HashMap<String, String> response = catalogController.getAllBooks(
                    SessionContext.getCurrentUser().getRole()
            );

            if (response.get("status").equals("200")) {
                // Display table in scrollable text area
                JTextArea textArea = new JTextArea(response.get("message"));
                textArea.setEditable(false);
                textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new java.awt.Dimension(900, 400));

                JOptionPane.showMessageDialog(null, scrollPane, "All Books", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchBookView() {
        try {
            String isbn = getRequiredInput("Enter ISBN to search:");

            HashMap<String, String> response = catalogController.findBookByIsbn(
                    isbn, SessionContext.getCurrentUser().getRole()
            );

            if (response.get("status").equals("200")) {
                // Display book details in scrollable text area
                JTextArea textArea = new JTextArea(response.get("message"));
                textArea.setEditable(false);
                textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));

                JOptionPane.showMessageDialog(null, scrollPane, "Book Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Filter books by category and/or author
     */
    private void filterBooksView() {
        try {
            String[] filterOptions = {"By Category", "By Author", "By Category and Author"};
            int filterChoice = JOptionPane.showOptionDialog(null,
                    "Select filter type:",
                    "Filter Books",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    filterOptions,
                    filterOptions[0]);

            if (filterChoice == -1) {
                return; // User cancelled
            }

            HashMap<String, String> response = null;

            switch (filterChoice) {
                case 0: // By Category
                    String category = selectCategory();
                    if (category == null) {
                        return; // User cancelled
                    }
                    
                    response = catalogController.findBooksByCategory(
                            category,
                            SessionContext.getCurrentUser().getRole()
                    );
                    break;

                case 1: // By Author
                    String author = getRequiredInput("Enter Author name (or partial name):");
                    response = catalogController.findBooksByAuthor(
                            author,
                            SessionContext.getCurrentUser().getRole()
                    );
                    break;

                case 2: // By Category and Author
                    String category2 = selectCategory();
                    if (category2 == null) {
                        return; // User cancelled
                    }
                    
                    String author2 = getRequiredInput("Enter Author name (or partial name):");
                    response = catalogController.findBooksByCategoryAndAuthor(
                            category2,
                            author2,
                            SessionContext.getCurrentUser().getRole()
                    );
                    break;
            }

            if (response != null) {
                if (response.get("status").equals("200")) {
                    // Display table in scrollable text area
                    JTextArea textArea = new JTextArea(response.get("message"));
                    textArea.setEditable(false);
                    textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new java.awt.Dimension(900, 400));

                    JOptionPane.showMessageDialog(null, scrollPane, "Filtered Books", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Error: " + response.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Helper method to select category from numbered menu
     * @return Category name or null if cancelled
     */
    private String selectCategory() {
        String option = JOptionPane.showInputDialog(null,
                "Select Category:\n" +
                        "1. UNKNOWN\n" +
                        "2. FICTION\n" +
                        "3. NON_FICTION\n" +
                        "4. SCIENCE\n" +
                        "5. TECHNOLOGY\n" +
                        "6. HISTORY\n" +
                        "7. OTHERS\n" +
                        "Enter option (1-7):");
        
        if (option == null) {
            return null; // User cancelled
        }
        
        switch (option.trim()) {
            case "1":
                return "UNKNOWN";
            case "2":
                return "FICTION";
            case "3":
                return "NON_FICTION";
            case "4":
                return "SCIENCE";
            case "5":
                return "TECHNOLOGY";
            case "6":
                return "HISTORY";
            case "7":
                return "OTHERS";
            default:
                JOptionPane.showMessageDialog(null, "Invalid option. Please select 1-7.", "Error", JOptionPane.ERROR_MESSAGE);
                return selectCategory(); // Recursive call for retry
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
