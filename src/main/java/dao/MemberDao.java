package dao;

import domain.Member;
import errors.DataAccessException;

import java.util.List;

public interface MemberDao {
    /**
     * Save a new member to the database
     * @param member Member to save
     * @return The saved member with generated ID
     * @throws DataAccessException if database error occurs
     */
    Member save(Member member) throws DataAccessException;

    /**
     * Find member by email
     * @param email Email to search
     * @return Member if found, null otherwise
     * @throws DataAccessException if database error occurs
     */
    Member findByEmail(String email) throws DataAccessException;

    /**
     * Find member by phone
     * @param phone Phone to search
     * @return Member if found, null otherwise
     * @throws DataAccessException if database error occurs
     */
    Member findByPhone(String phone) throws DataAccessException;

    /**
     * Find member by ID
     * @param id Member ID
     * @return Member if found, null otherwise
     * @throws DataAccessException if database error occurs
     */
    Member findById(int id) throws DataAccessException;

    /**
     * Get all members
     * @return List of all members
     * @throws DataAccessException if database error occurs
     */
    List<Member> findAll() throws DataAccessException;

    /**
     * Update member information
     * @param member Member to update
     * @return true if update was successful
     * @throws DataAccessException if database error occurs
     */
    boolean update(Member member) throws DataAccessException;

    /**
     * Delete member by ID
     * @param id Member ID to delete
     * @return true if deletion was successful
     * @throws DataAccessException if database error occurs
     */
    boolean delete(int id) throws DataAccessException;
}
