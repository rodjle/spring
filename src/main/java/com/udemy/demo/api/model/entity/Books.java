package com.udemy.demo.api.model.entity;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Books {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column
  private Long id;

  private String title;
  private String author;
  private String isbn;

  @OneToMany(mappedBy = "book")
  private List<Loan> loans;
}
