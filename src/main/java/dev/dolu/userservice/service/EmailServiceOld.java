package dev.dolu.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceOld {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailServiceOld(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTestEmail(String to) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom("service@qorelabs.org"); // Match the authenticated email
        helper.setTo(to);
        helper.setSubject("Test Email from Spring");
        helper.setText("Hello from Spring! This is a test email.", true);

        mailSender.send(message);
    }

    public void sendVerificationEmail(String to, String verificationUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom("service@qorelabs.org"); // Match the authenticated email
        helper.setTo(to);
        helper.setSubject("Verify Your Email");
        helper.setText("<p>Please verify your email by clicking on the link below:</p>" +
                "<a href=\"" + verificationUrl + "\">Verify Email</a>", true);

        mailSender.send(message);
    }
}
