package dao.impl;

import dao.LoanDao;
import domain.Loan;
import errors.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDaoImpl implements LoanDao {
    private final Connection connection;

    public LoanDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Loan save(Loan loan) throws DataAccessException {
        String sql = "INSERT INTO loans (memberId, isbn, borrowDate, dueDate, returnDate, status, fineAmount, createdAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getMemberId());
            ps.setString(2, loan.getIsbn());
            ps.setDate(3, Date.valueOf(loan.getBorrowDate()));
            ps.setDate(4, Date.valueOf(loan.getDueDate()));
            ps.setDate(5, loan.getReturnDate() != null ? Date.valueOf(loan.getReturnDate()) : null);
            ps.setString(6, loan.getStatus().name());
            ps.setDouble(7, loan.getFineAmount());
            ps.setDate(8, Date.valueOf(loan.getCreatedAt()));
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DataAccessException("Creating loan failed, no rows affected", new SQLException("No rows affected"));
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    loan.setId(generatedKeys.getInt(1));
                } else {
                    throw new DataAccessException("Creating loan failed, no ID obtained", new SQLException("No ID obtained"));
                }
            }
            
            return loan;
        } catch (SQLException e) {
            throw new DataAccessException("Error saving loan", e);
        }
    }

    @Override
    public Loan findById(int id) throws DataAccessException {
        String sql = "SELECT l.*, m.name as memberName, b.title as bookTitle " +
                     "FROM loans l " +
                     "LEFT JOIN members m ON l.memberId = m.id " +
                     "LEFT JOIN books b ON l.isbn = b.isbn " +
                     "WHERE l.id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLoan(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding loan by ID", e);
        }
    }

    @Override
    public List<Loan> findAll() throws DataAccessException {
        String sql = "SELECT l.*, m.name as memberName, b.title as bookTitle " +
                     "FROM loans l " +
                     "LEFT JOIN members m ON l.memberId = m.id " +
                     "LEFT JOIN books b ON l.isbn = b.isbn " +
                     "ORDER BY l.createdAt DESC";
        List<Loan> loans = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
            
            return loans;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all loans", e);
        }
    }

    @Override
    public boolean update(Loan loan) throws DataAccessException {
        String sql = "UPDATE loans SET memberId = ?, isbn = ?, borrowDate = ?, dueDate = ?, " +
                     "returnDate = ?, status = ?, fineAmount = ? WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, loan.getMemberId());
            ps.setString(2, loan.getIsbn());
            ps.setDate(3, Date.valueOf(loan.getBorrowDate()));
            ps.setDate(4, Date.valueOf(loan.getDueDate()));
            ps.setDate(5, loan.getReturnDate() != null ? Date.valueOf(loan.getReturnDate()) : null);
            ps.setString(6, loan.getStatus().name());
            ps.setDouble(7, loan.getFineAmount());
            ps.setInt(8, loan.getId());
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error updating loan", e);
        }
    }

    @Override
    public boolean delete(int id) throws DataAccessException {
        String sql = "DELETE FROM loans WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting loan", e);
        }
    }

    @Override
    public List<Loan> findByMemberId(int memberId) throws DataAccessException {
        String sql = "SELECT l.*, m.name as memberName, b.title as bookTitle " +
                     "FROM loans l " +
                     "LEFT JOIN members m ON l.memberId = m.id " +
                     "LEFT JOIN books b ON l.isbn = b.isbn " +
                     "WHERE l.memberId = ? " +
                     "ORDER BY l.createdAt DESC";
        List<Loan> loans = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapResultSetToLoan(rs));
                }
            }
            
            return loans;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding loans by member ID", e);
        }
    }

    @Override
    public List<Loan> findByIsbn(String isbn) throws DataAccessException {
        String sql = "SELECT l.*, m.name as memberName, b.title as bookTitle " +
                     "FROM loans l " +
                     "LEFT JOIN members m ON l.memberId = m.id " +
                     "LEFT JOIN books b ON l.isbn = b.isbn " +
                     "WHERE l.isbn = ? " +
                     "ORDER BY l.createdAt DESC";
        List<Loan> loans = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, isbn);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapResultSetToLoan(rs));
                }
            }
            
            return loans;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding loans by ISBN", e);
        }
    }

    @Override
    public List<Loan> findByStatus(Loan.Status status) throws DataAccessException {
        String sql = "SELECT l.*, m.name as memberName, b.title as bookTitle " +
                     "FROM loans l " +
                     "LEFT JOIN members m ON l.memberId = m.id " +
                     "LEFT JOIN books b ON l.isbn = b.isbn " +
                     "WHERE l.status = ? " +
                     "ORDER BY l.createdAt DESC";
        List<Loan> loans = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapResultSetToLoan(rs));
                }
            }
            
            return loans;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding loans by status", e);
        }
    }

    @Override
    public List<Loan> findActiveLoansByMemberId(int memberId) throws DataAccessException {
        String sql = "SELECT l.*, m.name as memberName, b.title as bookTitle " +
                     "FROM loans l " +
                     "LEFT JOIN members m ON l.memberId = m.id " +
                     "LEFT JOIN books b ON l.isbn = b.isbn " +
                     "WHERE l.memberId = ? AND l.status IN ('BORROWED', 'OVERDUE') " +
                     "ORDER BY l.createdAt DESC";
        List<Loan> loans = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapResultSetToLoan(rs));
                }
            }
            
            return loans;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding active loans by member ID", e);
        }
    }

    @Override
    public Loan findActiveLoanByMemberAndIsbn(int memberId, String isbn) throws DataAccessException {
        String sql = "SELECT l.*, m.name as memberName, b.title as bookTitle " +
                     "FROM loans l " +
                     "LEFT JOIN members m ON l.memberId = m.id " +
                     "LEFT JOIN books b ON l.isbn = b.isbn " +
                     "WHERE l.memberId = ? AND l.isbn = ? AND l.status IN ('BORROWED', 'OVERDUE')";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setString(2, isbn);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLoan(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding active loan by member and ISBN", e);
        }
    }

    /**
     * Helper method to map ResultSet to Loan object
     */
    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getInt("id"));
        loan.setMemberId(rs.getInt("memberId"));
        loan.setIsbn(rs.getString("isbn"));
        loan.setBorrowDate(rs.getDate("borrowDate").toLocalDate());
        loan.setDueDate(rs.getDate("dueDate").toLocalDate());
        
        Date returnDate = rs.getDate("returnDate");
        if (returnDate != null) {
            loan.setReturnDate(returnDate.toLocalDate());
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            loan.setStatus(Loan.Status.valueOf(statusStr));
        }
        
        loan.setFineAmount(rs.getDouble("fineAmount"));
        
        Date createdAt = rs.getDate("createdAt");
        if (createdAt != null) {
            loan.setCreatedAt(createdAt.toLocalDate());
        }
        
        // Set additional display fields if available
        try {
            String memberName = rs.getString("memberName");
            if (memberName != null) {
                loan.setMemberName(memberName);
            }
        } catch (SQLException e) {
            // Column might not exist in some queries
        }
        
        try {
            String bookTitle = rs.getString("bookTitle");
            if (bookTitle != null) {
                loan.setBookTitle(bookTitle);
            }
        } catch (SQLException e) {
            // Column might not exist in some queries
        }
        
        return loan;
    }
}
