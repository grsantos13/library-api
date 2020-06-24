package br.com.servicosaprimore.libraryapi.service;

import br.com.servicosaprimore.libraryapi.model.entity.Book;

import java.util.Optional;


public interface BookService {
    Book save(Book book);

    Optional<Book> getById(Long id);

    void delete(Book book);
}
