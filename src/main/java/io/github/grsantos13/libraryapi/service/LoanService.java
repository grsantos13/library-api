package io.github.grsantos13.libraryapi.service;

import io.github.grsantos13.libraryapi.api.dto.LoanFilterDTO;
import io.github.grsantos13.libraryapi.model.entity.Book;
import io.github.grsantos13.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO dto, Pageable pageRequest);

    Page<Loan> getLoansByBook(Book book, Pageable pageRequest);

    List<Loan> getAllLateLoans();
}
