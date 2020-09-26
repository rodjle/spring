package com.udemy.demo.api.resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udemy.demo.api.dto.LoanDTO;
import com.udemy.demo.api.dto.LoanFilterDTO;
import com.udemy.demo.api.dto.ReturnedLoanDTO;
import com.udemy.demo.api.model.entity.Books;

import com.udemy.demo.api.model.entity.Loan;
import com.udemy.demo.exception.BusinessException;
import com.udemy.demo.service.BookService;
import com.udemy.demo.service.LoanService;
import java.time.LocalDate;
import java.util.Arrays;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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


  @Test
  @DisplayName("Deve retornar um livro")
  public void returnBookTest() throws Exception {
    //cenário (returned:true)
    ReturnedLoanDTO dto= ReturnedLoanDTO.builder().returned(true).build();
    Loan loan=Loan.builder().id(1l).build();
    //sem essa mock lá no controler o método       Loan loan=service.getById(id).get();, nao vai retornar nada
    //comente essa linha para ver
    //ou senha willReturn será o retorno do service.getById(id).get()
    BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));


    String json=new ObjectMapper().writeValueAsString(dto);

    mvc.perform(
        patch(LOAN_API.concat(("/1")))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
    ).andExpect(status().isOk());

    Mockito.verify(loanService,Mockito.times(1)).update(loan);
  }


  @Test
  @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente ")
  public void returnInexistentBookTest() throws Exception {
    //cenário (returned:true)
    ReturnedLoanDTO dto= ReturnedLoanDTO.builder().returned(true).build();


    //aqui estou dizendo que irá retornar o vazio o Loan loan=service.getById(id).get(); em LoanController
    BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());


    String json=new ObjectMapper().writeValueAsString(dto);

    mvc.perform(
        patch(LOAN_API.concat(("/1")))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
    ).andExpect(status().isNotFound());


  }


  @Test
  @DisplayName("Deve filtrar empréstimos")
  public void filterLoansTest() throws Exception{
    Long id=1l;

    Loan loan=createLoan();
    loan.setId(id);


    //busca qualquer livro, passando qualquer página e deve retorna um pagina de livros 0-100
    BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
        .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0,10),1));

    String queryString=String.format("?isbn=%s&customer=%s&page=0&size=10",loan.getBook().getIsbn(),loan.getCustomer());



    //execução
    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .get(LOAN_API.concat("/"+queryString))
        .accept(MediaType.APPLICATION_JSON);

    //verificação
    mvc
        .perform(request)
        .andExpect(status().isOk())
        .andExpect(jsonPath("content", Matchers.hasSize(1)))
        .andExpect(jsonPath("totalElements").value(1))
        .andExpect(jsonPath("pageable.pageSize").value(10))
        .andExpect(jsonPath("pageable.pageNumber").value(0))
    ;
  }


  public Loan createLoan(){
    Books book=createNewBook();
    String customer="Fulano";
    return Loan.builder().book(book).customer(customer).loanDate(LocalDate.now()).build();
  }

  private Books createNewBook() {
    return Books.builder().isbn("123").author("Joao").title("Clean Code").build();
  }


}
