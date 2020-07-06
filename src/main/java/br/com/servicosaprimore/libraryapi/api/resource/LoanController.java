package br.com.servicosaprimore.libraryapi.api.resource;

import br.com.servicosaprimore.libraryapi.api.dto.LoanDTO;
import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import br.com.servicosaprimore.libraryapi.service.BookService;
import br.com.servicosaprimore.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;

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


}
