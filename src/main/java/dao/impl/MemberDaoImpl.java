package dao.impl;

import dao.MemberDao;
import domain.Member;
import errors.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberDaoImpl implements MemberDao {
    private final Connection connection;

    public MemberDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Member save(Member member) throws DataAccessException {
        String sql = "INSERT INTO members (name, email, phone, isActive, createdAt) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setBoolean(4, member.isActive());
            ps.setDate(5, Date.valueOf(member.getCreatedAt()));
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DataAccessException("Creating member failed, no rows affected", new SQLException("No rows affected"));
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    member.setId(generatedKeys.getInt(1));
                    return member;
                } else {
                    throw new DataAccessException("Creating member failed, no ID obtained", new SQLException("No ID obtained"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving member", e);
        }
    }

    @Override
    public Member findByEmail(String email) throws DataAccessException {
        String sql = "SELECT * FROM members WHERE email = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMember(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding member by email", e);
        }
    }

    @Override
    public Member findByPhone(String phone) throws DataAccessException {
        String sql = "SELECT * FROM members WHERE phone = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phone);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMember(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding member by phone", e);
        }
    }

    @Override
    public Member findById(int id) throws DataAccessException {
        String sql = "SELECT * FROM members WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMember(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding member by id", e);
        }
    }

    @Override
    public List<Member> findAll() throws DataAccessException {
        String sql = "SELECT * FROM members ORDER BY createdAt DESC";
        List<Member> members = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
            
            return members;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all members", e);
        }
    }

    @Override
    public boolean update(Member member) throws DataAccessException {
        String sql = "UPDATE members SET name = ?, email = ?, phone = ?, isActive = ? WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setBoolean(4, member.isActive());
            ps.setInt(5, member.getId());
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error updating member", e);
        }
    }

    @Override
    public boolean delete(int id) throws DataAccessException {
        String sql = "DELETE FROM members WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting member", e);
        }
    }

    /**
     * Helper method to map ResultSet to Member object
     */
    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        boolean isActive = rs.getBoolean("isActive");
        LocalDate createdAt = rs.getDate("createdAt").toLocalDate();
        
        return new Member(id, name, email, phone, isActive, createdAt);
    }
}
