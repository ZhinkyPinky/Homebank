package com.example.Homebank;

import io.jsonwebtoken.Jwts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.security.SecureRandom;
import java.util.Base64;

@SpringBootApplication
public class HomeBankApplication {
    private static ConfigurableApplicationContext configurableApplicationContext;

    public HomeBankApplication(ConfigurableApplicationContext configurableApplicationContext) {
        HomeBankApplication.configurableApplicationContext = configurableApplicationContext;
    }

    public static void main(String[] args) {
        configurableApplicationContext = SpringApplication.run(HomeBankApplication.class, args);

//        byte[] secret = new byte[64];
//        SecureRandom secureRandom = new SecureRandom();
//        secureRandom.nextBytes(secret);
//        String base64Secret = Base64.getEncoder().encodeToString(secret);
//        System.out.println(base64Secret);
    }

}
