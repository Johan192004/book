package controller;

import domain.Loan;
import domain.User;
import errors.*;
import service.LoanService;
import util.Logger;
import util.TableFormatter;

import java.util.HashMap;
import java.util.List;

public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * Register a new loan
     * Both ADMIN and ASSISTANT can register loans
     */
    public HashMap<String, String> registerLoan(String memberIdStr, String isbn, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("LoanController", String.format("Register loan attempt - Member: %s, ISBN: %s, Role: %s",
                memberIdStr, isbn, userRole));
        
        try {
            // Validate input data
            validateLoanInput(memberIdStr, isbn);
            
            int memberId = Integer.parseInt(memberIdStr);
            
            Loan loan = loanService.registerLoan(memberId, isbn, userRole);
            
            response.put("status", "201");
            response.put("message", "Loan registered successfully");
            response.put("loanId", String.valueOf(loan.getId()));
            response.put("dueDate", loan.getDueDate().toString());
            
            Logger.info("LoanController", String.format("[201] Loan registered successfully - ID: %d", loan.getId()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[400] Register loan failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[401] Register loan failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[404] Register loan failed - Not found: %s", e.getMessage()));
            
        } catch (ConflictException e) {
            response.put("status", "409");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[409] Register loan failed - Conflict: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("LoanController", "[500] Register loan error", e);
        }
        
        return response;
    }

    /**
     * Mark loan as returned
     * Both ADMIN and ASSISTANT can mark returns
     */
    public HashMap<String, String> markReturn(String loanIdStr, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("LoanController", String.format("Mark return attempt - ID: %s, Role: %s", loanIdStr, userRole));
        
        try {
            // Validate loan ID
            if (loanIdStr == null || loanIdStr.trim().isEmpty()) {
                throw new BadRequestException("Loan ID is required");
            }
            
            int loanId = Integer.parseInt(loanIdStr);
            
            Loan loan = loanService.markReturn(loanId, userRole);
            
            response.put("status", "200");
            response.put("message", "Loan marked as returned successfully");
            response.put("loanId", String.valueOf(loan.getId()));
            response.put("returnDate", loan.getReturnDate().toString());
            response.put("fineAmount", String.format("%.2f", loan.getFineAmount()));
            
            if (loan.getFineAmount() > 0) {
                response.put("warning", String.format("Fine amount: %.2f", loan.getFineAmount()));
            }
            
            Logger.info("LoanController", String.format("[200] Loan marked as returned - ID: %d, Fine: %.2f",
                    loanId, loan.getFineAmount()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[400] Mark return failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[401] Mark return failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[404] Mark return failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("LoanController", "[500] Mark return error", e);
        }
        
        return response;
    }

    /**
     * Delete a loan (ADMIN only)
     */
    public HashMap<String, String> deleteLoan(String loanIdStr, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("LoanController", String.format("Delete loan attempt - ID: %s, Role: %s", loanIdStr, userRole));
        
        try {
            if (loanIdStr == null || loanIdStr.trim().isEmpty()) {
                throw new BadRequestException("Loan ID is required");
            }
            
            int loanId = Integer.parseInt(loanIdStr);
            
            boolean deleted = loanService.deleteLoan(loanId, userRole);
            
            if (deleted) {
                response.put("status", "200");
                response.put("message", "Loan deleted successfully");
                Logger.info("LoanController", String.format("[200] Loan deleted successfully - ID: %d", loanId));
            } else {
                response.put("status", "404");
                response.put("message", "Loan not found");
                Logger.warn("LoanController", String.format("[404] Loan not found - ID: %d", loanId));
            }
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[400] Delete loan failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[401] Delete loan failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[404] Delete loan failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("LoanController", "[500] Delete loan error", e);
        }
        
        return response;
    }

    /**
     * Get all loans
     */
    public HashMap<String, String> getAllLoans(User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("LoanController", String.format("Get all loans attempt - Role: %s", userRole));
        
        try {
            List<Loan> loans = loanService.getAllLoans(userRole);
            
            response.put("status", "200");
            response.put("message", "Loans retrieved successfully");
            response.put("count", String.valueOf(loans.size()));
            response.put("data", formatLoansTable(loans));
            
            Logger.info("LoanController", String.format("[200] Retrieved %d loans", loans.size()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[401] Get all loans failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[404] Get all loans failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("LoanController", "[500] Get all loans error", e);
        }
        
        return response;
    }

    /**
     * Find loan by ID
     */
    public HashMap<String, String> findLoanById(String loanIdStr, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("LoanController", String.format("Find loan by ID attempt - ID: %s, Role: %s", loanIdStr, userRole));
        
        try {
            if (loanIdStr == null || loanIdStr.trim().isEmpty()) {
                throw new BadRequestException("Loan ID is required");
            }
            
            int loanId = Integer.parseInt(loanIdStr);
            
            Loan loan = loanService.findLoanById(loanId, userRole);
            
            response.put("status", "200");
            response.put("message", "Loan found successfully");
            response.put("data", formatLoanDetails(loan));
            
            Logger.info("LoanController", String.format("[200] Loan found - ID: %d", loanId));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[400] Find loan failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[401] Find loan failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[404] Find loan failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("LoanController", "[500] Find loan error", e);
        }
        
        return response;
    }

    /**
     * Find loans by member ID
     */
    public HashMap<String, String> findLoansByMemberId(String memberIdStr, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("LoanController", String.format("Find loans by member ID attempt - Member: %s, Role: %s",
                memberIdStr, userRole));
        
        try {
            if (memberIdStr == null || memberIdStr.trim().isEmpty()) {
                throw new BadRequestException("Member ID is required");
            }
            
            int memberId = Integer.parseInt(memberIdStr);
            
            List<Loan> loans = loanService.findLoansByMemberId(memberId, userRole);
            
            response.put("status", "200");
            response.put("message", "Loans found successfully");
            response.put("count", String.valueOf(loans.size()));
            response.put("data", formatLoansTable(loans));
            
            Logger.info("LoanController", String.format("[200] Found %d loans for member ID: %d",
                    loans.size(), memberId));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[400] Find loans failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[401] Find loans failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[404] Find loans failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("LoanController", "[500] Find loans error", e);
        }
        
        return response;
    }

    /**
     * Find loans by ISBN
     */
    public HashMap<String, String> findLoansByIsbn(String isbn, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("LoanController", String.format("Find loans by ISBN attempt - ISBN: %s, Role: %s",
                isbn, userRole));
        
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new BadRequestException("ISBN is required");
            }
            
            List<Loan> loans = loanService.findLoansByIsbn(isbn, userRole);
            
            response.put("status", "200");
            response.put("message", "Loans found successfully");
            response.put("count", String.valueOf(loans.size()));
            response.put("data", formatLoansTable(loans));
            
            Logger.info("LoanController", String.format("[200] Found %d loans for ISBN: %s",
                    loans.size(), isbn));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[400] Find loans failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[401] Find loans failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[404] Find loans failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("LoanController", "[500] Find loans error", e);
        }
        
        return response;
    }

    /**
     * Find loans by status
     */
    public HashMap<String, String> findLoansByStatus(String statusStr, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("LoanController", String.format("Find loans by status attempt - Status: %s, Role: %s",
                statusStr, userRole));
        
        try {
            if (statusStr == null || statusStr.trim().isEmpty()) {
                throw new BadRequestException("Status is required");
            }
            
            Loan.Status status = parseStatus(statusStr);
            
            List<Loan> loans = loanService.findLoansByStatus(status, userRole);
            
            response.put("status", "200");
            response.put("message", "Loans found successfully");
            response.put("count", String.valueOf(loans.size()));
            response.put("data", formatLoansTable(loans));
            
            Logger.info("LoanController", String.format("[200] Found %d loans with status: %s",
                    loans.size(), status.name()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[400] Find loans failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[401] Find loans failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("LoanController", String.format("[404] Find loans failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("LoanController", "[500] Find loans error", e);
        }
        
        return response;
    }

    /**
     * Validate loan input data
     */
    private void validateLoanInput(String memberId, String isbn) {
        // Validate member ID
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new BadRequestException("Member ID is required");
        }
        
        try {
            Integer.parseInt(memberId);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Member ID must be a valid number");
        }
        
        // Validate ISBN
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BadRequestException("ISBN is required");
        }
        
        if (isbn.trim().length() > 155) {
            throw new BadRequestException("ISBN cannot exceed 155 characters");
        }
    }

    /**
     * Parse status string to Status enum
     */
    private Loan.Status parseStatus(String statusStr) {
        try {
            return Loan.Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Valid values: BORROWED, RETURNED, OVERDUE");
        }
    }

    /**
     * Format loans list as a table
     */
    private String formatLoansTable(List<Loan> loans) {
        return TableFormatter.formatLoansTable(loans);
    }

    /**
     * Format loan details
     */
    private String formatLoanDetails(Loan loan) {
        return TableFormatter.formatLoanDetails(loan);
    }
}
