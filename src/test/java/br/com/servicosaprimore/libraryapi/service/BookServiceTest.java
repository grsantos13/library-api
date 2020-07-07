package br.com.servicosaprimore.libraryapi.service;

import br.com.servicosaprimore.libraryapi.exception.BusinessException;
import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.model.repository.BookRepository;
import br.com.servicosaprimore.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //cenario
        Book book = createValidBook();
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        when(repository.save(book)).thenReturn(Book.builder()
                                                            .id(1L)
                                                            .isbn("123")
                                                            .author("Gustavo")
                                                            .title("Aventuras notórias")
                                                            .build());

        //execução
        Book savedBook = service.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("Aventuras notórias");
        assertThat(savedBook.getAuthor()).isEqualTo("Gustavo");

    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar livro com isbn duplicado")
    public void shouldNotSaveBookWithDuplicatedIsbn(){
        Book book = createValidBook();
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        assertThat(exception)
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Isbn já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro pelo id")
    public void shouldFindBookByIdTest(){
        Long id = 1L;
        Book book = createValidBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = service.getById(id);

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazia ao obter um livro pelo id quando não existe na base")
    public void bookNotFoundByIdTest(){
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> foundBook = service.getById(id);

        assertThat(foundBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro pelo id")
    public void deleteBookByIdTest(){
        //cenario
        Book book = createValidBook();
        book.setId(1L);

        assertDoesNotThrow(() -> service.delete(book));

        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve lançar um erro ao tentar deletar um livro sem passar o id ou o livro")
    public void deleteBookWithInvalidParameterTest(){
        //cenario
        Book book = createValidBook();

        Throwable exception = Assertions.catchThrowable(() -> service.delete(book));


        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id não pode ser nulo.");

        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um book na base")
    public void updateBookTest(){
        Long id = 1L;
        Book outdatedBook = Book.builder().id(id).build();
        Book updatedBook = createValidBook();
            updatedBook.setId(id);

        when(repository.save(outdatedBook)).thenReturn(updatedBook);

        //execução
        Book book = service.update(outdatedBook);

        //verificação
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());

    }

    @Test
    @DisplayName("Deve ocorrer um erro ao atualizar um livro inexistente")
    public void updateBookWithInvalidParameterTest(){

        Book book = createValidBook();

        assertThrows(IllegalArgumentException.class, () -> service.update(book));

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest(){
        Book book = createValidBook();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> bookList = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(bookList, pageRequest, 1);

        when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                    .thenReturn(page);

        Page<Book> result = service.find(book, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(bookList);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn.")
    public void getBookByIsbnTest(){
        String isbn = "123";
        when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getIsbn()).isEqualTo(isbn);
        assertThat(book.get().getId()).isEqualTo(1);

        verify(repository, times(1)).findByIsbn(isbn);

    }

    private Book createValidBook() {
        return Book.builder()
                .isbn("123")
                .author("Gustavo")
                .title("Aventuras notórias")
                .build();
    }
}
