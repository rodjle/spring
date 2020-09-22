package com.udemy.demo.service;

import com.udemy.demo.api.model.entity.Books;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

  Books save(Books any);

  Optional<Books> getById(Long id);

  void delete(Books book);

  Books update(Books book);

  Page<Books> find(Books filter, Pageable pageRequest);

  Optional<Books> getByIsbn(String isbn);
}
