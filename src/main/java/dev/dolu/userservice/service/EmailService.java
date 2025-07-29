package dev.dolu.userservice.service;

import dev.dolu.userservice.metrics.CustomMetricService;
import dev.dolu.userservice.models.EmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final RestTemplate restTemplate;
    private final CustomMetricService customMetricService;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${email.service.api-key}")
    private String apiKey;

    @Value("${bulk.email.service.url}")
    private String bulkEmailServiceUrl;

    // Shared executor for virtual threads using JDK 21's approach
    private final ExecutorService virtualThreadExecutor =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

    public EmailService(RestTemplate restTemplate, CustomMetricService customMetricService) {
        this.restTemplate = restTemplate;
        this.customMetricService = customMetricService;
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

    // 🟡 Function 4: Send Zenest Verification Code Email
    public boolean sendZenestVerificationEmail(String to, String code) {
        String subject = "Welcome to Zennest – Your Verification Code";
        String content = "<!DOCTYPE html>" +
                "<html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Welcome to Zennest</title>" +
                "<style>body{font-family:Arial,sans-serif;background:#f9f9f9;margin:0;padding:0;}" +
                ".container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.1);}" +
                ".header{text-align:center;font-size:24px;font-weight:bold;color:#333;padding-bottom:10px;}" +
                ".content{font-size:16px;color:#555;line-height:1.5;}" +
                ".code{display:block;margin:20px auto;font-size:32px;font-weight:bold;color:#007bff;text-align:center;}" +
                ".footer{text-align:center;font-size:12px;color:#888;padding-top:15px;}" +
                "</style></head><body><div class='container'>" +
                "<div class='header'>Welcome to Zennest!</div>" +
                "<div class='content'>" +
                "<p>Hi there 👋,</p>" +
                "<p>We’re thrilled to have you aboard. To complete your registration and start renting with peace of mind, please use the verification code below:</p>" +
                "<span class='code'>" + code + "</span>" +
                "<p>This code expires in 5 minutes. If you did not request this, please ignore this email.</p>" +
                "<p class='footer'>&copy; 2025 Zennest. All rights reserved.</p>" +
                "</div></body></html>";
        return sendEmail(to, subject, content);
    }

    // 🟣 Function 5: Send Zenest Resend Verification Code Email
    public boolean sendZenestResendVerificationEmail(String to, String code) {
        String subject = "Your New Zennest Verification Code";
        String content = "<!DOCTYPE html>" +
                "<html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Your New Zennest Code</title>" +
                "<style>body{font-family:Arial,sans-serif;background:#f9f9f9;margin:0;padding:0;}" +
                ".container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.1);}" +
                ".header{text-align:center;font-size:24px;font-weight:bold;color:#333;padding-bottom:10px;}" +
                ".content{font-size:16px;color:#555;line-height:1.5;}" +
                ".code{display:block;margin:20px auto;font-size:32px;font-weight:bold;color:#007bff;text-align:center;}" +
                ".footer{text-align:center;font-size:12px;color:#888;padding-top:15px;}" +
                "</style></head><body><div class='container'>" +
                "<div class='header'>Your New Verification Code</div>" +
                "<div class='content'>" +
                "<p>Hi there 👋,</p>" +
                "<p>This is your new verification code for Zennest. Enter it in the app to complete your verification:</p>" +
                "<span class='code'>" + code + "</span>" +
                "<p>This code expires in 5 minutes. If you did not request a new code, please ignore this email.</p>" +
                "<p class='footer'>&copy; 2025 Zennest. All rights reserved.</p>" +
                "</div></body></html>";
        return sendEmail(to, subject, content);
    }

    public boolean sendPasswordResetEmail(String to, String code) {
        String subject = "Zennest Password Reset Code";
        String content = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Password Reset</title>" +
                "    <style>" +
                "        .container { max-width: 600px; margin: 20px auto; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                "        .header { text-align: center; font-size: 24px; font-weight: bold; color: #333; padding-bottom: 10px; }" +
                "        .footer { text-align: center; font-size: 12px; color: #888; padding-top: 15px; }" +
                "        .content { font-size: 16px; color: #555; line-height: 1.5; text-align: center; }" +
                "        .code { display: block; margin: 20px auto; font-size: 32px; font-weight: bold; color: #007bff; text-align: center; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>Zennest Password Reset</div>" +
                "        <div class='content'>" +
                "            <p>We received a request to reset your Zennest password.</p>" +
                "            <p>Use the code below to reset your password:</p>" +
                "            <span class='code'>" + code + "</span>" +
                "            <p>This code expires in 5 minutes. If you did not request a password reset, please ignore this email.</p>" +
                "        </div>" +
                "        <div class='footer'>&copy; 2025 Zennest. All rights reserved.</div>" +
                "    </div>" +
                "</body>" +
                "</html>";
        return sendEmail(to, subject, content);
    }


    // 🚀 Function 3: Generic Email Sending Function (Used Internally)
    public boolean sendEmail(String to, String subject, String content) {
        long startTime = System.currentTimeMillis();
        try {
            boolean result = CompletableFuture.supplyAsync(() -> {
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
                // Increment sent counter if successful
                customMetricService.incrementEmailSentCounter();
                return response.getStatusCode().is2xxSuccessful();
            }, virtualThreadExecutor).get();

            long duration = System.currentTimeMillis() - startTime;
            customMetricService.recordEmailSendingTime(duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            customMetricService.recordEmailSendingTime(duration);
            logger.error("Failed to send email: {}", e.getMessage(), e);
            customMetricService.incrementEmailFailureCounter();
            return false;
        }
    }

    // 🚀 Bulk Email Sending Function
    public boolean sendBulkEmail(List<String> recipients, String subject, String content) {
        long startTime = System.currentTimeMillis();
        try {
            boolean result = CompletableFuture.supplyAsync(() -> {
                // Build bulk email request payload
                Map<String, Object> bulkRequest = Map.of(
                        "recipients", recipients,
                        "subject", subject,
                        "content", content
                );

                // Set headers with API key
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                headers.set("Authorization", "Bearer " + apiKey);

                // Create HTTP request entity
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bulkRequest, headers);

                // Send HTTP request to external email microservice bulk endpoint
                ResponseEntity<String> response = restTemplate.exchange(
                        bulkEmailServiceUrl,
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );
                // Increment sent counter if successful
                customMetricService.incrementEmailSentCounter();
                return response.getStatusCode().is2xxSuccessful();
            }, virtualThreadExecutor).get();

            long duration = System.currentTimeMillis() - startTime;
            customMetricService.recordEmailSendingTime(duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            customMetricService.recordEmailSendingTime(duration);
            logger.error("Failed to send bulk email: {}", e.getMessage(), e);
            customMetricService.incrementEmailFailureCounter();
            return false;
        }
    }
}