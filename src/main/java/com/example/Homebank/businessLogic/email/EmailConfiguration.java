package com.example.Homebank.businessLogic.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfiguration {
    @Value("${email.host}")
    private String emailHost;

    @Value("${email.port}")
    private int emailPort;

    @Value("${email.app.username}")
    private String appUsername;

    @Value("${email.app.password}")
    private String appPassword;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        try {
            mailSender.setHost(emailHost);
            mailSender.setPort(emailPort);
            mailSender.setUsername(appUsername);
            mailSender.setPassword(appPassword);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mailSender;
    }

}
