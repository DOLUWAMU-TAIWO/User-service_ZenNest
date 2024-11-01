package dev.dolu.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // SMTP server settings
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        // Email credentials from application.properties
        mailSender.setUsername("doluwamua@gmail.com");
        mailSender.setPassword("vzridziyubuiiizq");

        // Additional properties for secure email transmission
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false"); // Set to true if you need debugging output

        return mailSender;
    }
}
