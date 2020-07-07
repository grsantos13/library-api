package br.com.servicosaprimore.libraryapi.service.impl;

import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import br.com.servicosaprimore.libraryapi.model.repository.LoanRepository;
import br.com.servicosaprimore.libraryapi.service.LoanService;

public class LoanServiceImpl implements LoanService {

    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        return repository.save(loan);
    }
}
