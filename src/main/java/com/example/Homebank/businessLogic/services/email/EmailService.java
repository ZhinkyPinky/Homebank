package com.example.Homebank.businessLogic.services.email;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${email.app.username}")
    private String appUsername;


    private final JavaMailSender emailSender;

    /**
     * Sends an e-mail to the specified address.
     *
     * @param targetAddress Address of the recipient.
     * @param subject       Subject of the e-mail.
     * @param text          Content of the e-mail.
     */
    public void sendEmail(String targetAddress, String subject, String text) {
        logger.info("Sending an e-mail to: {}", targetAddress);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(appUsername);
        message.setTo(targetAddress);
        message.setSubject(subject);
        message.setText(text);

        emailSender.send(message);

        logger.info("E-mail sent to: {}", targetAddress);
    }
}
