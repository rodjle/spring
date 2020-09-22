package com.udemy.demo.service;

import com.udemy.demo.api.model.entity.Books;
import com.udemy.demo.exception.BusinessException;
import com.udemy.demo.model.repository.BooksRepository;
import java.util.Optional;
import org.apache.catalina.Store;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BooksServiceImpl implements BookService {

  private BooksRepository repository;

  public BooksServiceImpl(BooksRepository repository) {
    this.repository=repository;
  }

  @Override
  public Books save(Books book) {
    if (repository.existsByIsbn(book.getIsbn())) {
      throw new BusinessException("Isbn Já Cadastrado");
    }
    return repository.save(book);
  }

  @Override
  public Optional<Books> getById(Long id) {
    return this.repository.findById(id);
  }

  @Override
  public void delete(Books book) {
    if (book==null || book.getId()==null){
      throw new IllegalArgumentException("Bok id can't be null");
    }
      this.repository.delete(book);

  }

  @Override
  public Books update(Books book) {
    if (book==null || book.getId()==null){
      throw new IllegalArgumentException("Bok id can't be null");
    }
    return this.repository.save(book);


  }

  @Override
  public Page<Books> find(Books filter, Pageable pageRequest) {
    Example<Books> example=Example.of(filter,
        ExampleMatcher.matching()
        .withIgnoreCase()
        .withIgnoreNullValues()
        .withStringMatcher(StringMatcher.CONTAINING)
        );
    return repository.findAll(example,pageRequest);
  }

  @Override
  public Optional<Books> getByIsbn(String isbn) {
    return Optional.empty();
  }


}
