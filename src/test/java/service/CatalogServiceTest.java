package service;

import dao.CatalogDao;
import domain.Book;
import domain.User;
import errors.ConflictException;
import errors.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Catalog Service - Stock and ISBN Validation Tests")
class CatalogServiceTest {

    @Mock
    private CatalogDao catalogDao;

    @Mock
    private Connection connection;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(catalogDao, connection);
    }

    // ==================== VALIDACIÓN DE STOCK ====================

    @Test
    @DisplayName("Stock positivo - debe crear libro exitosamente")
    void testCreateBook_WithPositiveStock_Success() throws Exception {
        String isbn = "978-3-16-148410-0";
        Book savedBook = new Book(isbn, "Test Book", "Author", Book.Category.FICTION, 10, 10, 15.99, true);
        
        when(catalogDao.findByIsbn(isbn)).thenReturn(null);
        when(catalogDao.save(any(Book.class))).thenReturn(savedBook);

        Book result = catalogService.createBook(isbn, "Test Book", "Author", Book.Category.FICTION, 10, 15.99, User.Role.ADMIN);

        assertEquals(10, result.getQuantity());
        assertTrue(result.getQuantity() > 0);
    }

    @Test
    @DisplayName("Stock negativo - debe lanzar excepción")
    void testCreateBook_WithNegativeStock_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Book("978-3-16-148410-0", "Book", "Author", Book.Category.FICTION, -5, 0, 15.99, true);
        });
    }

    // ==================== VALIDACIÓN DE ISBN ====================

    @Test
    @DisplayName("ISBN válido - debe crear libro exitosamente")
    void testCreateBook_WithValidISBN_Success() throws Exception {
        String isbn = "978-0-06-112008-4";
        Book savedBook = new Book(isbn, "Book", "Author", Book.Category.FICTION, 10, 10, 14.99, true);
        
        when(catalogDao.findByIsbn(isbn)).thenReturn(null);
        when(catalogDao.save(any(Book.class))).thenReturn(savedBook);

        Book result = catalogService.createBook(isbn, "Book", "Author", Book.Category.FICTION, 10, 14.99, User.Role.ADMIN);

        assertEquals(isbn, result.getIsbn());
    }

    @Test
    @DisplayName("ISBN nulo - debe lanzar excepción")
    void testCreateBook_WithNullISBN_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Book(null, "Book", "Author", Book.Category.FICTION, 10, 10, 15.99, true);
        });
    }

    @Test
    @DisplayName("ISBN duplicado - debe lanzar ConflictException")
    void testCreateBook_WithDuplicateISBN_ThrowsConflictException() throws Exception {
        String isbn = "978-3-16-148410-0";
        Book existingBook = new Book(isbn, "Existing", "Author", Book.Category.FICTION, 5, 5, 12.99, true);
        
        when(catalogDao.findByIsbn(isbn)).thenReturn(existingBook);

        assertThrows(ConflictException.class, () -> {
            catalogService.createBook(isbn, "New Book", "Author", Book.Category.SCIENCE, 10, 15.99, User.Role.ADMIN);
        });
    }

    @Test
    @DisplayName("Buscar por ISBN inexistente - debe lanzar NotFoundException")
    void testFindBookByISBN_NotFound_ThrowsNotFoundException() throws Exception {
        String isbn = "978-9-99-999999-9";
        
        when(catalogDao.findByIsbn(isbn)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            catalogService.findBookByIsbn(isbn, User.Role.ADMIN);
        });
    }
}
