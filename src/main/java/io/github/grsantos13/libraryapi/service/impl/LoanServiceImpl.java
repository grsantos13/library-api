package io.github.grsantos13.libraryapi.service.impl;

import io.github.grsantos13.libraryapi.api.dto.LoanFilterDTO;
import io.github.grsantos13.libraryapi.exception.BusinessException;
import io.github.grsantos13.libraryapi.model.entity.Book;
import io.github.grsantos13.libraryapi.model.entity.Loan;
import io.github.grsantos13.libraryapi.model.repository.LoanRepository;
import io.github.grsantos13.libraryapi.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {

    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if (repository.existsByBookAndNotReturned(loan.getBook())){
            throw new BusinessException("Book already loaned.");
        }
        return repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO dto, Pageable pageRequest) {
        return repository.findByBookIsbnOrCustomer(dto.getIsbn(), dto.getCustomer(), pageRequest);
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageRequest) {
        return repository.findByBook(book, pageRequest);
    }

    @Override
    public List<Loan> getAllLateLoans() {
        final Integer loanDays = 4;
        LocalDate threeDaysAgo = LocalDate.now().minusDays(loanDays);
        return repository.findByLoanDateLessThanAndNotReturned(threeDaysAgo);
    }
}
