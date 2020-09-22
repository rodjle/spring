package com.udemy.demo.api.resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udemy.demo.api.dto.LoanDTO;
import com.udemy.demo.api.model.entity.Books;

import com.udemy.demo.api.model.entity.Loan;
import com.udemy.demo.exception.BusinessException;
import com.udemy.demo.service.BookService;
import com.udemy.demo.service.LoanService;
import java.time.LocalDate;
import java.util.Optional;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc //configura um objetivo para fazer requisições
public class LoanControllerTest {

  static String LOAN_API="/api/loans";

  @Autowired
  MockMvc mvc;//simula as requisições

  @MockBean
  BookService bookService;

  @MockBean
  private LoanService loanService;

  @Test
  @DisplayName("Deve realizar um emprestimo")
  public void createLoanTest() throws Exception {
    LoanDTO dto= LoanDTO.builder().isbn("123").customer("Fulano").build();
    String json=new ObjectMapper().writeValueAsString(dto);

    Books book=Books.builder().id(1l).isbn("123").build();
    BDDMockito.given(bookService.getByIsbn("123"))
        .willReturn(Optional.of(book));


    Loan loan=Loan.builder().id(1l).customer("fulano").book(book).loanDate(LocalDate.now()).build();
    BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
        .willReturn(loan);

    MockHttpServletRequestBuilder request= MockMvcRequestBuilders
        .post(LOAN_API)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json);

    mvc
        .perform(request)
        .andExpect(status().isCreated())
        .andExpect(content().string("1"))

    ;
  }

  @Test
  @DisplayName("Retorna um erro ao tentar fazer empréstimo de um livro inexistente")
  public void invalidIsbnCreateLoanTest() throws Exception {
    LoanDTO dto= LoanDTO.builder().isbn("123").customer("Fulano").build();
    String json=new ObjectMapper().writeValueAsString(dto);

    BDDMockito.given(bookService.getByIsbn("123"))
        .willReturn(Optional.empty());


    MockHttpServletRequestBuilder request= MockMvcRequestBuilders
        .post(LOAN_API)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json);

    mvc
        .perform(request)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("errors", Matchers.hasSize(1)))
        .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"))


    ;
  }


  @Test
  @DisplayName("Retorna um se erro ao tentar emprestar um livro que já está emprestado")
  public void loanedBookErrorOnCreateLoanTest() throws Exception {
    LoanDTO dto= LoanDTO.builder().isbn("123").customer("Fulano").build();
    String json=new ObjectMapper().writeValueAsString(dto);

    Books book=Books.builder().id(1l).isbn("123").build();
    BDDMockito.given(bookService.getByIsbn("123"))
        .willReturn(Optional.of(book));

    BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
        .willThrow(new BusinessException("Book has already loaned"));


    MockHttpServletRequestBuilder request= MockMvcRequestBuilders
        .post(LOAN_API)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json);

    mvc
        .perform(request)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("errors", Matchers.hasSize(1)))
        .andExpect(jsonPath("errors[0]").value("Book has already loaned"))


    ;
  }

}
