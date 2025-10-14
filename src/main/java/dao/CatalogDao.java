package dao;

import domain.Book;
import errors.DataAccessException;

import java.util.List;

public interface CatalogDao {
    /**
     * Add a new book to the catalog
     * @param book Book to add
     * @return The added book
     * @throws DataAccessException if database error occurs
     */
    Book save(Book book) throws DataAccessException;

    /**
     * Find book by ISBN
     * @param isbn ISBN to search
     * @return Book if found, null otherwise
     * @throws DataAccessException if database error occurs
     */
    Book findByIsbn(String isbn) throws DataAccessException;

    /**
     * Get all books
     * @return List of all books
     * @throws DataAccessException if database error occurs
     */
    List<Book> findAll() throws DataAccessException;

    /**
     * Update book information
     * @param book Book to update
     * @return true if update was successful
     * @throws DataAccessException if database error occurs
     */
    boolean update(Book book) throws DataAccessException;

    /**
     * Delete book by ISBN
     * @param isbn ISBN of book to delete
     * @return true if deletion was successful
     * @throws DataAccessException if database error occurs
     */
    boolean delete(String isbn) throws DataAccessException;

    /**
     * Find books by category
     * @param category Category to filter by
     * @return List of books in the category
     * @throws DataAccessException if database error occurs
     */
    List<Book> findByCategory(Book.Category category) throws DataAccessException;

    /**
     * Find books by author
     * @param author Author name to filter by
     * @return List of books by the author
     * @throws DataAccessException if database error occurs
     */
    List<Book> findByAuthor(String author) throws DataAccessException;

    /**
     * Find books by category and author
     * @param category Category to filter by
     * @param author Author name to filter by
     * @return List of books matching both criteria
     * @throws DataAccessException if database error occurs
     */
    List<Book> findByCategoryAndAuthor(Book.Category category, String author) throws DataAccessException;
}
