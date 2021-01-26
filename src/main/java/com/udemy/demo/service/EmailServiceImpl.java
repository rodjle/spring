package com.udemy.demo.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{
  @Autowired
private JavaMailSender javaMailSender;

  @Override
  public void sendEmails(String msg, List<String> mailList) {
    String[] mails = mailList.toArray(new String[mailList.size()]);
    SimpleMailMessage mailMessage=new SimpleMailMessage();
    mailMessage.setFrom("library-api-a54154@inbox.mailtrap.io");
    mailMessage.setSubject("Livro com empréstimo atrasado");
    mailMessage.setText(msg);

    mailMessage.setTo(mails);

    javaMailSender.send(mailMessage);
  }
}
