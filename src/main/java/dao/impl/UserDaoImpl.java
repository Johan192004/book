package dao.impl;

import dao.UserDao;
import domain.User;
import errors.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {
    private final Connection connection;

    public UserDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public User findByUserName(String userName) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by username", e);
        }
    }

    @Override
    public User create(User user) throws DataAccessException {
        String sql = "INSERT INTO users (name, username, password, role, isActive, createdAt) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getUserName());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            ps.setDate(6, Date.valueOf(user.getCreatedAt()));
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                connection.rollback();
                throw new DataAccessException("Creating user failed, no rows affected", new SQLException("No rows affected"));
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                    connection.commit();
                    return user;
                } else {
                    connection.rollback();
                    throw new DataAccessException("Creating user failed, no ID obtained", new SQLException("No ID obtained"));
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Error rolling back transaction", rollbackEx);
            }
            throw new DataAccessException("Error creating user", e);
        }
    }

    @Override
    public User findById(int id) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by ID", e);
        }
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        String sql = "SELECT * FROM users ORDER BY createdAt DESC";
        List<User> users = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all users", e);
        }
    }

    @Override
    public boolean update(User user) throws DataAccessException {
        String sql = "UPDATE users SET name = ?, username = ?, password = ?, role = ?, isActive = ? WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getUserName());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            ps.setInt(6, user.getId());
            
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
            throw new DataAccessException("Error updating user", e);
        }
    }

    @Override
    public boolean delete(int id) throws DataAccessException {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
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
            throw new DataAccessException("Error deleting user", e);
        }
    }

    /**
     * Helper method to map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String userName = rs.getString("username");
        String password = rs.getString("password");
        User.Role role = User.Role.valueOf(rs.getString("role"));
        boolean isActive = rs.getBoolean("isActive");
        LocalDate createdAt = rs.getDate("createdAt").toLocalDate();
        
        return new User(id, name, userName, password, role, isActive, createdAt);
    }
}
