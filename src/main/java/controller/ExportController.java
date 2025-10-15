package controller;

import domain.User;
import errors.*;
import service.ExportService;
import util.Logger;

import java.util.HashMap;

public class ExportController {
    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * Export all books to CSV file (ADMIN only)
     */
    public HashMap<String, String> exportBooksToCSV(String filePath, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("ExportController", String.format("Export books to CSV attempt - File: %s, Role: %s",
                filePath, userRole));
        
        try {
            // Validate file path
            if (filePath == null || filePath.trim().isEmpty()) {
                throw new BadRequestException("File path is required");
            }
            
            // Ensure .csv extension
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            String exportedFile = exportService.exportBooksToCSV(filePath, userRole);
            
            response.put("status", "200");
            response.put("message", "Books catalog exported successfully");
            response.put("filePath", exportedFile);
            
            Logger.info("ExportController", String.format("[200] Books exported successfully to: %s", exportedFile));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("ExportController", String.format("[400] Export books failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("ExportController", String.format("[401] Export books failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("ExportController", String.format("[404] Export books failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("ExportController", "[500] Export books error", e);
        }
        
        return response;
    }

    /**
     * Export overdue loans to CSV file (ADMIN only)
     */
    public HashMap<String, String> exportOverdueLoansToCSV(String filePath, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("ExportController", String.format("Export overdue loans to CSV attempt - File: %s, Role: %s",
                filePath, userRole));
        
        try {
            // Validate file path
            if (filePath == null || filePath.trim().isEmpty()) {
                throw new BadRequestException("File path is required");
            }
            
            // Ensure .csv extension
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            String exportedFile = exportService.exportOverdueLoansToCSV(filePath, userRole);
            
            response.put("status", "200");
            response.put("message", "Overdue loans exported successfully");
            response.put("filePath", exportedFile);
            
            Logger.info("ExportController", String.format("[200] Overdue loans exported successfully to: %s", exportedFile));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("ExportController", String.format("[400] Export overdue loans failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("ExportController", String.format("[401] Export overdue loans failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("ExportController", String.format("[404] Export overdue loans failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("ExportController", "[500] Export overdue loans error", e);
        }
        
        return response;
    }
}
