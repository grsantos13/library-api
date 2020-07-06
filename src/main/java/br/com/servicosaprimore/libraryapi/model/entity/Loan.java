package br.com.servicosaprimore.libraryapi.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "loan")
public class Loan {

    private Long id;
    private String customer;
    private Book book;
    private LocalDate loanDate;
    private boolean returned;
}
