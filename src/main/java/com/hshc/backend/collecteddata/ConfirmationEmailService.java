package com.hshc.backend.collecteddata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmationEmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public ConfirmationEmailService(JavaMailSender mailSender,
                                    @Value("${app.mail.from-address:no-reply@homesweethomecare.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public boolean sendConfirmation(CollectedData data) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(data.getEmail());
        message.setSubject("We received your Home Sweet Home Care request");
        message.setText("Hello " + data.getFullName() + ",\n\n" +
                "Thank you for contacting Home Sweet Home Care. " +
                "Our team has received your message and will follow up shortly.\n\n" +
                "Your message:\n" + data.getMessage() + "\n\n" +
                "Best,\nHome Sweet Home Care");

        try {
            mailSender.send(message);
            return true;
        } catch (MailException ex) {
            logger.warn("Unable to send confirmation email for submission {}", data.getId(), ex);
            return false;
        }
    }
}
