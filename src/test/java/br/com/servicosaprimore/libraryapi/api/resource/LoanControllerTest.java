package br.com.servicosaprimore.libraryapi.api.resource;

import br.com.servicosaprimore.libraryapi.api.dto.LoanDTO;
import br.com.servicosaprimore.libraryapi.api.dto.LoanFilterDTO;
import br.com.servicosaprimore.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.servicosaprimore.libraryapi.exception.BusinessException;
import br.com.servicosaprimore.libraryapi.model.entity.Book;
import br.com.servicosaprimore.libraryapi.model.entity.Loan;
import br.com.servicosaprimore.libraryapi.service.BookService;
import br.com.servicosaprimore.libraryapi.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    private final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar um empréstimo.")
    public void createLoanTest() throws Exception {

        LoanDTO dto = LoanDTO.builder()
                            .isbn("123")
                            .customer("Natália")
                            .customerEmail("customer@customer.com")
                            .build();

        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder()
                .id(1L)
                .isbn("123")
                .build();

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

        Loan loan = Loan.builder()
                        .id(1L)
                        .customer("Natália")
                        .customerEmail("customer@customer.com")
                        .book(book)
                        .loanDate(LocalDate.now())
                        .build();

        BDDMockito.given( loanService.save(Mockito.any(Loan.class)) ).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isCreated() )
                .andExpect( content().string("1") );
    }

    @Test
    @DisplayName("Deve retornar erro ao passar um ISBN inválido.")
    public void invalidIsbnCreateLoanTest() throws Exception{
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Natália")
                .customerEmail("customer@customer.com")
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", Matchers.hasSize(1)) )
                .andExpect(jsonPath("errors[0]").value("Book not found by informed isbn."));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar emprestar um livro já empresatado.")
    public void errorByLoanedBookTest() throws Exception{
        LoanDTO dto = LoanDTO.builder()
                .isbn("123")
                .customer("Natália")
                .customerEmail("customer@customer.com")
                .build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder()
                .id(1L)
                .isbn("123")
                .build();
        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

        BDDMockito.given( loanService.save(Mockito.any(Loan.class)) )
                .willThrow(new BusinessException("Book already loaned."));


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", Matchers.hasSize(1)) )
                .andExpect(jsonPath("errors[0]").value("Book already loaned."));
    }

    @Test
    @DisplayName("Deve devolver um livro")
    public void returnBookTest() throws Exception{
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);
        Loan loan = Loan.builder()
                        .id(1L)
                        .build();

        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isOk());

        verify(loanService, times(1)).update(loan);
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar devolver um livro inexistente.")
    public void returnNotFoundBookTest() throws Exception{
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve filtrar empréstimos")
    public void findLoansTest() throws Exception {
        String customer = "Natália";
        String customerEmail = "customer@customer.com";
        LocalDate loanDate = LocalDate.now();
        Long id = 1L;
        Book book = Book.builder().id(id).isbn("123").build();
        Loan loan = Loan.builder()
                .id(id)
                .customer(customer)
                .loanDate(loanDate)
                .customerEmail(customerEmail)
                .book(book)
                .returned(true)
                .build();

        BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 100), 1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=100",
                book.getIsbn(),
                loan.getCustomer());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }



}
