package com.udemy.demo.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.udemy.demo.api.model.entity.Books;
import com.udemy.demo.exception.BusinessException;
import com.udemy.demo.model.repository.BooksRepository;
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
public class BookServiceTest {
  BookService service;
  
  @MockBean
  private BooksRepository repository;

  @BeforeEach
  public void setup(){
    this.service=new BooksServiceImpl(repository);
  }


  @Test
  @DisplayName("Deve salvar um livro")
  public void saveBookTest(){
    //cenário
    Books book = createNewBook();
    Mockito.when(repository.save(book)).thenReturn(Books.builder().id(1l).author("João").title("Clean Code").isbn("123").build());
    //execucao
    Books saved=service.save(book);

    //verificacao
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getIsbn()).isEqualTo("123");
    assertThat(saved.getAuthor()).isEqualTo("João");
    assertThat(saved.getTitle()).isEqualTo("Clean Code");

    
  }

  @Test
  @DisplayName("Deve lançar erro ao tentar lançar isbn lançado")
  public void shouldNotSaveBookWithIsbnDuplicated(){
    //cenário
    Books book = createNewBook();
    //quando repository executar existsByIsbn e passar qualquer string então  retornará TRUE NESSE TESTE, NA SEQUENCIA
    Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
    //execucao
    Throwable ex= Assertions.catchThrowable(()->service.save(book));


    //verificacao
    assertThat(ex)
           .isInstanceOf(BusinessException.class)
        .hasMessage("Isbn Já Cadastrado");

    //verifica se nao está chamado método salvar
    verify(repository,Mockito.never()).save(book);

  }

  private Books createNewBook() {
    return Books.builder().isbn("123").author("Joao").title("Clean Code").build();
  }
  private Books createValidBook() {
    return Books.builder().isbn("123").author("Joao").title("Clean Code").build();
  }

  @Test
  @DisplayName("Deve obter um livro por id")
  public void getByIdTeste() {
    //cenário
    Long id = 1l;
    Books book = createNewBook();
    book.setId(id);
    Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

    //execução
    Optional<Books> foundBook = service.getById(id);

    //verificações
    assertThat(foundBook.isPresent()).isTrue();
    assertThat(foundBook.get().getId()).isEqualTo(id);
    assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
    assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());

  }


  @Test
  @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele não existe na base")
  public void bookNotFoundByIdTest() {
    //cenário
    Long id = 1l;

    Mockito.when(repository.findById(id)).thenReturn(Optional.empty());//força a retornar vazio

    //execução
    Optional<Books> book = service.getById(id);

    //verificações
    assertThat(book.isPresent()).isFalse();


  }
  @Test
  @DisplayName("Deve deletar um livro")
  public void DeleteBookTest(){
    //cenário
    Books book= Books.builder().id(1l).build();

    //execução -- não pode lançar exceções
    org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->service.delete(book));

    //verificações - Verifica se no repository foi chamado 1vz o metodo delete
    verify(repository, times(1)).delete(book);

  }

  @Test
  @DisplayName("Deve ocorrer um erro ao tentar deletar um livro exitente")
  public void deleteInvalidBookTest(){
    //cenário
    Books book= new Books(); //sem id, ou seja, é certo que vai lançar um erro

    //execução -- não pode lançar exceções
    org.junit.jupiter.api.Assertions.assertThrows (IllegalArgumentException.class,() ->service.delete(book));

    //verificações - Verifica se no repository foi chamado 0vz o metodo delete
    verify(repository, times(0)).delete(book);

  }


  @Test
  @DisplayName("Deve ocorrer um erro ao tentar atualizar um livro exitente")
  public void updateInvalidBookTest(){
    //cenário
    Books book= new Books(); //sem id, ou seja, é certo que vai lançar um erro

    //execução -- não pode lançar exceções
    org.junit.jupiter.api.Assertions.assertThrows (IllegalArgumentException.class,() ->service.update(book));

    //verificações - Verifica se no repository foi chamado 0vz o metodo delete
    verify(repository, times(0)).save(book);

  }


  @Test
  @DisplayName("Deve atualizar um livro")
  public void updateBookTest(){
    //cenário
    Long id=1l;
    //livro a atualiazar
    Books updatingBook= Books.builder().id(id).build();


    //simula~~ao
    Books updatedBook=createValidBook();
    updatedBook.setId(id);
    Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);


    //execução -- não pode lançar exceções
    Books book=service.update(updatingBook);

    //verificações - Verifica se no repository foi chamado 1vz o metodo delete
    assertThat(book.getId()).isEqualTo(updatedBook.getId());
    assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
    assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
    assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());



  }

  @Test
  @DisplayName("Deve filtar pelas propriedades")
  public void findBookTest() {

    //cenário
    Books book=createValidBook();
    List<Books> lista=Arrays.asList(book);
    Page<Books> page=new PageImpl<Books>(lista, PageRequest.of(0,10),1);

    Mockito.when(repository.findAll(Mockito.any(Example.class),Mockito.any(PageRequest.class))).thenReturn(page);

    //execução
    Page<Books> result=service.find(book,PageRequest.of(0,10));


    //verificações
    assertThat(result.getTotalElements()).isEqualTo(1);//la no cenário eu passei 1
    assertThat(result.getContent()).isEqualTo(lista); //la no cenário eu passei uma lista
    assertThat(result.getPageable().getPageNumber()).isEqualTo(0); //lá no cenário eu passei 0
    assertThat(result.getPageable().getPageSize()).isEqualTo(10); //la no cenário eu passei 10
  }

  @Test
  @DisplayName("Deve obter um livro pelo ISBN")
  public void getBookByIsbnTest(){
    String isbn="1230";

    //aqui faz o mock colocando no contexto o thenReturn
    Mockito.when( repository.findByisbn(isbn)).thenReturn(Optional.of(Books.builder().id(1l).isbn(isbn).build()));

    Optional<Books> book=service.getByIsbn(isbn);

    assertThat(book.isPresent()).isTrue();
    assertThat(book.get().getId()).isEqualTo(1l);
    assertThat(book.get().getIsbn()).isEqualTo(isbn);

    verify(repository,times(1)).findByisbn(isbn);
  }

}
