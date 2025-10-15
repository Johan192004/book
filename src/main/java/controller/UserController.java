package controller;

import domain.User;
import errors.*;
import service.UserService;
import util.Logger;
import util.TableFormatter;

import java.util.HashMap;
import java.util.List;

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user (ADMIN only)
     * Users are created as ASSISTANT by default and active
     */
    public HashMap<String, String> createUser(String name, String username, String password, User.Role currentUserRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("UserController", String.format("Create user attempt - Username: %s, Role: %s", username, currentUserRole));
        
        try {
            // Validate input data in controller
            validateUserInput(name, username, password);
            
            User user = userService.createUser(name, username, password, currentUserRole);
            
            response.put("status", "201");
            response.put("message", "User created successfully");
            response.put("id", String.valueOf(user.getId()));
            response.put("username", user.getUserName());
            
            Logger.info("UserController", String.format("[201] User created successfully - Username: %s", user.getUserName()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[400] Create user failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[401] Create user failed - Unauthorized: %s", e.getMessage()));
            
        } catch (ConflictException e) {
            response.put("status", "409");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[409] Create user failed - Conflict: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("UserController", "[500] Create user error", e);
        }
        
        return response;
    }

    /**
     * Update user (ADMIN only)
     */
    public HashMap<String, String> updateUser(int id, String name, String username, String password,
                                              String roleStr, String isActiveStr, User.Role currentUserRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("UserController", String.format("Update user attempt - ID: %d, Role: %s", id, currentUserRole));
        
        try {
            // Validate input
            validateUserInput(name, username, password);
            validateIsActive(isActiveStr);
            
            User.Role role = parseRole(roleStr);
            boolean isActive = Boolean.parseBoolean(isActiveStr);
            
            User user = userService.updateUser(id, name, username, password, role, isActive, currentUserRole);
            
            response.put("status", "200");
            response.put("message", "User updated successfully");
            response.put("id", String.valueOf(user.getId()));
            response.put("username", user.getUserName());
            
            Logger.info("UserController", String.format("[200] User updated successfully - ID: %d", id));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[400] Update user failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[401] Update user failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[404] Update user failed - Not found: %s", e.getMessage()));
            
        } catch (ConflictException e) {
            response.put("status", "409");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[409] Update user failed - Conflict: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("UserController", "[500] Update user error", e);
        }
        
        return response;
    }

    /**
     * Delete user (ADMIN only)
     */
    public HashMap<String, String> deleteUser(int id, User.Role currentUserRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("UserController", String.format("Delete user attempt - ID: %d, Role: %s", id, currentUserRole));
        
        try {
            boolean deleted = userService.deleteUser(id, currentUserRole);
            
            if (deleted) {
                response.put("status", "200");
                response.put("message", "User deleted successfully");
                Logger.info("UserController", String.format("[200] User deleted successfully - ID: %d", id));
            } else {
                response.put("status", "500");
                response.put("message", "Failed to delete user");
                Logger.warn("UserController", String.format("[500] Delete user failed - ID: %d", id));
            }
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[401] Delete user failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[404] Delete user failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("UserController", "[500] Delete user error", e);
        }
        
        return response;
    }

    /**
     * Get all users (ADMIN only)
     */
    public HashMap<String, String> getAllUsers(User.Role currentUserRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("UserController", String.format("Get all users attempt - Role: %s", currentUserRole));
        
        try {
            List<User> users = userService.getAllUsers(currentUserRole);
            
            // Format users as table and put in message
            String tableMessage = TableFormatter.formatUsersTable(users);
            
            response.put("status", "200");
            response.put("message", tableMessage);
            
            Logger.info("UserController", String.format("[200] Users retrieved successfully - Count: %d", users.size()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[401] Get all users failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[404] Get all users failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("UserController", "[500] Get all users error", e);
        }
        
        return response;
    }

    /**
     * Find user by ID (ADMIN only)
     */
    public HashMap<String, String> findUserById(int id, User.Role currentUserRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("UserController", String.format("Find user by ID attempt - ID: %d, Role: %s", id, currentUserRole));
        
        try {
            User user = userService.findUserById(id, currentUserRole);
            
            // Format user details and put in message
            String detailsMessage = TableFormatter.formatUserDetails(user);
            
            response.put("status", "200");
            response.put("message", detailsMessage);
            
            Logger.info("UserController", String.format("[200] User found - ID: %d", id));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[401] Find user failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[404] Find user failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("UserController", "[500] Find user error", e);
        }
        
        return response;
    }

    /**
     * Find user by username (ADMIN only)
     */
    public HashMap<String, String> findUserByUsername(String username, User.Role currentUserRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("UserController", String.format("Find user by username attempt - Username: %s, Role: %s", username, currentUserRole));
        
        try {
            if (username == null || username.trim().isEmpty()) {
                throw new BadRequestException("Username cannot be null or empty");
            }
            
            User user = userService.findUserByUsername(username, currentUserRole);
            
            // Format user details and put in message
            String detailsMessage = TableFormatter.formatUserDetails(user);
            
            response.put("status", "200");
            response.put("message", detailsMessage);
            
            Logger.info("UserController", String.format("[200] User found - Username: %s", username));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[400] Find user failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[401] Find user failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("UserController", String.format("[404] Find user failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("UserController", "[500] Find user error", e);
        }
        
        return response;
    }

    /**
     * Validate user input data
     */
    private void validateUserInput(String name, String username, String password) {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Name cannot be null or empty");
        }
        if (name.trim().length() > 75) {
            throw new BadRequestException("Name cannot exceed 75 characters");
        }
        
        // Validate username
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username cannot be null or empty");
        }
        if (username.trim().length() > 75) {
            throw new BadRequestException("Username cannot exceed 75 characters");
        }
        
        // Validate password
        if (password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Password cannot be null or empty");
        }
        if (password.trim().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }
        if (password.trim().length() > 75) {
            throw new BadRequestException("Password cannot exceed 75 characters");
        }
    }

    private void validateIsActive(String isActive) {
        if (isActive == null || isActive.trim().isEmpty()) {
            throw new BadRequestException("IsActive cannot be null or empty");
        }
    }

    private User.Role parseRole(String roleStr) {
        try {
            return User.Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Valid roles are: ADMIN, ASSISTANT");
        }
    }
}
