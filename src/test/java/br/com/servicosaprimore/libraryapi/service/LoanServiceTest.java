package br.com.servicosaprimore.libraryapi.service;

import br.com.servicosaprimore.libraryapi.exception.BusinessException;
import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import br.com.servicosaprimore.libraryapi.model.repository.LoanRepository;
import br.com.servicosaprimore.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    private LoanService loanService;

    @MockBean
    private LoanRepository repository;

    @BeforeEach
    public void setUp(){
        this.loanService = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest(){
        Book book = Book.builder().id(1L).isbn("123").build();
        String customer = "Natália";
        LocalDate loanDate = LocalDate.now();

        Loan savingLoan = Loan.builder()
                        .book(book)
                        .customer(customer)
                        .loanDate(loanDate)
                        .build();

        Loan savedLoan = Loan.builder()
                                .id(1L)
                                .book(book)
                                .customer(customer)
                                .loanDate(loanDate)
                                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan = loanService.save(savingLoan);

        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar empréstimo com livro já emprestado")
    public void loanedBookSaveTest(){
        Book book = Book.builder().id(1L).isbn("123").build();
        String customer = "Natália";
        LocalDate loanDate = LocalDate.now();

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(loanDate)
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);
        Throwable error = catchThrowable(() ->loanService.save(savingLoan));

        assertThat(error)
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Book already loaned.");

        verify(repository, never()).save(savingLoan);
    }
}
