package com.udemy.demo.api.resource;

import com.udemy.demo.api.dto.LoanDTO;
import com.udemy.demo.api.model.entity.Books;
import com.udemy.demo.api.model.entity.Loan;
import com.udemy.demo.service.BookService;
import com.udemy.demo.service.LoanService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
  private final LoanService service;
  private final BookService bookService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Long create( @RequestBody LoanDTO dto){
    Books book=bookService.getByIsbn(dto.getIsbn())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Book not found for passed isbn"));
    Loan entity=Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build();

    entity=service.save(entity);
    return entity.getId();
  }
}
