package service;

import dao.CatalogDao;
import dao.LoanDao;
import domain.Book;
import domain.Loan;
import domain.User;
import errors.*;
import util.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ExportService {
    private final CatalogDao catalogDao;
    private final LoanDao loanDao;
    private final Connection connection;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ExportService(CatalogDao catalogDao, LoanDao loanDao, Connection connection) {
        this.catalogDao = catalogDao;
        this.loanDao = loanDao;
        this.connection = connection;
    }

    /**
     * Export all books to CSV file (ADMIN only)
     * @param filePath Path where the CSV file will be saved
     * @param userRole Role of the user performing the action
     * @return Path of the exported file
     */
    public String exportBooksToCSV(String filePath, User.Role userRole) {
        try {
            // Validate permission (ADMIN only)
            validatePermissionForExport(userRole);
            
            // Get all books
            List<Book> books = catalogDao.findAll();
            
            if (books == null || books.isEmpty()) {
                throw new NotFoundException("No books found to export");
            }
            
            // Create CSV file
            try (FileWriter writer = new FileWriter(filePath)) {
                // Write header
                writer.append("ISBN,Title,Author,Category,Quantity,Available,Price,Status,Created Date\n");
                
                // Write data
                for (Book book : books) {
                    writer.append(escapeCsvField(book.getIsbn())).append(",");
                    writer.append(escapeCsvField(book.getTitle())).append(",");
                    writer.append(escapeCsvField(book.getAuthor())).append(",");
                    writer.append(book.getCategory() != null ? book.getCategory().name() : "UNKNOWN").append(",");
                    writer.append(String.valueOf(book.getQuantity())).append(",");
                    writer.append(String.valueOf(book.getAvailable())).append(",");
                    writer.append(String.format(Locale.US, "%.2f", book.getPrice())).append(",");
                    writer.append(book.isActive() ? "ACTIVE" : "INACTIVE").append(",");
                    writer.append(book.getCreatedAt() != null ? book.getCreatedAt().format(dateFormatter) : "N/A");
                    writer.append("\n");
                }
                
                Logger.info("ExportService", String.format("Exported %d books to %s by %s",
                        books.size(), filePath, userRole.name()));
            }
            
            return filePath;
            
        } catch (IOException e) {
            Logger.logException("ExportService", "Error exporting books to CSV", e);
            throw new ServiceException("Error exporting books to CSV", e);
        } catch (DataAccessException e) {
            Logger.logException("ExportService", "Error accessing book data", e);
            throw new ServiceException("Error accessing book data for export", e);
        }
    }

    /**
     * Export overdue loans to CSV file (ADMIN only)
     * @param filePath Path where the CSV file will be saved
     * @param userRole Role of the user performing the action
     * @return Path of the exported file
     */
    public String exportOverdueLoansToCSV(String filePath, User.Role userRole) {
        try {
            // Validate permission (ADMIN only)
            validatePermissionForExport(userRole);
            
            // Get all loans and filter overdue ones
            List<Loan> allLoans = loanDao.findAll();
            LocalDate today = LocalDate.now();
            
            // Update overdue statuses first
            for (Loan loan : allLoans) {
                if (loan.getStatus() == Loan.Status.BORROWED && today.isAfter(loan.getDueDate())) {
                    loan.setStatus(Loan.Status.OVERDUE);
                    loanDao.update(loan);
                }
            }
            connection.commit();
            
            // Get overdue loans
            List<Loan> overdueLoans = loanDao.findByStatus(Loan.Status.OVERDUE);
            
            if (overdueLoans == null || overdueLoans.isEmpty()) {
                throw new NotFoundException("No overdue loans found to export");
            }
            
            // Create CSV file
            try (FileWriter writer = new FileWriter(filePath)) {
                // Write header
                writer.append("Loan ID,Member ID,Member Name,Book ISBN,Book Title,Borrow Date,Due Date,Days Overdue,Fine Amount,Created Date\n");
                
                // Write data
                for (Loan loan : overdueLoans) {
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), today);
                    
                    writer.append(String.valueOf(loan.getId())).append(",");
                    writer.append(String.valueOf(loan.getMemberId())).append(",");
                    writer.append(escapeCsvField(loan.getMemberName() != null ? loan.getMemberName() : "Unknown")).append(",");
                    writer.append(escapeCsvField(loan.getIsbn())).append(",");
                    writer.append(escapeCsvField(loan.getBookTitle() != null ? loan.getBookTitle() : "Unknown")).append(",");
                    writer.append(loan.getBorrowDate() != null ? loan.getBorrowDate().format(dateFormatter) : "N/A").append(",");
                    writer.append(loan.getDueDate() != null ? loan.getDueDate().format(dateFormatter) : "N/A").append(",");
                    writer.append(String.valueOf(daysOverdue)).append(",");
                    writer.append(String.format(Locale.US, "%.2f", loan.getFineAmount())).append(",");
                    writer.append(loan.getCreatedAt() != null ? loan.getCreatedAt().format(dateFormatter) : "N/A");
                    writer.append("\n");
                }
                
                Logger.info("ExportService", String.format("Exported %d overdue loans to %s by %s",
                        overdueLoans.size(), filePath, userRole.name()));
            }
            
            return filePath;
            
        } catch (IOException e) {
            Logger.logException("ExportService", "Error exporting overdue loans to CSV", e);
            throw new ServiceException("Error exporting overdue loans to CSV", e);
        } catch (DataAccessException | SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("ExportService", "Error rolling back transaction", rollbackEx);
            }
            Logger.logException("ExportService", "Error accessing loan data", e);
            throw new ServiceException("Error accessing loan data for export", e);
        }
    }

    /**
     * Escape CSV field to handle commas, quotes, and newlines
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        // If field contains comma, quote, or newline, wrap in quotes and escape existing quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        
        return field;
    }

    /**
     * Validate permission for export operations (ADMIN only)
     */
    private void validatePermissionForExport(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can export data");
        }
        
        Logger.info("ExportService", String.format("Permission validated for export - Role: %s", userRole.name()));
    }
}
