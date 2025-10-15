package dao;

import domain.Loan;
import errors.DataAccessException;

import java.util.List;

public interface LoanDao {
    /**
     * Save a new loan
     * @param loan Loan to save
     * @return The saved loan with generated ID
     * @throws DataAccessException if database error occurs
     */
    Loan save(Loan loan) throws DataAccessException;

    /**
     * Find loan by ID
     * @param id Loan ID
     * @return Loan if found, null otherwise
     * @throws DataAccessException if database error occurs
     */
    Loan findById(int id) throws DataAccessException;

    /**
     * Get all loans
     * @return List of all loans
     * @throws DataAccessException if database error occurs
     */
    List<Loan> findAll() throws DataAccessException;

    /**
     * Update loan information
     * @param loan Loan to update
     * @return true if update was successful
     * @throws DataAccessException if database error occurs
     */
    boolean update(Loan loan) throws DataAccessException;

    /**
     * Delete loan by ID
     * @param id Loan ID
     * @return true if deletion was successful
     * @throws DataAccessException if database error occurs
     */
    boolean delete(int id) throws DataAccessException;

    /**
     * Find loans by member ID
     * @param memberId Member ID
     * @return List of loans for the member
     * @throws DataAccessException if database error occurs
     */
    List<Loan> findByMemberId(int memberId) throws DataAccessException;

    /**
     * Find loans by ISBN
     * @param isbn Book ISBN
     * @return List of loans for the book
     * @throws DataAccessException if database error occurs
     */
    List<Loan> findByIsbn(String isbn) throws DataAccessException;

    /**
     * Find loans by status
     * @param status Loan status
     * @return List of loans with the given status
     * @throws DataAccessException if database error occurs
     */
    List<Loan> findByStatus(Loan.Status status) throws DataAccessException;

    /**
     * Find active loans by member ID (not returned)
     * @param memberId Member ID
     * @return List of active loans for the member
     * @throws DataAccessException if database error occurs
     */
    List<Loan> findActiveLoansByMemberId(int memberId) throws DataAccessException;

    /**
     * Find active loan by member ID and ISBN
     * @param memberId Member ID
     * @param isbn Book ISBN
     * @return Active loan if found, null otherwise
     * @throws DataAccessException if database error occurs
     */
    Loan findActiveLoanByMemberAndIsbn(int memberId, String isbn) throws DataAccessException;
}
