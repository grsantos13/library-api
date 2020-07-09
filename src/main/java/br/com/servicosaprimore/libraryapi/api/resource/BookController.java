package br.com.servicosaprimore.libraryapi.api.resource;

import br.com.servicosaprimore.libraryapi.api.dto.BookDTO;
import br.com.servicosaprimore.libraryapi.api.dto.LoanDTO;
import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import br.com.servicosaprimore.libraryapi.service.BookService;
import br.com.servicosaprimore.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequiredArgsConstructor
@Api("Book API")
@Slf4j
public class BookController {
    private final BookService service;
    private final ModelMapper mapper;
    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create a book.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created."),
            @ApiResponse(code = 400, message = "Invalid parameters.")
    })
    public BookDTO create(@Valid @RequestBody BookDTO bookDTO){
        log.info(" creating a book for isbn: {} ", bookDTO.getIsbn());
        Book book = mapper.map(bookDTO, Book.class);
        book = service.save(book);
        return mapper.map(book, BookDTO.class);
    }

    @GetMapping("/{id}")
    @ApiOperation("Get a book's detail by id.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK."),
            @ApiResponse(code = 404, message = "Book not found.")
    })
    public BookDTO get(@PathVariable Long id){
        log.info(" getting a book details by id: {} ", id);
        return service.getById(id)
                                .map( book -> mapper.map(book, BookDTO.class))
                                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Delete a book by id.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "NO CONTENT."),
            @ApiResponse(code = 404, message = "Book not found.")
    })
    public void delete(@PathVariable Long id){
        log.info(" deleting book by id: {} ", id);
        Book book = service.getById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("/{id}")
    @ApiOperation("Update a book by id.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "NO CONTENT."),
            @ApiResponse(code = 400, message = "Invalid parameters."),
            @ApiResponse(code = 404, message = "Book not found.")
    })
    public BookDTO update(@PathVariable Long id, @RequestBody @Valid BookDTO bookDTO){
        log.info(" updating book by id: {} ", id);
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

    @GetMapping("/{id}/loans")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageRequest){
        Book book = service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        Page<Loan> result = loanService.getLoansByBook(book, pageRequest);

        List<LoanDTO> loanDTOList = result
                                        .getContent()
                                        .stream()
                                        .map(loan -> {
                                            Book loanBook = loan.getBook();
                                            BookDTO bookDTO = mapper.map(loanBook, BookDTO.class);
                                            LoanDTO loanDTO = mapper.map(loan, LoanDTO.class);
                                            loanDTO.setBook(bookDTO);
                                            return loanDTO;
                                        }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>(loanDTOList, pageRequest, result.getTotalElements());
    }
}
