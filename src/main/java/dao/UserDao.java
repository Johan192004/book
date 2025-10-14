package dao;

import errors.DataAccessException;
import domain.User;

public interface UserDao {
    User findByUserName(String userName) throws DataAccessException;
    User create(User user) throws DataAccessException;
}
