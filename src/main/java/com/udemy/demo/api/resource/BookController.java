package com.udemy.demo.api.resource;

import com.udemy.demo.api.dto.BookDTO;
import com.udemy.demo.api.dto.LoanDTO;
import com.udemy.demo.api.model.entity.Books;
import com.udemy.demo.api.model.entity.Loan;
import com.udemy.demo.service.BookService;
import com.udemy.demo.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book Api - Testando")
@Slf4j
public class BookController {

  private final BookService service;
  private final LoanService loanService;
  private final  ModelMapper modelMapper;





  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookDTO create(@RequestBody @Valid BookDTO dto) {
    //Book book=Book.bui
    log.info("Create a book for isbn agora automatizado com heroku: {}",dto.getIsbn());
    Books entity = modelMapper.map(dto, Books.class);
    entity = service.save(entity);

    return modelMapper.map(entity, BookDTO.class);
  }

  @GetMapping("{id}")
  public BookDTO get(@PathVariable Long id) {
    return service
        .getById(id)
        .map(book -> modelMapper.map(book, BookDTO.class))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));


  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    Books book = service.getById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    service.delete(book);


  }

  @PutMapping("{id}")
  public BookDTO update(@PathVariable Long id, BookDTO dto) {
    return service
        .getById(id).map(book -> {
          book.setAuthor(dto.getAuthor());
          book.setTitle(dto.getTitle());
          book = service.update(book);
          return modelMapper.map(book, BookDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

  }

  //observa que tem 2 get - Spring encaixa os parametros com o metodo correspondente
  @GetMapping
  @ApiOperation("Procura um livro")
  @ApiResponses({
       @ApiResponse(code=204,message="Livro não localizado")
  })
  public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
    System.out.println(dto.getAuthor() + "" + dto.getIsbn());
    Books filter = modelMapper.map(dto, Books.class);
    Page<Books> result = service.find(filter, pageRequest);
    List<BookDTO> list = result.getContent()
        .stream()
        .map(entity -> modelMapper.map(entity, BookDTO.class))
        .collect(Collectors.toList());

    return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
  }


  @GetMapping("{id}/loans")
  public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable) {

      Books book = service.getById(id)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

     Page<Loan> result= loanService.getLoansByBook(book,pageable);

    List<LoanDTO> loans=result.getContent().stream()
        .map(entity->{
          Books loanBook=entity.getBook();
          BookDTO bookDTO=modelMapper.map(loanBook,BookDTO.class);//isso evita de mandar todo book , somente aquilo que é preciso
          LoanDTO loanDTO=modelMapper.map(entity,LoanDTO.class);
          loanDTO.setBook(bookDTO);
          return loanDTO;
        }).collect(Collectors.toList());

    return new PageImpl<LoanDTO>(loans,pageable, result.getTotalElements());

  }


}
