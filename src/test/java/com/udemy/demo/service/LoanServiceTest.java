package com.udemy.demo.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.udemy.demo.api.dto.LoanDTO;
import com.udemy.demo.api.dto.LoanFilterDTO;
import com.udemy.demo.api.model.entity.Books;
import com.udemy.demo.api.model.entity.Loan;
import com.udemy.demo.exception.BusinessException;
import com.udemy.demo.model.repository.BooksRepository;
import com.udemy.demo.model.repository.LoanRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {
  LoanService service;
  
  @MockBean
  private LoanRepository repository;

  @BeforeEach
  public void setup(){
    this.service=new LoanServiceImpl(repository);
  }

  private Books createNewBook() {
    return Books.builder().isbn("123").author("Joao").title("Clean Code").build();
  }
  private Books createValidBook() {
    return Books.builder().isbn("123").author("Joao").title("Clean Code").build();
  }


  @Test
  @DisplayName("Deve salvar um emprestimo")
  public void saveLoanTest(){
    //cenário
    Books book = createNewBook();
    Loan savingLoan = Loan.builder().id(1l).book(book).customer("Fulano").loanDate(LocalDate.now()).build();

    Loan savedLoan=Loan.builder().id(1l).book(book).customer("Fulano").loanDate(LocalDate.now()).build();
    //to dizendo que vai retornar false, ou seja, não vai dar erro de businnes exception
    when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
    Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);

    //execucao
    Loan saved=service.save(savingLoan);

    //verificacao
    assertThat(saved.getId()).isEqualTo(savedLoan.getId());
    assertThat(saved.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
    assertThat(saved.getCustomer()).isEqualTo(savedLoan.getCustomer());
    assertThat(saved.getLoanDate()).isEqualTo(savedLoan.getLoanDate());

    
  }


  @Test
  @DisplayName("Deve lançar negócio ao salvar um empréstimo de livro já emprestado"  )
  public void loanedBookTest(){
    //cenário
    Books book = createNewBook();
    Loan savingLoan = Loan.builder().id(1l).book(book).customer("Fulano").loanDate(LocalDate.now()).build();



    //aqui diz que o livro não retornou...ou seja -=true, a expception será gerada
    when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

    //execucao

    Throwable ex=catchThrowable(()-> service.save(savingLoan));



    //verificacao
    assertThat(ex).isInstanceOf(BusinessException.class)
           .hasMessage("Book has already loaned");

   verify(repository,never()).save(savingLoan);


  }



  @Test
  @DisplayName("Deve obter as informações de empréstimo por id"  )
  public void getLoanDetailsTest(){
    //cenário
    Long id=1l;
    Loan loan=createLoan();
    loan.setId(id);




    //aqui diz que o livro não retornou...ou seja -=true, a expception será gerada
    when(repository.findById(id)).thenReturn(Optional.of(loan));

    //execucao
    Optional<Loan> result=service.getById(id);



    //verificacao
    assertThat(result.isPresent()).isTrue();
    assertThat(result.get().getId()).isEqualTo(id);
    assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
    assertThat(result.get().getBook()).isEqualTo(loan.getBook());
    assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

   verify(repository).findById(id);

  }

  public Loan createLoan(){
    Books book=createNewBook();
    String customer="Fulano";
    return Loan.builder().book(book).customer(customer).loanDate(LocalDate.now()).build();
  }

  @Test
  @DisplayName("Deve atualizar emprestimo")
  public void updateLoanTest(){
    //cenário
    Long id=1l;
    Loan loan=createLoan();
    loan.setId(id);
    loan.setReturned(true);



    when(repository.save(loan)).thenReturn(loan);

    Loan updatedLoan =service.update(loan);

    assertThat(updatedLoan.getReturned()).isTrue();
    verify(repository).save(loan);
  }

  @Test
  @DisplayName("Deve filtar emprestimos pelas propriedades")
  public void findLoanTest() {

    //cenário
    LoanFilterDTO loanFilterDTO=LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
    Loan loan=createLoan();
    loan.setId(1l);


    List<Loan> lista=Arrays.asList(loan);
    Page<Loan> page=new PageImpl<Loan>(lista, PageRequest.of(0,10),1);

    Mockito.when(repository.findByBookIsbnOrCustomer(Mockito.anyString(),Mockito.anyString(),Mockito.any(PageRequest.class))).thenReturn(page);

    //execução
    Page<Loan> result=service.find(loanFilterDTO,PageRequest.of(0,10));


    //verificações
    assertThat(result.getTotalElements()).isEqualTo(1);//la no cenário eu passei 1
    assertThat(result.getContent()).isEqualTo(lista); //la no cenário eu passei uma lista
    assertThat(result.getPageable().getPageNumber()).isEqualTo(0); //lá no cenário eu passei 0
    assertThat(result.getPageable().getPageSize()).isEqualTo(10); //la no cenário eu passei 10
  }


}
