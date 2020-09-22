package com.udemy.demo.model.respository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.OPTIONAL;

import com.udemy.demo.api.model.entity.Books;
import com.udemy.demo.model.repository.BooksRepository;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

//aqui o autor fala em teste de integração


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

  @Autowired
  TestEntityManager entityManager;

  @Autowired
  BooksRepository booksRepository;

  @Test
  @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
  public void returnTrueWhenIsbnExists(){
      //cenário
        String isbn="123";
        Books book=createNewBook(isbn);
        entityManager.persist(book);

      //execução
    boolean exists=booksRepository.existsByIsbn(isbn);



      //verificação
    assertThat(exists).isTrue();//verdadeiro
  }

  @Test
  @DisplayName("Deve retornar false quando existir um livro na base com o isbn informado")
  public void returnFalseWhenIsbnExists(){
    //cenário
    String isbn="12345";
    //Books book=createNewBook(isbn);
    //entityManager.persist(book);

    //execução
    boolean exists=booksRepository.existsByIsbn(isbn);



    //verificação
    assertThat(exists).isFalse(); //falso
  }

  @Test
  @DisplayName("Deve obter um livro por ID")
  public void findByIdTest(){
    //cenário
    Books book=createNewBook("123");
    entityManager.persist(book);

    //execucao
    Optional<Books> foundBook=booksRepository.findById(book.getId());

    //verificação
    assertThat(foundBook.isPresent()).isTrue();

  }

  @Test
  @DisplayName("Deve salvar um livro")
  public void saveBookTestTest(){
    //cenário
    Books book=createNewBook("123");
    entityManager.persist(book);


    //execucao
    Books savedBook=booksRepository.save(book);

    //verificação - se tem  id informado no saved book
    assertThat(savedBook.getId()).isNotNull();

  }

  @Test
  @DisplayName("Deve salvar um livro")
  public void deleteBookTest(){
    //cenário -persistir livro
    Books book=createNewBook("123");
    entityManager.persist(book);
    Books foundBook=entityManager.find(Books.class,book.getId());



    //execução --exclui o livro
    booksRepository.delete(foundBook);


    //verificação - verifica se realmente foi excluidoo o livro
    Books deletedBook=entityManager.find(Books.class,book.getId());
    assertThat(deletedBook).isNull();

  }




  private Books createNewBook(String isbn) {
    return Books.builder().isbn(isbn).author("Joao").title("Clean Code").build();
  }

  class Library {
    private String orignalWord;
    private String originalLang;
    private String translatedWord;
    private String translationLanguage;


    public String getOrignalWord() {
      return orignalWord;
    }

    public void setOrignalWord(String orignalWord) {
      this.orignalWord = orignalWord;
    }

    public String getOriginalLang() {
      return originalLang;
    }

    public void setOriginalLang(String originalLang) {
      this.originalLang = originalLang;
    }

    public String getTranslatedWord() {
      return translatedWord;
    }

    public void setTranslatedWord(String translatedWord) {
      this.translatedWord = translatedWord;
    }

    public String getTranslationLanguage() {
      return translationLanguage;
    }

    public void setTranslationLanguage(String translationLanguage) {
      this.translationLanguage = translationLanguage;
    }
  }

}
