package br.com.servicosaprimore.libraryapi.service;

import br.com.servicosaprimore.libraryapi.api.dto.LoanFilterDTO;
import br.com.servicosaprimore.libraryapi.exception.BusinessException;
import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import br.com.servicosaprimore.libraryapi.model.repository.LoanRepository;
import br.com.servicosaprimore.libraryapi.service.impl.LoanServiceImpl;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class
LoanServiceTest {

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

    @Test
    @DisplayName("Deve obter um empréstimo pelo Id.")
    public void getLoanByIdTest(){
        String customer = "Natália";
        LocalDate loanDate = LocalDate.now();
        Long id = 1L;
        Book book = Book.builder().id(id).isbn("123").build();
        Loan loan = Loan.builder()
                            .id(id)
                            .customer(customer)
                            .loanDate(loanDate)
                            .book(book)
                            .build();

        when(repository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = loanService.getById(id);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(customer);
        assertThat(result.get().getBook().getId()).isEqualTo(book.getId());
        assertThat(result.get().getLoanDate()).isEqualTo(loanDate);

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo.")
    public void updateLoanTest(){
        String customer = "Natália";
        LocalDate loanDate = LocalDate.now();
        Long id = 1L;
        Book book = Book.builder().id(id).isbn("123").build();
        Loan loan = Loan.builder()
                .id(id)
                .customer(customer)
                .loanDate(loanDate)
                .book(book)
                .returned(true)
                .build();

        when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = loanService.update(loan);

        assertThat(updatedLoan.isReturned()).isTrue();
        verify(repository, times(1)).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoanTest(){
        String customer = "Natália";
        LocalDate loanDate = LocalDate.now();
        Long id = 1L;
        Book book = Book.builder().id(id).isbn("123").build();

        LoanFilterDTO dto = LoanFilterDTO.builder()
                                .customer(customer)
                                .isbn("123")
                                .build();

        Loan loan = Loan.builder()
                .id(id)
                .customer(customer)
                .loanDate(loanDate)
                .book(book)
                .returned(true)
                .build();

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> loanList = Arrays.asList(loan);
        Page<Loan> page = new PageImpl<Loan>(loanList, pageRequest, 1);

        when(repository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class))
            ).thenReturn(page);

        Page<Loan> result = loanService.find(dto, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(loanList);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }
}
