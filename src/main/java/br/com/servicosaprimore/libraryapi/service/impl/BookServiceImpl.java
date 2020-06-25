package br.com.servicosaprimore.libraryapi.service.impl;

import br.com.servicosaprimore.libraryapi.exception.BusinessException;
import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.model.repository.BookRepository;
import br.com.servicosaprimore.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if (repository.existsByIsbn(book.getIsbn())){
            throw new BusinessException("Isbn já cadastrado.");
        }

        return repository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if (book == null || book.getId() == null){
            throw new IllegalArgumentException("Book id não pode ser nulo.");
        }
        this.repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if (book == null || book.getId() == null){
            throw new IllegalArgumentException("Book id não pode ser nulo.");
        }
        return this.repository.save(book);
    }
}
