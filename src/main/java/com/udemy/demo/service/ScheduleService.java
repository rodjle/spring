package com.udemy.demo.service;

import com.udemy.demo.api.model.entity.Loan;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
  private static final String CRON_LATE_LOAN="0 0 0 1/1 * *";

  private final LoanService loanService;

  private final EmailService emailService;

  @Value("${applcation.mail.lateloans.message")
  private String msg;

  public ScheduleService(LoanService loanService, EmailService emailService) {
    this.loanService = loanService;
    this.emailService = emailService;
  }


  @Scheduled(cron= CRON_LATE_LOAN)
  public void sendMailToLateLoan(){
    List<Loan> allLateLoans=loanService.getAllLateLoans();
    List<String> mailList=allLateLoans.stream()
                         .map(loan-> loan.getCustomerEmail())
                          .collect(Collectors.toList());

    emailService.sendEmails(msg,mailList);
  }

}
