package com.udemy.demo.model.respository;

import static org.assertj.core.api.Assertions.assertThat;

import com.udemy.demo.api.model.entity.Books;
import com.udemy.demo.api.model.entity.Loan;
import com.udemy.demo.model.repository.BooksRepository;
import com.udemy.demo.model.repository.LoanRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

//aqui o autor fala em teste de integração


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

  @Autowired
  TestEntityManager entityManager;

  @Autowired
  LoanRepository loanRepository;

  @Test
  @DisplayName("Deve retornar verificar se existe emprestimo não devolvido para o livro")
  public void returnTrueWhenIsbnExists() {
    Loan loan = createAndPersistLoan(LocalDate.now());
    Books book = loan.getBook();
    //execução
    boolean exists = loanRepository.existsByBookAndNotReturned(book);

    //verificacao
    //verifica se é verdade que emprestimo nao retornou
    assertThat(exists).isTrue();

    //verificação
    assertThat(exists).isTrue();//verdadeiro
  }

  @Test
  @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
  public void findByBookIsbnOrCustomer() {
    Loan loan=createAndPersistLoan(LocalDate.now());

    Page<Loan> result=loanRepository.findByBookIsbnOrCustomer("123","Fulano", PageRequest.of(0,10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent()).contains(loan);
    assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    assertThat(result.getTotalElements()).isEqualTo(1);

  }

  public Loan createAndPersistLoan(LocalDate loanDate) {
    //cenário
    String isbn = "123";
    Books book = createNewBook(isbn);
    entityManager.persist(book);
    Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(loanDate).build();
    entityManager.persist(loan);

    return  loan;
  }

  @Test
  @DisplayName("Deve obter emprestimo cuja data do emprestimo for menor ou igual a 3 dias atras e nao retornados")
  public void findByLoanDateLessThanAndNotReturned(){
    Loan loan= createAndPersistLoan(LocalDate.now().minusDays(5));
    List<Loan> result= loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

    assertThat(result).hasSize(1).contains(loan);

  }

  @Test
  @DisplayName("Deve retornar vazio quando não houver emprestimos atrasados")
  public void notFindByLoanDateLessThanAndNotReturned(){
    Loan loan= createAndPersistLoan(LocalDate.now());
    List<Loan> result= loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

    assertThat(result).isEmpty();

  }

  private Books createNewBook(String isbn) {
    return Books.builder().isbn(isbn).author("Joao").title("Clean Code").build();
  }

}
