package controller;

import domain.Book;
import domain.User;
import errors.*;
import service.CatalogService;
import util.Logger;
import util.TableFormatter;

import java.util.HashMap;
import java.util.List;

public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * Create a new book (ADMIN only)
     */
    public HashMap<String, String> createBook(String isbn, String title, String author, String categoryStr,
                                             String quantityStr, String priceStr, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("CatalogController", String.format("Create book attempt - ISBN: %s, Role: %s", isbn, userRole));
        
        try {
            // Validate input data in controller
            validateBookInput(isbn, title, author, categoryStr, quantityStr, priceStr);
            
            // Parse category
            Book.Category category = parseCategory(categoryStr);
            
            // Parse quantity and price
            int quantity = Integer.parseInt(quantityStr);
            double price = Double.parseDouble(priceStr);
            
            Book book = catalogService.createBook(isbn, title, author, category, quantity, price, userRole);
            
            response.put("status", "201");
            response.put("message", "Book created successfully");
            response.put("isbn", book.getIsbn());
            response.put("title", book.getTitle());
            
            Logger.info("CatalogController", String.format("[201] Book created successfully - ISBN: %s", book.getIsbn()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[400] Create book failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[401] Create book failed - Unauthorized: %s", e.getMessage()));
            
        } catch (ConflictException e) {
            response.put("status", "409");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[409] Create book failed - Conflict: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("CatalogController", "[500] Create book error", e);
        }
        
        return response;
    }

    /**
     * Update a book
     */
    public HashMap<String, String> updateBook(String isbn, String title, String author, String categoryStr,
                                             String quantityStr, String availableStr, String priceStr, 
                                             String isActiveStr, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("CatalogController", String.format("Update book attempt - ISBN: %s, Role: %s", isbn, userRole));
        
        try {
            // Validate ISBN
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new BadRequestException("ISBN cannot be null or empty");
            }
            
            // Validate based on role
            if (userRole == User.Role.ADMIN) {
                // ADMIN: validate all fields
                validateBookInput(isbn, title, author, categoryStr, quantityStr, priceStr);
                validateAvailable(availableStr);
                validateIsActive(isActiveStr);
                
                Book.Category category = parseCategory(categoryStr);
                int quantity = Integer.parseInt(quantityStr);
                int available = Integer.parseInt(availableStr);
                double price = Double.parseDouble(priceStr);
                boolean isActive = Boolean.parseBoolean(isActiveStr);
                
                Book book = catalogService.updateBook(isbn, title, author, category, quantity, available, price, isActive, userRole);
                
                response.put("status", "200");
                response.put("message", "Book updated successfully");
                response.put("isbn", book.getIsbn());
                response.put("title", book.getTitle());
                
            } else if (userRole == User.Role.ASSISTANT) {
                // ASSISTANT: only validate quantity, available, and price
                validateQuantity(quantityStr);
                validateAvailable(availableStr);
                validatePrice(priceStr);
                
                int quantity = Integer.parseInt(quantityStr);
                int available = Integer.parseInt(availableStr);
                double price = Double.parseDouble(priceStr);
                
                // Get current book data for fields that ASSISTANT can't modify
                Book currentBook = catalogService.findBookByIsbn(isbn, userRole);
                
                Book book = catalogService.updateBook(isbn, currentBook.getTitle(), currentBook.getAuthor(), 
                    currentBook.getCategory(), quantity, available, price, currentBook.isActive(), userRole);
                
                response.put("status", "200");
                response.put("message", "Book updated successfully");
                response.put("isbn", book.getIsbn());
                response.put("title", book.getTitle());
            }
            
            Logger.info("CatalogController", String.format("[200] Book updated successfully - ISBN: %s", isbn));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[400] Update book failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[401] Update book failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[404] Update book failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("CatalogController", "[500] Update book error", e);
        }
        
        return response;
    }

    /**
     * Delete a book (ADMIN only)
     */
    public HashMap<String, String> deleteBook(String isbn, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("CatalogController", String.format("Delete book attempt - ISBN: %s, Role: %s", isbn, userRole));
        
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new BadRequestException("ISBN cannot be null or empty");
            }
            
            boolean deleted = catalogService.deleteBook(isbn, userRole);
            
            if (deleted) {
                response.put("status", "200");
                response.put("message", "Book deleted successfully");
                Logger.info("CatalogController", String.format("[200] Book deleted successfully - ISBN: %s", isbn));
            } else {
                response.put("status", "500");
                response.put("message", "Failed to delete book");
                Logger.warn("CatalogController", String.format("[500] Delete book failed - ISBN: %s", isbn));
            }
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[400] Delete book failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[401] Delete book failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[404] Delete book failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("CatalogController", "[500] Delete book error", e);
        }
        
        return response;
    }

    /**
     * Get all books
     */
    public HashMap<String, String> getAllBooks(User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("CatalogController", String.format("Get all books attempt - Role: %s", userRole));
        
        try {
            List<Book> books = catalogService.getAllBooks(userRole);
            
            // Format books as table and put in message
            String tableMessage = TableFormatter.formatBooksTable(books);
            
            response.put("status", "200");
            response.put("message", tableMessage);
            
            Logger.info("CatalogController", String.format("[200] Books retrieved successfully - Count: %d", books.size()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[401] Get all books failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[404] Get all books failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("CatalogController", "[500] Get all books error", e);
        }
        
        return response;
    }

    /**
     * Find book by ISBN
     */
    public HashMap<String, String> findBookByIsbn(String isbn, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("CatalogController", String.format("Find book by ISBN attempt - ISBN: %s, Role: %s", isbn, userRole));
        
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new BadRequestException("ISBN cannot be null or empty");
            }
            
            Book book = catalogService.findBookByIsbn(isbn, userRole);
            
            // Format book details and put in message
            String detailsMessage = TableFormatter.formatBookDetails(book);
            
            response.put("status", "200");
            response.put("message", detailsMessage);
            
            Logger.info("CatalogController", String.format("[200] Book found - ISBN: %s", isbn));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[400] Find book failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[401] Find book failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[404] Find book failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("CatalogController", "[500] Find book error", e);
        }
        
        return response;
    }

    /**
     * Find books by category
     */
    public HashMap<String, String> findBooksByCategory(String categoryStr, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("CatalogController", String.format("Find books by category attempt - Category: %s, Role: %s", categoryStr, userRole));
        
        try {
            if (categoryStr == null || categoryStr.trim().isEmpty()) {
                throw new BadRequestException("Category cannot be null or empty");
            }
            
            Book.Category category = parseCategory(categoryStr);
            List<Book> books = catalogService.findBooksByCategory(category, userRole);
            
            // Format books as table and put in message
            String tableMessage = TableFormatter.formatBooksTable(books);
            
            response.put("status", "200");
            response.put("message", tableMessage);
            
            Logger.info("CatalogController", String.format("[200] Books found by category - Count: %d", books.size()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[400] Find books by category failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[401] Find books by category failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[404] Find books by category failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("CatalogController", "[500] Find books by category error", e);
        }
        
        return response;
    }

    /**
     * Find books by author
     */
    public HashMap<String, String> findBooksByAuthor(String author, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("CatalogController", String.format("Find books by author attempt - Author: %s, Role: %s", author, userRole));
        
        try {
            if (author == null || author.trim().isEmpty()) {
                throw new BadRequestException("Author cannot be null or empty");
            }
            
            List<Book> books = catalogService.findBooksByAuthor(author, userRole);
            
            // Format books as table and put in message
            String tableMessage = TableFormatter.formatBooksTable(books);
            
            response.put("status", "200");
            response.put("message", tableMessage);
            
            Logger.info("CatalogController", String.format("[200] Books found by author - Count: %d", books.size()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[400] Find books by author failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[401] Find books by author failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[404] Find books by author failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("CatalogController", "[500] Find books by author error", e);
        }
        
        return response;
    }

    /**
     * Find books by category and author
     */
    public HashMap<String, String> findBooksByCategoryAndAuthor(String categoryStr, String author, User.Role userRole) {
        HashMap<String, String> response = new HashMap<>();
        Logger.info("CatalogController", String.format("Find books by category and author attempt - Category: %s, Author: %s, Role: %s", 
            categoryStr, author, userRole));
        
        try {
            if (categoryStr == null || categoryStr.trim().isEmpty()) {
                throw new BadRequestException("Category cannot be null or empty");
            }
            if (author == null || author.trim().isEmpty()) {
                throw new BadRequestException("Author cannot be null or empty");
            }
            
            Book.Category category = parseCategory(categoryStr);
            List<Book> books = catalogService.findBooksByCategoryAndAuthor(category, author, userRole);
            
            // Format books as table and put in message
            String tableMessage = TableFormatter.formatBooksTable(books);
            
            response.put("status", "200");
            response.put("message", tableMessage);
            
            Logger.info("CatalogController", String.format("[200] Books found by category and author - Count: %d", books.size()));
            
        } catch (BadRequestException e) {
            response.put("status", "400");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[400] Find books by category and author failed - Bad request: %s", e.getMessage()));
            
        } catch (UnauthorizedException e) {
            response.put("status", "401");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[401] Find books by category and author failed - Unauthorized: %s", e.getMessage()));
            
        } catch (NotFoundException e) {
            response.put("status", "404");
            response.put("message", e.getMessage());
            Logger.warn("CatalogController", String.format("[404] Find books by category and author failed - Not found: %s", e.getMessage()));
            
        } catch (ServiceException e) {
            response.put("status", "500");
            response.put("message", "Internal server error. Please try again later");
            Logger.logException("CatalogController", "[500] Find books by category and author error", e);
        }
        
        return response;
    }

    /**
     * Validate book input data
     */
    private void validateBookInput(String isbn, String title, String author, String category, String quantity, String price) {
        // Validate ISBN
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BadRequestException("ISBN cannot be null or empty");
        }
        if (isbn.trim().length() > 155) {
            throw new BadRequestException("ISBN cannot exceed 155 characters");
        }
        
        // Validate title
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("Title cannot be null or empty");
        }
        if (title.trim().length() > 255) {
            throw new BadRequestException("Title cannot exceed 255 characters");
        }
        
        // Validate author
        if (author == null || author.trim().isEmpty()) {
            throw new BadRequestException("Author cannot be null or empty");
        }
        if (author.trim().length() > 255) {
            throw new BadRequestException("Author cannot exceed 255 characters");
        }
        
        // Validate category
        if (category == null || category.trim().isEmpty()) {
            throw new BadRequestException("Category cannot be null or empty");
        }
        
        // Validate quantity
        validateQuantity(quantity);
        
        // Validate price
        validatePrice(price);
    }

    private void validateQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) {
            throw new BadRequestException("Quantity cannot be null or empty");
        }
        try {
            int qty = Integer.parseInt(quantity);
            if (qty < 0) {
                throw new BadRequestException("Quantity cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Quantity must be a valid number");
        }
    }

    private void validateAvailable(String available) {
        if (available == null || available.trim().isEmpty()) {
            throw new BadRequestException("Available quantity cannot be null or empty");
        }
        try {
            int avail = Integer.parseInt(available);
            if (avail < 0) {
                throw new BadRequestException("Available quantity cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Available quantity must be a valid number");
        }
    }

    private void validatePrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            throw new BadRequestException("Price cannot be null or empty");
        }
        try {
            double prc = Double.parseDouble(price);
            if (prc < 0) {
                throw new BadRequestException("Price cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Price must be a valid number");
        }
    }

    private void validateIsActive(String isActive) {
        if (isActive == null || isActive.trim().isEmpty()) {
            throw new BadRequestException("IsActive cannot be null or empty");
        }
    }

    private Book.Category parseCategory(String categoryStr) {
        try {
            return Book.Category.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid category. Valid categories are: " + 
                java.util.Arrays.toString(Book.Category.values()));
        }
    }
}

