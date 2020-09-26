package com.udemy.demo.service;

import com.udemy.demo.api.dto.LoanFilterDTO;
import com.udemy.demo.api.model.entity.Loan;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanService {

  Loan save(Loan loan);


  Optional<Loan> getById(Long id);

  Loan update(Loan loan);

  Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable);
}
