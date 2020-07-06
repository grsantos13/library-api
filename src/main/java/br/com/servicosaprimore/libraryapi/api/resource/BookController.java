package br.com.servicosaprimore.libraryapi.api.resource;

import br.com.servicosaprimore.libraryapi.api.dto.BookDTO;
import br.com.servicosaprimore.libraryapi.api.exception.ApiErrors;
import br.com.servicosaprimore.libraryapi.exception.BusinessException;
import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService service;
    private ModelMapper mapper;

    public BookController(BookService service, ModelMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@Valid @RequestBody BookDTO bookDTO){
        Book book = mapper.map(bookDTO, Book.class);
        book = service.save(book);
        return mapper.map(book, BookDTO.class);
    }

    @GetMapping("/{id}")
    public BookDTO get(@PathVariable Long id){
        return service.getById(id)
                                .map( book -> mapper.map(book, BookDTO.class))
                                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        Book book = service.getById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("/{id}")
    public BookDTO update(@PathVariable Long id, BookDTO bookDTO){
        return service.getById(id).map(book ->{
            book.setAuthor(bookDTO.getAuthor());
            book.setTitle(bookDTO.getTitle());
            book = service.update(book);
            return mapper.map(book, BookDTO.class);
        }).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO book, Pageable pageRequest){
        Book filter = mapper.map(book, Book.class);
        Page<Book> result = service.find(filter, pageRequest);

        List<BookDTO> bookList = result.getContent()
                .stream()
                .map(entity -> mapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<BookDTO>(bookList, pageRequest, result.getTotalElements());
    }
}
