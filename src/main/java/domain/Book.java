package domain;

import java.time.LocalDate;

public class Book {
    
    public enum Category {
        UNKNOWN,
        FICTION,
        NON_FICTION,
        SCIENCE,
        TECHNOLOGY,
        HISTORY,
        OTHERS
    }
    
    private String isbn;
    private String title;
    private String author;
    private Category category;
    private int quantity;
    private int available;
    private double price;
    private boolean isActive;
    private LocalDate createdAt;

    public Book() {
    }

    public Book(String isbn, String title, String author, Category category, int quantity, int available, double price, boolean isActive) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN is required");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }
        if (available < 0) {
            throw new IllegalArgumentException("Available must be >= 0");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }
        
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category != null ? category : Category.UNKNOWN;
        this.quantity = quantity;
        this.available = available;
        this.price = price;
        this.isActive = isActive;
        this.createdAt = LocalDate.now();
    }

    // Getters
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Category getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getAvailable() {
        return available;
    }

    public double getPrice() {
        return price;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }
        this.quantity = quantity;
    }

    public void setAvailable(int available) {
        if (available < 0) {
            throw new IllegalArgumentException("Available must be >= 0");
        }
        this.available = available;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }
        this.price = price;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Book{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", available=" + available +
                ", price=" + price +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
