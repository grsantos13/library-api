package br.com.servicosaprimore.libraryapi.model.repository;

import br.com.servicosaprimore.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir o livro na base com o isbn informado")
    public void returnTrueWhenIsbnExists(){
        //cenário
        String isbn = "131211";
        Book book = createBook();

        entityManager.persist(book);

        //execução
        boolean exists = bookRepository.existsByIsbn(isbn);

        //verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o isbn informado")
    public void returnFalseWhenIsbnDoesNotExists(){
        //cenário
        String isbn = "131211";

        //execução
        boolean exists = bookRepository.existsByIsbn(isbn);

        //verificação
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro pelo id")
    public void findByIdTest(){
        Book book = createBook();
        entityManager.persist(book);

        Optional<Book> foundBook = bookRepository.findById(book.getId());

        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        Book book = createBook();
        Book savedBook = bookRepository.save(book);

        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest(){
        Book book = createBook();
        bookRepository.save(book);
        Book foundBook = entityManager.find(Book.class, book.getId());

        bookRepository.delete(foundBook);

        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();
    }

    private Book createBook() {
        return Book.builder()
                .title("As aventuras sombrias de Joãozinho")
                .author("Natália Rodrigues")
                .isbn("131211")
                .build();
    }
}
