package dao.impl;

import dao.CatalogDao;
import domain.Book;
import errors.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CatalogDaoImpl implements CatalogDao {
    private final Connection connection;

    public CatalogDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Book save(Book book) throws DataAccessException {
        String sql = "INSERT INTO books (isbn, title, author, category, quantity, available, price, isActive, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory() != null ? book.getCategory().name() : "UNKNOWN");
            ps.setInt(5, book.getQuantity());
            ps.setInt(6, book.getAvailable());
            ps.setDouble(7, book.getPrice());
            ps.setBoolean(8, book.isActive());
            ps.setDate(9, Date.valueOf(book.getCreatedAt()));
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                connection.rollback();
                throw new DataAccessException("Creating book failed, no rows affected", new SQLException("No rows affected"));
            }
            
            connection.commit();
            return book;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Error rolling back transaction", rollbackEx);
            }
            throw new DataAccessException("Error saving book", e);
        }
    }

    @Override
    public Book findByIsbn(String isbn) throws DataAccessException {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, isbn);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding book by ISBN", e);
        }
    }

    @Override
    public List<Book> findAll() throws DataAccessException {
        String sql = "SELECT * FROM books ORDER BY createdAt DESC";
        List<Book> books = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            
            return books;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all books", e);
        }
    }

    @Override
    public boolean update(Book book) throws DataAccessException {
        String sql = "UPDATE books SET title = ?, author = ?, category = ?, quantity = ?, available = ?, price = ?, isActive = ? WHERE isbn = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory() != null ? book.getCategory().name() : "UNKNOWN");
            ps.setInt(4, book.getQuantity());
            ps.setInt(5, book.getAvailable());
            ps.setDouble(6, book.getPrice());
            ps.setBoolean(7, book.isActive());
            ps.setString(8, book.getIsbn());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Error rolling back transaction", rollbackEx);
            }
            throw new DataAccessException("Error updating book", e);
        }
    }

    @Override
    public boolean delete(String isbn) throws DataAccessException {
        String sql = "DELETE FROM books WHERE isbn = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, isbn);
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Error rolling back transaction", rollbackEx);
            }
            throw new DataAccessException("Error deleting book", e);
        }
    }

    /**
     * Helper method to map ResultSet to Book object
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        
        String categoryStr = rs.getString("category");
        if (categoryStr != null) {
            book.setCategory(Book.Category.valueOf(categoryStr));
        }
        
        book.setQuantity(rs.getInt("quantity"));
        book.setAvailable(rs.getInt("available"));
        book.setPrice(rs.getDouble("price"));
        book.setActive(rs.getBoolean("isActive"));
        
        Date createdAtDate = rs.getDate("createdAt");
        if (createdAtDate != null) {
            book.setCreatedAt(createdAtDate.toLocalDate());
        }
        
        return book;
    }

    @Override
    public List<Book> findByCategory(Book.Category category) throws DataAccessException {
        String sql = "SELECT * FROM books WHERE category = ? ORDER BY createdAt DESC";
        List<Book> books = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.name());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            
            return books;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding books by category", e);
        }
    }

    @Override
    public List<Book> findByAuthor(String author) throws DataAccessException {
        String sql = "SELECT * FROM books WHERE author LIKE ? ORDER BY createdAt DESC";
        List<Book> books = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + author + "%");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            
            return books;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding books by author", e);
        }
    }

    @Override
    public List<Book> findByCategoryAndAuthor(Book.Category category, String author) throws DataAccessException {
        String sql = "SELECT * FROM books WHERE category = ? AND author LIKE ? ORDER BY createdAt DESC";
        List<Book> books = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.name());
            ps.setString(2, "%" + author + "%");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            
            return books;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding books by category and author", e);
        }
    }
}
