package com.udemy.demo.service;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {

  public void sendEmails(String msg, List<String> mailList) ;

}
