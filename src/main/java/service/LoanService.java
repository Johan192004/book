package service;

import config.PropertiesLoad;
import dao.CatalogDao;
import dao.LoanDao;
import dao.MemberDao;
import domain.Book;
import domain.Loan;
import domain.Member;
import domain.User;
import errors.*;
import util.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LoanService {
    private final LoanDao loanDao;
    private final MemberDao memberDao;
    private final CatalogDao catalogDao;
    private final Connection connection;

    public LoanService(LoanDao loanDao, MemberDao memberDao, CatalogDao catalogDao, Connection connection) {
        this.loanDao = loanDao;
        this.memberDao = memberDao;
        this.catalogDao = catalogDao;
        this.connection = connection;
    }

    /**
     * Register a new loan
     * Both ADMIN and ASSISTANT can register loans
     */
    public Loan registerLoan(int memberId, String isbn, User.Role userRole) {
        try {
            // Validate permission
            validatePermissionForRegister(userRole);
            
            // Validate member exists and is active
            Member member = memberDao.findById(memberId);
            if (member == null) {
                throw new NotFoundException("Member not found with ID: " + memberId);
            }
            if (!member.isActive()) {
                throw new BadRequestException("Member is not active");
            }
            
            // Validate book exists and is available
            Book book = catalogDao.findByIsbn(isbn);
            if (book == null) {
                throw new NotFoundException("Book not found with ISBN: " + isbn);
            }
            if (!book.isActive()) {
                throw new BadRequestException("Book is not active");
            }
            if (book.getAvailable() <= 0) {
                throw new BadRequestException("Book is not available for loan");
            }
            
            // Check if member already has an active loan for this book
            Loan existingLoan = loanDao.findActiveLoanByMemberAndIsbn(memberId, isbn);
            if (existingLoan != null) {
                throw new ConflictException("Member already has an active loan for this book");
            }
            
            // Create new loan
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(PropertiesLoad.DAYS_BORROW_LIMIT);
            
            Loan newLoan = new Loan(memberId, isbn, borrowDate, dueDate);
            Loan savedLoan = loanDao.save(newLoan);
            
            // Update book availability
            book.setAvailable(book.getAvailable() - 1);
            catalogDao.update(book);
            
            // Commit transaction
            connection.commit();
            
            Logger.info("LoanService", String.format("Loan registered successfully - ID: %d, Member: %d, ISBN: %s by %s",
                    savedLoan.getId(), memberId, isbn, userRole.name()));
            
            return savedLoan;
            
        } catch (NotFoundException | BadRequestException | ConflictException | UnauthorizedException e) {
            // Rollback on business logic errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("LoanService", "Error rolling back transaction", rollbackEx);
            }
            throw e;
        } catch (DataAccessException | SQLException e) {
            // Rollback on database errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("LoanService", "Error rolling back transaction", rollbackEx);
            }
            Logger.logException("LoanService", "Error registering loan", e);
            throw new ServiceException("Error registering loan", e);
        }
    }

    /**
     * Mark loan as returned
     * Both ADMIN and ASSISTANT can mark returns
     */
    public Loan markReturn(int loanId, User.Role userRole) {
        try {
            // Validate permission
            validatePermissionForReturn(userRole);
            
            // Check if loan exists
            Loan loan = loanDao.findById(loanId);
            if (loan == null) {
                throw new NotFoundException("Loan not found with ID: " + loanId);
            }
            
            // If already returned, don't process again
            if (loan.getStatus() == Loan.Status.RETURNED) {
                throw new BadRequestException("Loan is already marked as returned");
            }
            
            // Get the book
            Book book = catalogDao.findByIsbn(loan.getIsbn());
            if (book == null) {
                throw new NotFoundException("Book not found with ISBN: " + loan.getIsbn());
            }
            
            // Set return date and status
            LocalDate returnDate = LocalDate.now();
            loan.setReturnDate(returnDate);
            loan.setStatus(Loan.Status.RETURNED);
            
            // Calculate fine if overdue
            if (returnDate.isAfter(loan.getDueDate())) {
                long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), returnDate);
                double fine = daysOverdue * PropertiesLoad.FINE_PER_DAY;
                loan.setFineAmount(fine);
                
                Logger.info("LoanService", String.format("Loan overdue - ID: %d, Days: %d, Fine: %.2f",
                        loanId, daysOverdue, fine));
            } else {
                loan.setFineAmount(0.0);
            }
            
            // Update loan
            boolean updated = loanDao.update(loan);
            
            if (!updated) {
                throw new ServiceException("Failed to mark loan as returned", new SQLException("Update returned false"));
            }
            
            // Update book availability
            book.setAvailable(book.getAvailable() + 1);
            catalogDao.update(book);
            
            // Commit transaction
            connection.commit();
            
            Logger.info("LoanService", String.format("Loan marked as returned - ID: %d by %s",
                    loanId, userRole.name()));
            
            return loan;
            
        } catch (NotFoundException | BadRequestException | UnauthorizedException e) {
            // Rollback on business logic errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("LoanService", "Error rolling back transaction", rollbackEx);
            }
            throw e;
        } catch (DataAccessException | SQLException e) {
            // Rollback on database errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("LoanService", "Error rolling back transaction", rollbackEx);
            }
            Logger.logException("LoanService", "Error marking loan as returned", e);
            throw new ServiceException("Error marking loan as returned", e);
        }
    }

    /**
     * Delete a loan (ADMIN only)
     */
    public boolean deleteLoan(int loanId, User.Role userRole) {
        try {
            // Only ADMIN can delete loans
            validatePermissionForDelete(userRole);
            
            // Check if loan exists
            Loan loan = loanDao.findById(loanId);
            if (loan == null) {
                throw new NotFoundException("Loan not found with ID: " + loanId);
            }
            
            // If loan is not returned, return the book first
            if (loan.getStatus() != Loan.Status.RETURNED) {
                Book book = catalogDao.findByIsbn(loan.getIsbn());
                if (book != null) {
                    book.setAvailable(book.getAvailable() + 1);
                    catalogDao.update(book);
                }
            }
            
            boolean deleted = loanDao.delete(loanId);
            
            if (deleted) {
                // Commit transaction
                connection.commit();
                
                Logger.info("LoanService", String.format("Loan deleted successfully - ID: %d by %s",
                        loanId, userRole.name()));
            }
            
            return deleted;
            
        } catch (NotFoundException | UnauthorizedException e) {
            // Rollback on business logic errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("LoanService", "Error rolling back transaction", rollbackEx);
            }
            throw e;
        } catch (DataAccessException | SQLException e) {
            // Rollback on database errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("LoanService", "Error rolling back transaction", rollbackEx);
            }
            Logger.logException("LoanService", "Error deleting loan", e);
            throw new ServiceException("Error deleting loan", e);
        }
    }

    /**
     * Get all loans
     * Both ADMIN and ASSISTANT can view all loans
     */
    public List<Loan> getAllLoans(User.Role userRole) {
        try {
            // Validate permission
            validatePermissionForView(userRole);
            
            List<Loan> loans = loanDao.findAll();
            
            if (loans == null || loans.isEmpty()) {
                throw new NotFoundException("No loans found");
            }
            
            // Update overdue statuses
            updateOverdueStatuses(loans);
            
            return loans;
            
        } catch (DataAccessException e) {
            Logger.logException("LoanService", "Error getting all loans", e);
            throw new ServiceException("Error getting all loans", e);
        }
    }

    /**
     * Find loan by ID
     */
    public Loan findLoanById(int loanId, User.Role userRole) {
        try {
            // Validate permission
            validatePermissionForView(userRole);
            
            Loan loan = loanDao.findById(loanId);
            if (loan == null) {
                throw new NotFoundException("Loan not found with ID: " + loanId);
            }
            
            // Update overdue status if needed
            updateLoanStatus(loan);
            
            return loan;
            
        } catch (DataAccessException e) {
            Logger.logException("LoanService", "Error finding loan by ID", e);
            throw new ServiceException("Error finding loan", e);
        }
    }

    /**
     * Find loans by member ID
     */
    public List<Loan> findLoansByMemberId(int memberId, User.Role userRole) {
        try {
            // Validate permission
            validatePermissionForView(userRole);
            
            List<Loan> loans = loanDao.findByMemberId(memberId);
            if (loans.isEmpty()) {
                throw new NotFoundException("No loans found for member ID: " + memberId);
            }
            
            // Update overdue statuses
            updateOverdueStatuses(loans);
            
            Logger.info("LoanService", String.format("Found %d loans for member ID: %d",
                    loans.size(), memberId));
            
            return loans;
            
        } catch (DataAccessException e) {
            Logger.logException("LoanService", "Error finding loans by member ID", e);
            throw new ServiceException("Error finding loans by member", e);
        }
    }

    /**
     * Find loans by ISBN
     */
    public List<Loan> findLoansByIsbn(String isbn, User.Role userRole) {
        try {
            // Validate permission
            validatePermissionForView(userRole);
            
            List<Loan> loans = loanDao.findByIsbn(isbn);
            if (loans.isEmpty()) {
                throw new NotFoundException("No loans found for ISBN: " + isbn);
            }
            
            // Update overdue statuses
            updateOverdueStatuses(loans);
            
            Logger.info("LoanService", String.format("Found %d loans for ISBN: %s",
                    loans.size(), isbn));
            
            return loans;
            
        } catch (DataAccessException e) {
            Logger.logException("LoanService", "Error finding loans by ISBN", e);
            throw new ServiceException("Error finding loans by ISBN", e);
        }
    }

    /**
     * Find loans by status
     */
    public List<Loan> findLoansByStatus(Loan.Status status, User.Role userRole) {
        try {
            // Validate permission
            validatePermissionForView(userRole);
            
            // First update all overdue statuses
            List<Loan> allLoans = loanDao.findAll();
            updateOverdueStatuses(allLoans);
            
            // Then get loans by status
            List<Loan> loans = loanDao.findByStatus(status);
            if (loans.isEmpty()) {
                throw new NotFoundException("No loans found with status: " + status.name());
            }
            
            Logger.info("LoanService", String.format("Found %d loans with status: %s",
                    loans.size(), status.name()));
            
            return loans;
            
        } catch (DataAccessException e) {
            Logger.logException("LoanService", "Error finding loans by status", e);
            throw new ServiceException("Error finding loans by status", e);
        }
    }

    /**
     * Update overdue statuses for a list of loans
     */
    private void updateOverdueStatuses(List<Loan> loans) {
        try {
            LocalDate today = LocalDate.now();
            for (Loan loan : loans) {
                if (loan.getStatus() == Loan.Status.BORROWED && today.isAfter(loan.getDueDate())) {
                    loan.setStatus(Loan.Status.OVERDUE);
                    loanDao.update(loan);
                }
            }
            connection.commit();
        } catch (DataAccessException | SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("LoanService", "Error rolling back transaction", rollbackEx);
            }
            Logger.logException("LoanService", "Error updating overdue statuses", e);
        }
    }

    /**
     * Update overdue status for a single loan
     */
    private void updateLoanStatus(Loan loan) {
        try {
            if (loan.getStatus() == Loan.Status.BORROWED && LocalDate.now().isAfter(loan.getDueDate())) {
                loan.setStatus(Loan.Status.OVERDUE);
                loanDao.update(loan);
                connection.commit();
            }
        } catch (DataAccessException | SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("LoanService", "Error rolling back transaction", rollbackEx);
            }
            Logger.logException("LoanService", "Error updating loan status", e);
        }
    }

    /**
     * Validate permission for registering loans (ADMIN and ASSISTANT)
     */
    private void validatePermissionForRegister(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN && userRole != User.Role.ASSISTANT) {
            throw new UnauthorizedException("Invalid user role");
        }
        
        Logger.info("LoanService", String.format("Permission validated for register - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for marking returns (ADMIN and ASSISTANT)
     */
    private void validatePermissionForReturn(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN && userRole != User.Role.ASSISTANT) {
            throw new UnauthorizedException("Invalid user role");
        }
        
        Logger.info("LoanService", String.format("Permission validated for return - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for deleting loans (ADMIN only)
     */
    private void validatePermissionForDelete(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can delete loans");
        }
        
        Logger.info("LoanService", String.format("Permission validated for delete - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for viewing loans (ADMIN and ASSISTANT)
     */
    private void validatePermissionForView(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN && userRole != User.Role.ASSISTANT) {
            throw new UnauthorizedException("Invalid user role");
        }
        
        Logger.info("LoanService", String.format("Permission validated for view - Role: %s", userRole.name()));
    }
}
