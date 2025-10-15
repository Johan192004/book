package dao;

import errors.DataAccessException;
import domain.User;

import java.util.List;

public interface UserDao {
    /**
     * Find user by username
     * @param userName Username to search
     * @return User if found, null otherwise
     * @throws DataAccessException if database error occurs
     */
    User findByUserName(String userName) throws DataAccessException;
    
    /**
     * Create a new user
     * @param user User to create
     * @return The created user with generated ID
     * @throws DataAccessException if database error occurs
     */
    User create(User user) throws DataAccessException;
    
    /**
     * Find user by ID
     * @param id User ID to search
     * @return User if found, null otherwise
     * @throws DataAccessException if database error occurs
     */
    User findById(int id) throws DataAccessException;
    
    /**
     * Get all users
     * @return List of all users
     * @throws DataAccessException if database error occurs
     */
    List<User> findAll() throws DataAccessException;
    
    /**
     * Update user information
     * @param user User to update
     * @return true if update was successful
     * @throws DataAccessException if database error occurs
     */
    boolean update(User user) throws DataAccessException;
    
    /**
     * Delete user by ID
     * @param id User ID to delete
     * @return true if deletion was successful
     * @throws DataAccessException if database error occurs
     */
    boolean delete(int id) throws DataAccessException;
}
