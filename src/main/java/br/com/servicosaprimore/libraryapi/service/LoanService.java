package br.com.servicosaprimore.libraryapi.service;

import br.com.servicosaprimore.libraryapi.model.entity.Loan;

import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);
}
