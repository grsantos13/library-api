package br.com.servicosaprimore.libraryapi.api.resource;

import br.com.servicosaprimore.libraryapi.api.dto.BookDTO;
import br.com.servicosaprimore.libraryapi.api.dto.LoanDTO;
import br.com.servicosaprimore.libraryapi.api.dto.LoanFilterDTO;
import br.com.servicosaprimore.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import br.com.servicosaprimore.libraryapi.service.BookService;
import br.com.servicosaprimore.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final ModelMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO loan){
        Book book = bookService.getBookByIsbn(loan.getIsbn())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Book not found by informed isbn.")
                                    );

        Loan creatingLoan = Loan.builder()
                        .book(book)
                        .customer(loan.getCustomer())
                        .loanDate(LocalDate.now())
                        .build();

        Loan createdLoan = loanService.save(creatingLoan);

        return createdLoan.getId();
    }

    @PatchMapping("/{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO returned){
        Loan loan = loanService.getById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
            loan.setReturned(returned.getReturned());

        loanService.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO filter, Pageable pageRequest){
        Page<Loan> result = loanService.find(filter, pageRequest);
        List<LoanDTO> list = result
                .getContent()
                .stream()
                .map(entity -> {
                    Book book = entity.getBook();
                    BookDTO bookDTO = mapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = mapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>(list, pageRequest, result.getTotalElements());
    }


}
