package com.udemy.demo;

import com.udemy.demo.service.EmailService;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {
  @Bean
  public ModelMapper modelMapper(){
    return new ModelMapper();
  }



  @Scheduled(cron="0 0/1 * 1/1 * ?")
  public void testeCron(){
    System.out.println("Agendamento tarefas");
  }


//  //ao subir a aplicação rodará qualquer coisa ao iniciar que está abaixo
//  @Bean
//  public CommandLineRunner runner(){
//    return args -> {
//      List<String>  emails= Arrays.asList("library-api-a54154@inbox.mailtrap.io");
//      emailService.sendEmails("Testando emails",emails);
//      System.out.println("Emails enviados!!!");
//    };
//  }

  public static void main (String[] args){

    SpringApplication.run(LibraryApiApplication.class);
  }
}
