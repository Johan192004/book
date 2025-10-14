package service;

import dao.UserDao;
import domain.User;
import errors.BadRequestException;
import errors.DataAccessException;
import errors.ServiceException;
import errors.UnauthorizedException;

public class AuthService {
    private UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User Login(String userName, String password) {
        try{
            validateCredentials(userName, password);
            User user = userDao.findByUserName(userName);

            if (user == null || !user.getPassword().equals(password)) {
                throw new UnauthorizedException("Invalid username or password");
            }

            return user;
        } catch (DataAccessException e) {
            throw new ServiceException("Error during login", e);
        }
    }

    private void validateCredentials(String userName, String password) {
        if (userName == null || userName.isEmpty()) {
            throw new BadRequestException("Username cannot be null or empty");
        }

        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Password cannot be null or empty");
        }
    }
}

