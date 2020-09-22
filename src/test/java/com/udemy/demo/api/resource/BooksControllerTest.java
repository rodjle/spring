package com.udemy.demo.api.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udemy.demo.api.dto.BookDTO;
import com.udemy.demo.api.model.entity.Books;
import com.udemy.demo.exception.BusinessException;
import com.udemy.demo.service.BookService;

import java.util.Arrays;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
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
@WebMvcTest (controllers = BookController.class)//só carrega o BookController
@AutoConfigureMockMvc //configura um objetivo para fazer requisições
public class BooksControllerTest {
  static String BOOK_API="/api/books";

  @Autowired
  MockMvc mvc;//simula as requisições

  @MockBean
  BookService service;

  @Test
  @DisplayName("Criar um livro com sucesso")
  public void createBookTest() throws Exception{
    BookDTO dto = createNewBook();
    Books savedBook= Books.builder().id(10l).author("Artur").title("As aventuras do rei").isbn("001").build();
    BDDMockito.given(service.save(Mockito.any(Books.class))).willReturn(savedBook);


    String json=new ObjectMapper().writeValueAsString(dto);

    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
         .post(BOOK_API)
         .contentType(MediaType.APPLICATION_JSON)
         .accept(MediaType.APPLICATION_JSON)
         .content(json);

    mvc
        .perform(request)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("id").isNotEmpty())
        .andExpect(jsonPath("title").value(dto.getTitle()))
        .andExpect(jsonPath("author").value(dto.getAuthor()))
        .andExpect(jsonPath("isbn").value(dto.getIsbn()))
        ;

  }

  private BookDTO createNewBook() {
    BookDTO dto= BookDTO.builder().
                 author("Artur").title("As aventuras do rei").isbn("001").build();
    return dto;
  }


  @Test
  @DisplayName("Deve lançar erro de validação quando não houve dados suficiente para criação do livro")
  public void createInvalidBookTest() throws Exception{
    String json=new ObjectMapper().writeValueAsString(new BookDTO());

    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .post(BOOK_API)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json);

    mvc.perform(request)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("errors", hasSize(3)));
  }


  @Test
  @DisplayName("Lançar erro ao cadastrar livro com isbn duplicado")
  public void createBookWithDuplicatedIsbn() throws Exception{
    BookDTO savedBook=createNewBook();
    String json=new ObjectMapper().writeValueAsString(savedBook);
    String isbn_já_cadastrado = "Isbn Já Cadastrado";
    BDDMockito.given(service.save(Mockito.any(Books.class))).willThrow(new BusinessException(
        isbn_já_cadastrado));

    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .post(BOOK_API)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json);

    mvc.perform(request)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("errors", hasSize(1)))
        .andExpect(jsonPath("errors[0]").value(isbn_já_cadastrado));
  }

  @Test
  @DisplayName("Deve buscar informações de um livro")
  public void getBookDetailsTest() throws Exception {
    //cenário (diven)
    Long id=1l;
    Books book=Books.builder().id(id).title(createNewBook().getTitle()).author(
        createNewBook().getAuthor()).isbn(createNewBook().getIsbn()).build();
    BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

    //execução
    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .get(BOOK_API.concat("/"+id))
        .accept(MediaType.APPLICATION_JSON);

    //verificação
    mvc
         .perform(request)
        .andExpect(status().isOk())
        .andExpect(jsonPath("id").value(id))
        .andExpect(jsonPath("title").value(createNewBook().getTitle()))
        .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
        .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()))
    ;
  }


  @Test
  @DisplayName("Deve retornar not found quando o livro procurado não existir")
  public void getBookNotFoundTest() throws Exception {
    //cenário (given)
    BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

    //execução
    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .get(BOOK_API.concat("/"+1))
        .accept(MediaType.APPLICATION_JSON);

    //verificação
    mvc
        .perform(request)
        .andExpect(status().isNotFound())
    ;
  }

  @Test
  @DisplayName("Deve Deletar um livro")
  public void deleteBookTest() throws Exception {
    //cenário (given)
    BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Books.builder().id(1l).build()));

    //execução
    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .delete(BOOK_API.concat("/"+1))
        .accept(MediaType.APPLICATION_JSON);

    //verificação
    mvc
        .perform(request)
        .andExpect(status().isNoContent())
    ;
  }

  @Test
  @DisplayName("Deve retornar resource not found quando nao achar livro pra deletar")
  public void deleteBookNotFound() throws Exception {
    //cenário (given)
    BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

    //execução
    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .delete(BOOK_API.concat("/"+1))
        .accept(MediaType.APPLICATION_JSON);

    //verificação
    mvc
        .perform(request)
        .andExpect(status().isNotFound())
    ;
  }

  @Test
  @DisplayName("Deve atualizar um levro")
  public void updateBook() throws Exception {
    //cenário (given)
    Long id=1l;

    Books updatingBook=Books.builder().id(id).title("some title").isbn("12345").author("eu").build();
    Books updatedBook=Books.builder().id(id).title(createNewBook().getTitle()).isbn(createNewBook().getIsbn()).author(createNewBook().getAuthor()).build();
    BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));
    BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);


    String json=new ObjectMapper().writeValueAsString(createNewBook());

    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .put(BOOK_API.concat("/"+id))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json);

    mvc
        .perform(request)
        .andExpect(status().isOk())
        .andExpect(jsonPath("id").value(id))
        .andExpect(jsonPath("title").value(createNewBook().getTitle()))
        .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
        .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()))
    ;
  }


  @Test
  @DisplayName("Deve retornar resource not found quando nao achar livro pra atualizar")
  public void updateBookNotFound() throws Exception {
    BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());


    String json=new ObjectMapper().writeValueAsString(createNewBook());

    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .put(BOOK_API.concat("/"+1))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json);

    mvc
        .perform(request)
        .andExpect(status().isNotFound());
  }


  @Test
  @DisplayName("Deve filtrar livros")
  public void findBookTest() throws Exception{
    Long id=1l;

    Books book= Books.builder().id(id).title(createNewBook().getTitle()).author(
        createNewBook().getAuthor()).isbn(createNewBook().getIsbn()).build();

    //busca qualquer livro, passando qualquer página e deve retorna um pagina de livros 0-100
    BDDMockito.given(service.find(Mockito.any(Books.class), Mockito.any(Pageable.class)))
        .willReturn(new PageImpl<Books>(Arrays.asList(book), PageRequest.of(0,100),1));

    String queryString=String.format("?title=%s&author=%s&page=0&size=100",book.getTitle(),book.getAuthor());



    //execução
    MockHttpServletRequestBuilder request=MockMvcRequestBuilders
        .get(BOOK_API.concat("/"+queryString))
        .accept(MediaType.APPLICATION_JSON);

    //verificação
    mvc
        .perform(request)
        .andExpect(status().isOk())
        .andExpect(jsonPath("content", Matchers.hasSize(1)))
        .andExpect(jsonPath("totalElements").value(1))
        .andExpect(jsonPath("pageable.pageSize").value(100))
        .andExpect(jsonPath("pageable.pageNumber").value(0))
    ;
  }




}
