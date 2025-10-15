package service;

import dao.UserDao;
import domain.User;
import errors.*;
import util.Logger;

import java.util.List;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Create a new user (ADMIN only)
     * Users are created as ASSISTANT by default and active
     */
    public User createUser(String name, String username, String password, User.Role currentUserRole) {
        try {
            // Only ADMIN can create users
            validatePermissionForCreate(currentUserRole);
            
            // Check if username already exists
            User existingUser = userDao.findByUserName(username);
            if (existingUser != null) {
                throw new ConflictException("A user with this username already exists");
            }
            
            // Create new user with default values: ASSISTANT role, active, current date
            User newUser = new User(name, username, password, User.Role.ASSISTANT);
            User savedUser = userDao.create(newUser);
            
            Logger.info("UserService", String.format("User created successfully - Username: %s by %s", 
                savedUser.getUserName(), currentUserRole.name()));
            
            return savedUser;
            
        } catch (DataAccessException e) {
            Logger.logException("UserService", "Error creating user", e);
            throw new ServiceException("Error creating user", e);
        }
    }

    /**
     * Update user information (ADMIN only)
     */
    public User updateUser(int id, String name, String username, String password, 
                          User.Role role, boolean isActive, User.Role currentUserRole) {
        try {
            // Only ADMIN can update users
            validatePermissionForUpdate(currentUserRole);
            
            // Check if user exists
            User user = userDao.findById(id);
            if (user == null) {
                throw new NotFoundException("User not found with ID: " + id);
            }
            
            // Check if username is being changed and if it already exists
            if (!user.getUserName().equals(username)) {
                User existingUser = userDao.findByUserName(username);
                if (existingUser != null) {
                    throw new ConflictException("A user with this username already exists");
                }
            }
            
            // Update user fields
            user.setName(name);
            user.setUserName(username);
            user.setPassword(password);
            user.setRole(role);
            user.setActive(isActive);
            
            boolean updated = userDao.update(user);
            
            if (!updated) {
                throw new NotFoundException("Failed to update user - user not found");
            }
            
            Logger.info("UserService", String.format("User updated successfully - ID: %d by %s", 
                id, currentUserRole.name()));
            
            return user;
            
        } catch (DataAccessException e) {
            Logger.logException("UserService", "Error updating user", e);
            throw new ServiceException("Error updating user", e);
        }
    }

    /**
     * Delete user (ADMIN only)
     */
    public boolean deleteUser(int id, User.Role currentUserRole) {
        try {
            // Only ADMIN can delete users
            validatePermissionForDelete(currentUserRole);
            
            // Check if user exists
            User user = userDao.findById(id);
            if (user == null) {
                throw new NotFoundException("User not found with ID: " + id);
            }
            
            boolean deleted = userDao.delete(id);
            
            if (deleted) {
                Logger.info("UserService", String.format("User deleted successfully - ID: %d by %s", 
                    id, currentUserRole.name()));
            }
            
            return deleted;
            
        } catch (DataAccessException e) {
            Logger.logException("UserService", "Error deleting user", e);
            throw new ServiceException("Error deleting user", e);
        }
    }

    /**
     * Get all users (ADMIN only)
     */
    public List<User> getAllUsers(User.Role currentUserRole) {
        try {
            // Only ADMIN can view all users
            validatePermissionForView(currentUserRole);
            
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                throw new NotFoundException("No users found");
            }
            
            return users;
            
        } catch (DataAccessException e) {
            Logger.logException("UserService", "Error getting all users", e);
            throw new ServiceException("Error getting all users", e);
        }
    }

    /**
     * Find user by ID (ADMIN only)
     */
    public User findUserById(int id, User.Role currentUserRole) {
        try {
            // Only ADMIN can view users
            validatePermissionForView(currentUserRole);
            
            User user = userDao.findById(id);
            if (user == null) {
                throw new NotFoundException("User not found with ID: " + id);
            }
            
            return user;
            
        } catch (DataAccessException e) {
            Logger.logException("UserService", "Error finding user by ID", e);
            throw new ServiceException("Error finding user", e);
        }
    }

    /**
     * Find user by username (ADMIN only)
     */
    public User findUserByUsername(String username, User.Role currentUserRole) {
        try {
            // Only ADMIN can view users
            validatePermissionForView(currentUserRole);
            
            User user = userDao.findByUserName(username);
            if (user == null) {
                throw new NotFoundException("User not found with username: " + username);
            }
            
            return user;
            
        } catch (DataAccessException e) {
            Logger.logException("UserService", "Error finding user by username", e);
            throw new ServiceException("Error finding user", e);
        }
    }

    /**
     * Validate permission for creating users (ADMIN only)
     */
    private void validatePermissionForCreate(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can create users");
        }
        
        Logger.info("UserService", String.format("Permission validated for create - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for updating users (ADMIN only)
     */
    private void validatePermissionForUpdate(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can update users");
        }
        
        Logger.info("UserService", String.format("Permission validated for update - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for deleting users (ADMIN only)
     */
    private void validatePermissionForDelete(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can delete users");
        }
        
        Logger.info("UserService", String.format("Permission validated for delete - Role: %s", userRole.name()));
    }

    /**
     * Validate permission for viewing users (ADMIN only)
     */
    private void validatePermissionForView(User.Role userRole) {
        if (userRole == null) {
            throw new UnauthorizedException("User role is required");
        }
        
        if (userRole != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can view users");
        }
        
        Logger.info("UserService", String.format("Permission validated for view - Role: %s", userRole.name()));
    }
}
