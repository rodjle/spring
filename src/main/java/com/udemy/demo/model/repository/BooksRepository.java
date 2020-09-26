package com.udemy.demo.model.repository;

import com.udemy.demo.api.model.entity.Books;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BooksRepository extends JpaRepository<Books,Long> {

  boolean existsByIsbn(String isbn);

  Optional<Books> findByisbn(String isbn);
}
