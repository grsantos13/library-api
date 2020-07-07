package br.com.servicosaprimore.libraryapi.model.repository;

import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
}
