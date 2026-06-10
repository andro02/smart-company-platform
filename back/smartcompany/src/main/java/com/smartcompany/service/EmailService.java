package com.smartcompany.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${email.subject}")
    private String emailSubject;

    @Value("${email.message}")
    private String emailMessage;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendActivationEmail(String toEmail, String activationLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(toEmail);
            message.setSubject(emailSubject);
            message.setText(emailMessage + "\n\n" + activationLink);

            mailSender.send(message);
            System.out.println(message.toString());
        } catch (MailException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    public void sendInvoice(String toEmail, byte[] pdfBytes, Long orderId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(toEmail);
            helper.setSubject("Faktura #" + orderId + " - Smart Company");
            helper.setText("""
                <html>
                <body>
                    <h2>Hvala na porudžbini!</h2>
                    <p>U prilogu se nalazi faktura za vašu porudžbinu broj <strong>#%d</strong>.</p>
                    <p>Srdačan pozdrav,<br/>Smart Company tim</p>
                </body>
                </html>
                """.formatted(orderId), true);

            helper.addAttachment("faktura-" + orderId + ".pdf",
                new org.springframework.core.io.ByteArrayResource(pdfBytes),
                "application/pdf");

            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send invoice email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Greška pri slanju fakture: " + e.getMessage(), e);
        }
    }
    
}
