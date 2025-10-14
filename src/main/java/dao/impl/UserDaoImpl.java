package dao.impl;

import dao.UserDao;
import domain.User;
import errors.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDaoImpl implements UserDao {
    private final Connection connection;

    public UserDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public User findByUserName(String userName) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try(
                PreparedStatement ps = connection.prepareStatement(sql)
                ){

            ps.setString(1, userName);

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(User.Role.valueOf(rs.getString("role")));
                    return user;
                }else{
                    return null;
                }
            }

        }catch (SQLException e){
            throw new DataAccessException("Error finding user by email", e);
        }
    }

    @Override
    public User create(User user) throws DataAccessException {
        return null;
    }
}
