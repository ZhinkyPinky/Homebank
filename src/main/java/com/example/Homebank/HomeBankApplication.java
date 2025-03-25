package com.example.Homebank;

import com.example.Homebank.businessLogic.email.EmailService;
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

        /*
        UserRepository userRepository = configurableApplicationContext.getBean(UserRepository.class);
        List<UserEntity> users = userRepository.findAll();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        users.forEach((user -> {
            user.setPassword(encoder.encode(user.getPassword()));
            userRepository.save(user);
        }));
         */


//        byte[] secret = new byte[64];
//        SecureRandom secureRandom = new SecureRandom();
//        secureRandom.nextBytes(secret);
//        String base64Secret = Base64.getEncoder().encodeToString(secret);
//        System.out.println(base64Secret);
    }

}
