package com.example.Homebank.businessLogic.email;

import com.example.Homebank.presentation.ApiPaths;
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

    @Value("${application.url}")
    private String applicationURL;

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

    /**
     * Sends an e-mail with a link to confirm that the user wants to recover their account.
     *
     * @param targetAddress Address of the recipient.
     * @param recoveryToken Generated token used to confirm the recovery request.
     */
    public void sendRecoveryURLEmail(String targetAddress, String recoveryToken) {
        logger.info("Sending a recovery e-mail to: {}", targetAddress);
        String recoveryURL = applicationURL + ApiPaths.USERS + ApiPaths.ACCOUNT_RECOVERY + "?recoveryToken=" + recoveryToken;

        sendEmail(targetAddress, "Homebank - Account recovery confirmation", recoveryURL);
    }
}
