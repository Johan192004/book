package service;

import dao.CatalogDao;
import domain.Book;
import domain.User;
import errors.*;
import util.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CatalogService {
    private final CatalogDao catalogDao;
    private final Connection connection;

    public CatalogService(CatalogDao catalogDao, Connection connection) {
        this.catalogDao = catalogDao;
        this.connection = connection;
    }

    /**
     * Create a new book (ADMIN only)
     * @param isbn Book ISBN
     * @param title Book title
     * @param author Book author
     * @param category Book category
     * @param quantity Total quantity
     * @param price Book price
     * @param userRole Role of the user performing the action
     * @return Created book
     */
    public Book createBook(String isbn, String title, String author, Book.Category category, 
                          int quantity, double price, User.Role userRole) {
        try {
            // Only ADMIN can create books
            validatePermissionForCreate(userRole);
            
            // Check if ISBN already exists
            Book existingBook = catalogDao.findByIsbn(isbn);
            if (existingBook != null) {
                throw new ConflictException("A book with this ISBN already exists");
            }
            
            // Create new book
            Book newBook = new Book(isbn, title, author, category, quantity, quantity, price, true);
            Book savedBook = catalogDao.save(newBook);
            
            // Commit transaction
            connection.commit();
            
            Logger.info("CatalogService", String.format("Book created successfully - ISBN: %s by %s", 
                savedBook.getIsbn(), userRole.name()));
            
            return savedBook;
            
        } catch (ConflictException | UnauthorizedException e) {
            // Rollback on business logic errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("CatalogService", "Error rolling back transaction", rollbackEx);
            }
            throw e;
        } catch (DataAccessException | SQLException e) {
            // Rollback on database errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("CatalogService", "Error rolling back transaction", rollbackEx);
            }
            Logger.logException("CatalogService", "Error creating book", e);
            throw new ServiceException("Error creating book", e);
        }
    }

    /**
     * Update book information
     * ADMIN: Can update all fields
     * ASSISTANT: Can only update quantity, available, and price
     */
    public Book updateBook(String isbn, String title, String author, Book.Category category,
                          int quantity, int available, double price, boolean isActive, User.Role userRole) {
        try {
            // Both ADMIN and ASSISTANT can update books (with restrictions)
            validatePermissionForUpdate(userRole);
            
            // Check if book exists
            Book book = catalogDao.findByIsbn(isbn);
            if (book == null) {
                throw new NotFoundException("Book not found with ISBN: " + isbn);
            }
            
            // ADMIN can update all fields
            if (userRole == User.Role.ADMIN) {
                book.setTitle(title);
                book.setAuthor(author);
                book.setCategory(category);
                book.setQuantity(quantity);
                book.setAvailable(available);
                book.setPrice(price);
                book.setActive(isActive);
            } 
            // ASSISTANT can only update quantity, available, and price
            else if (userRole == User.Role.ASSISTANT) {
                book.setQuantity(quantity);
                book.setAvailable(available);
                book.setPrice(price);
            }
            
            boolean updated = catalogDao.update(book);
            
            if (!updated) {
                throw new ServiceException("Failed to update book", null);
            }
            
            // Commit transaction
            connection.commit();
            
            Logger.info("CatalogService", String.format("Book updated successfully - ISBN: %s by %s", 
                isbn, userRole.name()));
            
            return book;
            
        } catch (NotFoundException | UnauthorizedException e) {
            // Rollback on business logic errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("CatalogService", "Error rolling back transaction", rollbackEx);
            }
            throw e;
        } catch (DataAccessException | SQLException e) {
            // Rollback on database errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("CatalogService", "Error rolling back transaction", rollbackEx);
            }
            Logger.logException("CatalogService", "Error updating book", e);
            throw new ServiceException("Error updating book", e);
        }
    }

    /**
     * Delete a book (ADMIN only)
     */
    public boolean deleteBook(String isbn, User.Role userRole) {
        try {
            // Only ADMIN can delete books
            validatePermissionForDelete(userRole);
            
            // Check if book exists
            Book book = catalogDao.findByIsbn(isbn);
            if (book == null) {
                throw new NotFoundException("Book not found with ISBN: " + isbn);
            }
            
            boolean deleted = catalogDao.delete(isbn);
            
            if (deleted) {
                // Commit transaction
                connection.commit();
                Logger.info("CatalogService", String.format("Book deleted successfully - ISBN: %s by %s", 
                    isbn, userRole.name()));
            }
            
            return deleted;
            
        } catch (NotFoundException | UnauthorizedException e) {
            // Rollback on business logic errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("CatalogService", "Error rolling back transaction", rollbackEx);
            }
            throw e;
        } catch (DataAccessException | SQLException e) {
            // Rollback on database errors
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                Logger.logException("CatalogService", "Error rolling back transaction", rollbackEx);
            }
            Logger.logException("CatalogService", "Error deleting book", e);
            throw new ServiceException("Error deleting book", e);
        }
    }

    /**
     * Get all books
     */
    public List<Book> getAllBooks(User.Role userRole) {
        try {
            // Both ADMIN and ASSISTANT can view books
            validatePermissionForView(userRole);
            
            List<Book> books = catalogDao.findAll();
            
            if (books == null || books.isEmpty()) {
                throw new NotFoundException("No books found");
            }
            
            return books;
            
        } catch (DataAccessException e) {
            Logger.logException("CatalogService", "Error getting all books", e);
            throw new ServiceException("Error getting all books", e);
        }
    }

    /**
     * Find book by ISBN
     */
    public Book findBookByIsbn(String isbn, User.Role userRole) {
        try {
            // Both ADMIN and ASSISTANT can view books
            validatePermissionForView(userRole);
            
            Book book = catalogDao.findByIsbn(isbn);
            if (book == null) {
                throw new NotFoundException("Book not found with ISBN: " + isbn);
            }
            
            return book;
            
        } catch (DataAccessException e) {
            Logger.logException("CatalogService", "Error finding book by ISBN", e);
            throw new ServiceException("Error finding book", e);
        }
    }

    /**
     * Find books by category
     * Both ADMIN and ASSISTANT can filter books
     */
    public List<Book> findBooksByCategory(Book.Category category, User.Role userRole) {
        try {
            // Both ADMIN and ASSISTANT can view books
            validatePermissionForView(userRole);
            
            List<Book> books = catalogDao.findByCategory(category);
            if (books.isEmpty()) {
                throw new NotFoundException("No books found in category: " + category.name());
            }
            
            Logger.info("CatalogService", String.format("Found %d books in category: %s", 
                books.size(), category.name()));
            
            return books;
            
        } catch (DataAccessException e) {
            Logger.logException("CatalogService", "Error finding books by category", e);
            throw new ServiceException("Error finding books by category", e);
        }
    }

    /**
     * Find books by author
     * Both ADMIN and ASSISTANT can filter books
     */
    public List<Book> findBooksByAuthor(String author, User.Role userRole) {
        try {
            // Both ADMIN and ASSISTANT can view books
            validatePermissionForView(userRole);
            
            List<Book> books = catalogDao.findByAuthor(author);
            if (books.isEmpty()) {
                throw new NotFoundException("No books found by author: " + author);
            }
            
            Logger.info("CatalogService", String.format("Found %d books by author: %s", 
                books.size(), author));
            
            return books;
            
        } catch (DataAccessException e) {
            Logger.logException("CatalogService", "Error finding books by author", e);
            throw new ServiceException("Error finding books by author", e);
        }
    }

    /**
     * Find books by category and author
     * Both ADMIN and ASSISTANT can filter books
     */
    public List<Book> findBooksByCategoryAndAuthor(Book.Category category, String author, User.Role userRole) {
        try {
            // Both ADMIN and ASSISTANT can view books
            validatePermissionForView(userRole);
            
            List<Book> books = catalogDao.findByCategoryAndAuthor(category, author);
            if (books.isEmpty()) {
                throw new NotFoundException("No books found with category: " + category.name() + " and author: " + author);
            }
            
            Logger.info("CatalogService", String.format("Found %d books with category: %s and author: %s", 
                books.size(), category.name(), author));
            
            return books;
            
        } catch (DataAccessException e) {
            Logger.logException("CatalogService", "Error finding books by category and author", e);
            throw new ServiceException("Error finding books by category and author", e);
        }
    }

    /**
     * Validate permission for creating books (ADMIN only)
     */
    private void validatePermissionForCreate(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can create books");
        }
        
        Logger.info("CatalogService", String.format("Permission validated for create - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for updating books (ADMIN and ASSISTANT)
     */
    private void validatePermissionForUpdate(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN && userRole != User.Role.ASSISTANT) {
            throw new UnauthorizedException("Invalid user role");
        }
        
        Logger.info("CatalogService", String.format("Permission validated for update - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for deleting books (ADMIN only)
     */
    private void validatePermissionForDelete(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can delete books");
        }
        
        Logger.info("CatalogService", String.format("Permission validated for delete - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for viewing books (ADMIN and ASSISTANT)
     */
    private void validatePermissionForView(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN && userRole != User.Role.ASSISTANT) {
            throw new UnauthorizedException("Invalid user role");
        }
        
        Logger.info("CatalogService", String.format("Permission validated for view - Role: %s", userRole.name()));
    }
}

