package util;

import domain.Member;
import domain.Book;
import domain.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TableFormatter {
    
    /**
     * Format a list of members as a table
     * @param members List of members to format
     * @return Formatted table string
     */
    public static String formatMembersTable(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return "No members found.";
        }

        StringBuilder table = new StringBuilder();
        
        // Header
        table.append(String.format("%-5s %-25s %-30s %-15s %-12s %-12s%n", 
            "ID", "NAME", "EMAIL", "PHONE", "STATUS", "CREATED"));
        table.append(repeatChar('-', 105)).append("\n");
        
        // Rows
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Member member : members) {
            String status = member.isActive() ? "[ACTIVE]" : "[INACTIVE]";
            String createdAt = member.getCreatedAt() != null ? 
                member.getCreatedAt().format(dateFormatter) : "N/A";
            
            table.append(String.format("%-5d %-25s %-30s %-15s %-12s %-12s%n",
                member.getId(),
                truncate(member.getName(), 25),
                truncate(member.getEmail(), 30),
                truncate(member.getPhone(), 15),
                status,
                createdAt
            ));
        }
        
        table.append(repeatChar('-', 105)).append("\n");
        table.append(String.format("Total: %d member(s)", members.size()));
        
        return table.toString();
    }

    /**
     * Format a single member as detailed info
     * @param member Member to format
     * @return Formatted string
     */
    public static String formatMemberDetails(Member member) {
        if (member == null) {
            return "Member not found.";
        }

        String status = member.isActive() ? "[ACTIVE]" : "[INACTIVE]";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String createdAt = member.getCreatedAt() != null ? 
            member.getCreatedAt().format(dateFormatter) : "N/A";

        return String.format(
            "Member Details\n" +
            "================\n" +
            "ID:        %d\n" +
            "Name:      %s\n" +
            "Email:     %s\n" +
            "Phone:     %s\n" +
            "Status:    %s\n" +
            "Created:   %s",
            member.getId(),
            member.getName(),
            member.getEmail(),
            member.getPhone(),
            status,
            createdAt
        );
    }

    /**
     * Format a list of books as a table
     * @param books List of books to format
     * @return Formatted table string
     */
    public static String formatBooksTable(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return "No books found.";
        }

        StringBuilder table = new StringBuilder();
        
        // Header
        table.append(String.format("%-15s %-30s %-25s %-12s %-8s %-9s %-10s %-12s%n", 
            "ISBN", "TITLE", "AUTHOR", "CATEGORY", "QTY", "AVAILABLE", "PRICE", "STATUS"));
        table.append(repeatChar('-', 125)).append("\n");
        
        // Rows
        for (Book book : books) {
            String status = book.isActive() ? "[ACTIVE]" : "[INACTIVE]";
            
            table.append(String.format("%-15s %-30s %-25s %-12s %-8d %-9d $%-9.2f %-12s%n",
                truncate(book.getIsbn(), 15),
                truncate(book.getTitle(), 30),
                truncate(book.getAuthor(), 25),
                book.getCategory() != null ? book.getCategory().name() : "N/A",
                book.getQuantity(),
                book.getAvailable(),
                book.getPrice(),
                status
            ));
        }
        
        table.append(repeatChar('-', 125)).append("\n");
        table.append(String.format("Total: %d book(s)", books.size()));
        
        return table.toString();
    }

    /**
     * Format a single book as detailed info
     * @param book Book to format
     * @return Formatted string
     */
    public static String formatBookDetails(Book book) {
        if (book == null) {
            return "Book not found.";
        }

        String status = book.isActive() ? "[ACTIVE]" : "[INACTIVE]";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String createdAt = book.getCreatedAt() != null ? 
            book.getCreatedAt().format(dateFormatter) : "N/A";

        return String.format(
            "Book Details\n" +
            "================\n" +
            "ISBN:      %s\n" +
            "Title:     %s\n" +
            "Author:    %s\n" +
            "Category:  %s\n" +
            "Quantity:  %d\n" +
            "Available: %d\n" +
            "Price:     $%.2f\n" +
            "Status:    %s\n" +
            "Created:   %s",
            book.getIsbn(),
            book.getTitle(),
            book.getAuthor(),
            book.getCategory() != null ? book.getCategory().name() : "N/A",
            book.getQuantity(),
            book.getAvailable(),
            book.getPrice(),
            status,
            createdAt
        );
    }

    /**
     * Truncate string to max length with ellipsis
     * @param str String to truncate
     * @param maxLength Maximum length
     * @return Truncated string
     */
    private static String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Repeat a character n times
     * @param c Character to repeat
     * @param count Number of times
     * @return Repeated string
     */
    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Format a list of users as a table
     * @param users List of users to format
     * @return Formatted table string
     */
    public static String formatUsersTable(List<User> users) {
        if (users == null || users.isEmpty()) {
            return "No users found.";
        }

        StringBuilder table = new StringBuilder();
        
        // Header
        table.append(String.format("%-5s %-25s %-20s %-12s %-12s %-12s%n", 
            "ID", "NAME", "USERNAME", "ROLE", "STATUS", "CREATED"));
        table.append(repeatChar('-', 95)).append("\n");
        
        // Rows
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (User user : users) {
            String status = user.isActive() ? "[ACTIVE]" : "[INACTIVE]";
            String createdAt = user.getCreatedAt() != null ? 
                user.getCreatedAt().format(dateFormatter) : "N/A";
            
            table.append(String.format("%-5d %-25s %-20s %-12s %-12s %-12s%n",
                user.getId(),
                truncate(user.getName(), 25),
                truncate(user.getUserName(), 20),
                user.getRole().name(),
                status,
                createdAt
            ));
        }
        
        table.append(repeatChar('-', 95)).append("\n");
        table.append(String.format("Total: %d user(s)", users.size()));
        
        return table.toString();
    }

    /**
     * Format user details
     * @param user User to format
     * @return Formatted details string
     */
    public static String formatUserDetails(User user) {
        if (user == null) {
            return "No user information available.";
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String status = user.isActive() ? "[ACTIVE]" : "[INACTIVE]";
        String createdAt = user.getCreatedAt() != null ? 
            user.getCreatedAt().format(dateFormatter) : "N/A";

        StringBuilder details = new StringBuilder();
        details.append(repeatChar('=', 50)).append("\n");
        details.append("USER DETAILS\n");
        details.append(repeatChar('=', 50)).append("\n");
        details.append(String.format("ID           : %d%n", user.getId()));
        details.append(String.format("Name         : %s%n", user.getName()));
        details.append(String.format("Username     : %s%n", user.getUserName()));
        details.append(String.format("Role         : %s%n", user.getRole().name()));
        details.append(String.format("Status       : %s%n", status));
        details.append(String.format("Created At   : %s%n", createdAt));
        details.append(repeatChar('=', 50)).append("\n");

        return details.toString();
    }
}

