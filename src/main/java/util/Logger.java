package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized logging utility for the application.
 * Logs all activities and errors to app.log file.
 */
public class Logger {
    private static final String LOG_FILE = "app.log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log levels for categorizing log entries
     */
    public enum Level {
        INFO,    // General information
        WARN,    // Warning messages
        ERROR,   // Error messages
        DEBUG    // Debug information
    }
    
    /**
     * Write a log entry to the log file
     * @param level Log level
     * @param component Component or class name generating the log
     * @param message Log message
     */
    public static void log(Level level, String component, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logEntry = String.format("[%s] [%s] [%s] %s%n", 
                timestamp, level, component, message);
        
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.print(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    /**
     * Log an exception with stack trace
     * @param component Component or class name generating the log
     * @param message Error message
     * @param exception Exception to log
     */
    public static void logException(String component, String message, Exception exception) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("[%s] [ERROR] [%s] %s%n", timestamp, component, message);
            pw.printf("Exception: %s - %s%n", exception.getClass().getName(), exception.getMessage());
            exception.printStackTrace(pw);
            pw.println("---");
        } catch (IOException e) {
            System.err.println("Failed to write exception to log file: " + e.getMessage());
        }
    }
    
    /**
     * Convenience method for INFO level logs
     */
    public static void info(String component, String message) {
        log(Level.INFO, component, message);
    }
    
    /**
     * Convenience method for WARN level logs
     */
    public static void warn(String component, String message) {
        log(Level.WARN, component, message);
    }
    
    /**
     * Convenience method for ERROR level logs
     */
    public static void error(String component, String message) {
        log(Level.ERROR, component, message);
    }
    
    /**
     * Convenience method for DEBUG level logs
     */
    public static void debug(String component, String message) {
        log(Level.DEBUG, component, message);
    }
}
