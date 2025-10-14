package controller;

import domain.User;
import errors.BadRequestException;
import errors.ServiceException;
import errors.UnauthorizedException;
import service.AuthService;
import util.Logger;

import java.util.HashMap;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public HashMap<String,String> login(String username, String password) {
        HashMap<String,String> response = new HashMap<>();
        Logger.info("AuthController", String.format("Login attempt - Username: %s", username));
        
        try {
            User user = authService.Login(username, password);
            response.put("id", String.valueOf(user.getId()));
            response.put("role", user.getRole().name());
            response.put("name", user.getName());
            response.put("userName", user.getUserName());
            response.put("status", "200");
            response.put("message", "Login successful");
            
            Logger.info("AuthController", String.format("Login successful - UserId: %d, Role: %s", user.getId(), user.getRole()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("AuthController", String.format("Login failed - Bad request: %s", e.getMessage()));
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("AuthController", String.format("Login failed - Unauthorized: %s (Username: %s)", e.getMessage(), username));
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", e.getMessage());
            Logger.logException("AuthController", "Login error", e);
        }
        return response;
    }
}
