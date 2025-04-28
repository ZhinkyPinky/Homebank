package com.example.Homebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class HomeBankApplication {
    private static ConfigurableApplicationContext configurableApplicationContext;

    public HomeBankApplication(ConfigurableApplicationContext configurableApplicationContext) {
        HomeBankApplication.configurableApplicationContext = configurableApplicationContext;
    }

    public static void main(String[] args) {
        configurableApplicationContext = SpringApplication.run(HomeBankApplication.class, args);
    }

}
