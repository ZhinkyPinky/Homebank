package com.example.Homebank;

import com.example.Homebank.businessLogic.services.TransactionHeadService;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import com.example.Homebank.dataAccess.repositories.TransactionHeadRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@SpringBootApplication
public class HomebankApplication {
    private static ConfigurableApplicationContext configurableApplicationContext;

    public HomebankApplication(ConfigurableApplicationContext configurableApplicationContext) {
        HomebankApplication.configurableApplicationContext = configurableApplicationContext;
    }

    public static void main(String[] args) {
        configurableApplicationContext = SpringApplication.run(HomebankApplication.class, args);
    }

}
