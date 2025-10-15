package service;

import config.PropertiesLoad;
import dao.CatalogDao;
import dao.LoanDao;
import dao.MemberDao;
import domain.Book;
import domain.Loan;
import domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Loan Service - Fine Calculation Test")
class LoanServiceTest {

    @Mock
    private LoanDao loanDao;

    @Mock
    private MemberDao memberDao;

    @Mock
    private CatalogDao catalogDao;

    @Mock
    private Connection connection;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanDao, memberDao, catalogDao, connection);
    }

    // ==================== CÁLCULO DE MULTAS ====================

    @Test
    @DisplayName("Multa por días de retraso - debe calcular correctamente")
    void testReturnLoan_WithOverdueDays_CalculatesFineCorrectly() throws Exception {
        // Arrange
        int loanId = 1;
        LocalDate loanDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = LocalDate.now().minusDays(5); // 5 días de retraso
        
        Loan loan = new Loan(loanId, 1, "978-3-16-148410-0", loanDate, dueDate, null, Loan.Status.BORROWED, 0.0, loanDate);
        Book book = new Book("978-3-16-148410-0", "Test Book", "Author", Book.Category.FICTION, 10, 9, 15.99, true);
        
        when(loanDao.findById(loanId)).thenReturn(loan);
        when(catalogDao.findByIsbn(loan.getIsbn())).thenReturn(book);
        when(loanDao.update(any(Loan.class))).thenReturn(true);
        when(catalogDao.update(any(Book.class))).thenReturn(true);

        // Act
        Loan returnedLoan = loanService.markReturn(loanId, User.Role.ADMIN);

        // Assert
        double expectedFine = 5 * PropertiesLoad.FINE_PER_DAY; // 5 días * 1500 = 7500.0
        assertEquals(expectedFine, returnedLoan.getFineAmount());
        assertEquals(Loan.Status.RETURNED, returnedLoan.getStatus());
        assertNotNull(returnedLoan.getReturnDate());
    }
}
