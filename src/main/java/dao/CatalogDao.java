package dao;

import domain.Book;
import errors.DataAccessException;

public interface CatalogDao {
    Book addBook(Book book) throws DataAccessException;
}
