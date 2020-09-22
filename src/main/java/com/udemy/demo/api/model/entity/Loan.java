package com.udemy.demo.api.model.entity;


import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
  private Long id;
  private String customer;
  private Books book;
  private LocalDate loanDate;
  private Boolean returned;
}
