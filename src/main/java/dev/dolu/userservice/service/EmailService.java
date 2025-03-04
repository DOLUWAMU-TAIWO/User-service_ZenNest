package dev.dolu.userservice.service;

import dev.dolu.userservice.models.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmailService {

    private final RestTemplate restTemplate;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${email.service.api-key}")
    private String apiKey;

    // Shared executor for virtual threads using JDK 21's approach
    private final ExecutorService virtualThreadExecutor =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 🟢 Function 1: Send Test Email (General Purpose)
    public boolean sendTestEmail(String to) {
        String subject = "Test Email";
        String content = "<h1>Hello!</h1><p>This is a test email from our service.</p>";
        return sendEmail(to, subject, content);
    }

    // 🔵 Function 2: Send Verification Email (For New Users)
    public boolean sendVerificationEmail(String to, String verificationLink) {
        String subject = "Verify Your Email - QoreLabs";

        String content = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Email Verification</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                "        .container { width: 100%; max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1); }" +
                "        .header { text-align: center; padding: 20px 0; font-size: 20px; font-weight: bold; color: #333; }" +
                "        .content { text-align: center; font-size: 16px; color: #333; line-height: 1.5; padding: 20px; }" +
                "        .btn { display: inline-block; padding: 12px 20px; margin: 20px 0; font-size: 16px; color: #fff; background-color: #007bff; text-decoration: none; border-radius: 5px; }" +
                "        .btn:hover { background-color: #0056b3; }" +
                "        .footer { text-align: center; font-size: 14px; color: #777; padding: 10px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>QoreLabs</div>" +
                "        <div class='content'>" +
                "            <h2>Email Verification</h2>" +
                "            <p>Thank you for signing up! Please verify your email address to activate your account.</p>" +
                "            <a href='" + verificationLink + "' class='btn'>Verify Your Email</a>" +
                "            <p>If the button above does not work, copy and paste the link below into your browser:</p>" +
                "            <p><a href='" + verificationLink + "'>" + verificationLink + "</a></p>" +
                "        </div>" +
                "        <div class='footer'>&copy; 2025 QoreLabs. All Rights Reserved.</div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        return sendEmail(to, subject, content);
    }

    // 🚀 Function 3: Generic Email Sending Function (Used Internally)
    private boolean sendEmail(String to, String subject, String content) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                // Create email request DTO
                EmailRequest emailRequest = new EmailRequest(to, subject, content);

                // Set headers with API key
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", "Bearer " + apiKey);

                // Create HTTP request entity
                HttpEntity<EmailRequest> requestEntity = new HttpEntity<>(emailRequest, headers);

                // Send HTTP request to external email microservice
                ResponseEntity<String> response = restTemplate.exchange(
                        emailServiceUrl,
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                // Return true if email was successfully sent
                return response.getStatusCode().is2xxSuccessful();
            }, virtualThreadExecutor).get();
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }
}