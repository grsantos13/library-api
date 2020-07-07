package br.com.servicosaprimore.libraryapi.model.repository;

import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.stubbing.defaultanswers.GloballyConfiguredAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro informado.")
    public void existsByBookAndNotReturnedTest(){
        Loan loan = createScenarioForTest();

        boolean exists = repository.existsByBookAndNotReturned(loan.getBook());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo ISBN do livro ou pelo Customer.")
    public void findByBookIsbnOrCustomerTest(){
        Loan loan = createScenarioForTest();

        Page<Loan> result = repository.findByBookIsbnOrCustomer(loan.getBook().getIsbn(),
                                                                    loan.getCustomer(),
                                                                    PageRequest.of(0, 10)
                                                                );
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    private Loan createScenarioForTest() {
        Book book = Book.builder()
                .isbn("123")
                .author("Natália Rodrigues")
                .title("As aventuras aventuradas")
                .build();
        entityManager.persist(book);

        Loan loan = Loan.builder()
                .customer("Natália")
                .book(book)
                .loanDate(LocalDate.now())
                .build();
        entityManager.persist(loan);

        return loan;
    }

}
