package dev.dolu.userservice.service;

import dev.dolu.userservice.metrics.CustomMetricService;
import dev.dolu.userservice.models.EmailRequest;
import dev.dolu.userservice.utils.JwtUtils;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final RestTemplate restTemplate;
    private final CustomMetricService customMetricService;
    private final JwtUtils jwtUtils;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${email.service.api-key}")
    private String apiKey;

    @Value("${bulk.email.service.url}")
    private String bulkEmailServiceUrl;

    // Magic Link Configuration
    @Value("${magic.link.base.url}")
    private String baseUrl;

    @Value("${magic.link.landlord.dashboard.path}")
    private String landlordDashboardPath;

    @Value("${magic.link.landlord.create.listing.path}")
    private String landlordCreateListingPath;

    @Value("${magic.link.tenant.search.path}")
    private String tenantSearchPath;

    // Shared executor for virtual threads using JDK 21's approach
    private final ExecutorService virtualThreadExecutor =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

    public EmailService(RestTemplate restTemplate, CustomMetricService customMetricService, JwtUtils jwtUtils) {
        this.restTemplate = restTemplate;
        this.customMetricService = customMetricService;
        this.jwtUtils = jwtUtils;
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

    // 🎉 Function: Send Profile Completion Congratulations Email for Tenants
    public boolean sendTenantProfileCompletionEmail(String to, String firstName) {
        String subject = "🎉 Welcome to Zennest, " + firstName + "! Your profile is ready";
        String content = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Profile Complete - Welcome to Zennest</title>" +
                "    <style>" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.2); }" +
                "        .header { background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); color: white; padding: 30px 20px; text-align: center; }" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: bold; }" +
                "        .header p { margin: 10px 0 0; opacity: 0.9; font-size: 16px; }" +
                "        .content { padding: 40px 30px; }" +
                "        .welcome-message { font-size: 18px; color: #1f2937; margin-bottom: 25px; line-height: 1.6; }" +
                "        .feature-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 30px 0; }" +
                "        .feature-card { background: #f8fafc; border-radius: 12px; padding: 20px; border-left: 4px solid #4f46e5; }" +
                "        .feature-card h3 { color: #1f2937; margin: 0 0 8px; font-size: 16px; font-weight: 600; }" +
                "        .feature-card p { color: #6b7280; margin: 0; font-size: 14px; }" +
                "        .cta-button { display: block; width: fit-content; margin: 30px auto 20px; padding: 16px 32px; background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); color: white; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 16px; transition: transform 0.2s; }" +
                "        .cta-button:hover { transform: translateY(-2px); }" +
                "        .footer { background: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; }" +
                "        @media (max-width: 600px) { .feature-grid { grid-template-columns: 1fr; } .content { padding: 30px 20px; } }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🎉 Welcome to Zennest!</h1>" +
                "            <p>Your profile is complete and ready to go</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <div class='welcome-message'>" +
                "                <strong>Hi " + firstName + ",</strong><br><br>" +
                "                Congratulations! You've successfully completed your Zennest profile. You're now ready to discover amazing properties and connect with trusted landlords across Nigeria." +
                "            </div>" +
                "            <div class='feature-grid'>" +
                "                <div class='feature-card'>" +
                "                    <h3>🏠 Browse Properties</h3>" +
                "                    <p>Explore thousands of verified listings in your preferred locations</p>" +
                "                </div>" +
                "                <div class='feature-card'>" +
                "                    <h3>📅 Book Viewings</h3>" +
                "                    <p>Schedule property tours directly with landlords at your convenience</p>" +
                "                </div>" +
                "                <div class='feature-card'>" +
                "                    <h3>💬 Direct Communication</h3>" +
                "                    <p>Chat securely with property owners and get instant responses</p>" +
                "                </div>" +
                "                <div class='feature-card'>" +
                "                    <h3>⭐ Save Favorites</h3>" +
                "                    <p>Create your wishlist and get notified about similar properties</p>" +
                "                </div>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Need help? Contact us at <strong>support@zennest.ng</strong></p>" +
                "            <p>&copy; 2025 Zennest. Making property rental seamless in Nigeria.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        return sendEmail(to, subject, content);
    }

    // 🏠 Function: Send Profile Completion Congratulations Email for Landlords
    public boolean sendLandlordProfileCompletionEmail(String to, String firstName) {
        String subject = "🏠 Welcome to Zennest, " + firstName + "! Start listing your properties";
        String content = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Profile Complete - Welcome Landlord</title>" +
                "    <style>" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%); margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.2); }" +
                "        .header { background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%); color: white; padding: 30px 20px; text-align: center; }" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: bold; }" +
                "        .header p { margin: 10px 0 0; opacity: 0.9; font-size: 16px; }" +
                "        .content { padding: 40px 30px; }" +
                "        .welcome-message { font-size: 18px; color: #1f2937; margin-bottom: 25px; line-height: 1.6; }" +
                "        .feature-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 30px 0; }" +
                "        .feature-card { background: #f0f9ff; border-radius: 12px; padding: 20px; border-left: 4px solid #0ea5e9; }" +
                "        .feature-card h3 { color: #1f2937; margin: 0 0 8px; font-size: 16px; font-weight: 600; }" +
                "        .feature-card p { color: #6b7280; margin: 0; font-size: 14px; }" +
                "        .stats-card { background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; border-radius: 12px; padding: 20px; margin: 20px 0; text-align: center; }" +
                "        .stats-card h3 { margin: 0 0 5px; font-size: 24px; }" +
                "        .stats-card p { margin: 0; opacity: 0.9; }" +
                "        .footer { background: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; }" +
                "        @media (max-width: 600px) { .feature-grid { grid-template-columns: 1fr; } .content { padding: 30px 20px; } }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🏠 Welcome to Zennest!</h1>" +
                "            <p>Your landlord profile is ready - start earning today</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <div class='welcome-message'>" +
                "                <strong>Hi " + firstName + ",</strong><br><br>" +
                "                Congratulations! You've successfully set up your Zennest landlord profile. You're now ready to list your properties and connect with verified tenants across Nigeria." +
                "            </div>" +
                "            <div class='stats-card'>" +
                "                <h3>Join 10,000+ Landlords</h3>" +
                "                <p>Already earning an average of ₦2.5M annually on Zennest</p>" +
                "            </div>" +
                "            <div class='feature-grid'>" +
                "                <div class='feature-card'>" +
                "                    <h3>📋 List Properties</h3>" +
                "                    <p>Create professional listings with photos and detailed descriptions</p>" +
                "                </div>" +
                "                <div class='feature-card'>" +
                "                    <h3>🎯 Reach Quality Tenants</h3>" +
                "                    <p>Connect with verified tenants actively looking for properties</p>" +
                "                </div>" +
                "                <div class='feature-card'>" +
                "                    <h3>📅 Manage Viewings</h3>" +
                "                    <p>Accept or schedule property tours based on your availability</p>" +
                "                </div>" +
                "                <div class='feature-card'>" +
                "                    <h3>💰 Secure Payments</h3>" +
                "                    <p>Get paid safely with our integrated payment system</p>" +
                "                </div>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Need help getting started? Contact us at <strong>support@zennest.ng</strong></p>" +
                "            <p>&copy; 2025 Zennest. Empowering property owners across Nigeria.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        return sendEmail(to, subject, content);
    }

    // 🔗 Magic Link Generation Methods (Production-Ready)
    private String generateMagicLink(UUID userId, String email, String role) {
        try {
            // Generate 30-minute magic token
            String magicToken = jwtUtils.generateMagicLinkToken(userId, email, role);

            if (magicToken == null) {
                logger.warn("Failed to generate magic link token for user: {} with email: {}", userId, email);
                return null;
            }

            // Route to appropriate dashboard based on role
            String path = role.equalsIgnoreCase("LANDLORD") ? landlordCreateListingPath : tenantSearchPath;
            String magicLink = baseUrl + "/" + path + "?token=" + magicToken;

            logger.info("Generated magic link for user {} ({}): path={}", userId, role, path);
            return magicLink;

        } catch (Exception e) {
            logger.error("Failed to generate magic link for user {} with email {}: {}", userId, email, e.getMessage());
            return null;
        }
    }

    private String generateMagicLinkButton(String magicLink, String role) {
        if (magicLink == null) {
            return ""; // No button if magic link generation failed
        }

        String buttonText = role.equalsIgnoreCase("LANDLORD")
            ? "🏠 Create Your First Listing"
            : "🔍 Start Browsing Properties";

        String buttonColor = role.equalsIgnoreCase("LANDLORD") ? "#0ea5e9" : "#4f46e5";

        return String.format("""
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s"
                   style="background: linear-gradient(135deg, %s 0%%, %s 100%%); color: white; padding: 16px 32px; text-decoration: none;
                          border-radius: 8px; font-weight: 600; font-size: 16px; display: inline-block; margin: 10px;
                          transition: transform 0.2s; box-shadow: 0 4px 15px rgba(0,0,0,0.2);">
                    %s →
                </a>
            </div>
            <div style="text-align: center; margin: 20px 0; padding: 15px; background: rgba(79, 70, 229, 0.1); border-radius: 8px; border-left: 4px solid #4f46e5;">
                <p style="margin: 0; font-size: 14px; color: #374151; font-weight: 500;">
                    ✨ <strong>One-click access</strong> - no login required!
                </p>
                <p style="margin: 5px 0 0; font-size: 12px; color: #6b7280;">
                    🔒 Secure link expires in 30 minutes for your protection
                </p>
            </div>
            """,
            magicLink,
            buttonColor,
            role.equalsIgnoreCase("LANDLORD") ? "#3b82f6" : "#7c3aed",
            buttonText);
    }

    // 🎉 Enhanced Function: Send Profile Completion Email for Tenants (with Magic Link)
    public boolean sendTenantProfileCompletionEmailWithMagicLink(String to, String firstName, UUID userId) {
        try {
            // Generate magic link for direct property search access
            String magicLink = generateMagicLink(userId, to, "TENANT");
            String magicButton = generateMagicLinkButton(magicLink, "TENANT");

            String subject = "🎉 Welcome to Zennest, " + firstName + "! Start browsing properties now";
            String content = "<!DOCTYPE html>" +
                    "<html lang='en'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>Profile Complete - Welcome to Zennest</title>" +
                    "    <style>" +
                    "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; padding: 0; }" +
                    "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.2); }" +
                    "        .header { background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); color: white; padding: 30px 20px; text-align: center; }" +
                    "        .header h1 { margin: 0; font-size: 28px; font-weight: bold; }" +
                    "        .header p { margin: 10px 0 0; opacity: 0.9; font-size: 16px; }" +
                    "        .content { padding: 40px 30px; }" +
                    "        .welcome-message { font-size: 18px; color: #1f2937; margin-bottom: 25px; line-height: 1.6; }" +
                    "        .feature-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 30px 0; }" +
                    "        .feature-card { background: #f8fafc; border-radius: 12px; padding: 20px; border-left: 4px solid #4f46e5; }" +
                    "        .feature-card h3 { color: #1f2937; margin: 0 0 8px; font-size: 16px; font-weight: 600; }" +
                    "        .feature-card p { color: #6b7280; margin: 0; font-size: 14px; }" +
                    "        .footer { background: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; }" +
                    "        @media (max-width: 600px) { .feature-grid { grid-template-columns: 1fr; } .content { padding: 30px 20px; } }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='header'>" +
                    "            <h1>🎉 Welcome to Zennest!</h1>" +
                    "            <p>Your profile is complete and ready to go</p>" +
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <div class='welcome-message'>" +
                    "                <strong>Hi " + firstName + ",</strong><br><br>" +
                    "                Congratulations! You've successfully completed your Zennest profile. You're now ready to discover amazing properties and connect with trusted landlords across Nigeria." +
                    "            </div>" +
                    "            " + magicButton + // Insert magic link button here
                    "            <div class='feature-grid'>" +
                    "                <div class='feature-card'>" +
                    "                    <h3>🏠 Browse Properties</h3>" +
                    "                    <p>Explore thousands of verified listings in your preferred locations</p>" +
                    "                </div>" +
                    "                <div class='feature-card'>" +
                    "                    <h3>📅 Book Viewings</h3>" +
                    "                    <p>Schedule property tours directly with landlords at your convenience</p>" +
                    "                </div>" +
                    "                <div class='feature-card'>" +
                    "                    <h3>💬 Direct Communication</h3>" +
                    "                    <p>Chat securely with property owners and get instant responses</p>" +
                    "                </div>" +
                    "                <div class='feature-card'>" +
                    "                    <h3>⭐ Save Favorites</h3>" +
                    "                    <p>Create your wishlist and get notified about similar properties</p>" +
                    "                </div>" +
                    "            </div>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>Need help? Contact us at <strong>support@zennest.africa</strong></p>" +
                    "            <p>&copy; 2025 Zennest. Making property rental seamless in Nigeria.</p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";

            boolean emailSent = sendEmail(to, subject, content);
            if (emailSent) {
                logger.info("Enhanced tenant profile completion email with magic link sent to: {}", to);
            }
            return emailSent;

        } catch (Exception e) {
            logger.error("Failed to send enhanced tenant profile completion email to {}: {}", to, e.getMessage());
            // Fallback to regular email without magic link
            return sendTenantProfileCompletionEmail(to, firstName);
        }
    }

    // 🏠 Enhanced Function: Send Profile Completion Email for Landlords (with Magic Link)
    public boolean sendLandlordProfileCompletionEmailWithMagicLink(String to, String firstName, UUID userId) {
        try {
            // Generate magic link for direct listing creation access
            String magicLink = generateMagicLink(userId, to, "LANDLORD");
            String magicButton = generateMagicLinkButton(magicLink, "LANDLORD");

            String subject = "🏠 Welcome to Zennest, " + firstName + "! Create your first listing now";
            String content = "<!DOCTYPE html>" +
                    "<html lang='en'>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>Profile Complete - Welcome Landlord</title>" +
                    "    <style>" +
                    "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%); margin: 0; padding: 0; }" +
                    "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.2); }" +
                    "        .header { background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%); color: white; padding: 30px 20px; text-align: center; }" +
                    "        .header h1 { margin: 0; font-size: 28px; font-weight: bold; }" +
                    "        .header p { margin: 10px 0 0; opacity: 0.9; font-size: 16px; }" +
                    "        .content { padding: 40px 30px; }" +
                    "        .welcome-message { font-size: 18px; color: #1f2937; margin-bottom: 25px; line-height: 1.6; }" +
                    "        .feature-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 30px 0; }" +
                    "        .feature-card { background: #f0f9ff; border-radius: 12px; padding: 20px; border-left: 4px solid #0ea5e9; }" +
                    "        .feature-card h3 { color: #1f2937; margin: 0 0 8px; font-size: 16px; font-weight: 600; }" +
                    "        .feature-card p { color: #6b7280; margin: 0; font-size: 14px; }" +
                    "        .stats-card { background: linear-gradient(135deg, #10b981 0%, #059669 100%); color: white; border-radius: 12px; padding: 20px; margin: 20px 0; text-align: center; }" +
                    "        .stats-card h3 { margin: 0 0 5px; font-size: 24px; }" +
                    "        .stats-card p { margin: 0; opacity: 0.9; }" +
                    "        .footer { background: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; }" +
                    "        @media (max-width: 600px) { .feature-grid { grid-template-columns: 1fr; } .content { padding: 30px 20px; } }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='header'>" +
                    "            <h1>🏠 Welcome to Zennest!</h1>" +
                    "            <p>Your landlord profile is ready - start earning today</p>" +
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <div class='welcome-message'>" +
                    "                <strong>Hi " + firstName + ",</strong><br><br>" +
                    "                Congratulations! You've successfully set up your Zennest landlord profile. You're now ready to list your properties and connect with verified tenants across Nigeria." +
                    "            </div>" +
                    "            " + magicButton + // Insert magic link button here
                    "            <div class='stats-card'>" +
                    "                <h3>Join 10,000+ Landlords</h3>" +
                    "                <p>Already earning an average of ₦2.5M annually on Zennest</p>" +
                    "            </div>" +
                    "            <div class='feature-grid'>" +
                    "                <div class='feature-card'>" +
                    "                    <h3>📋 List Properties</h3>" +
                    "                    <p>Create professional listings with photos and detailed descriptions</p>" +
                    "                </div>" +
                    "                <div class='feature-card'>" +
                    "                    <h3>🎯 Reach Quality Tenants</h3>" +
                    "                    <p>Connect with verified tenants actively looking for properties</p>" +
                    "                </div>" +
                    "                <div class='feature-card'>" +
                    "                    <h3>📅 Manage Viewings</h3>" +
                    "                    <p>Accept or schedule property tours based on your availability</p>" +
                    "                </div>" +
                    "                <div class='feature-card'>" +
                    "                    <h3>💰 Secure Payments</h3>" +
                    "                    <p>Get paid safely with our integrated payment system</p>" +
                    "                </div>" +
                    "            </div>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>Need help getting started? Contact us at <strong>support@zennest.africa</strong></p>" +
                    "            <p>&copy; 2025 Zennest. Empowering property owners across Nigeria.</p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";

            boolean emailSent = sendEmail(to, subject, content);
            if (emailSent) {
                logger.info("Enhanced landlord profile completion email with magic link sent to: {}", to);
            }
            return emailSent;

        } catch (Exception e) {
            logger.error("Failed to send enhanced landlord profile completion email to {}: {}", to, e.getMessage());
            // Fallback to regular email without magic link
            return sendLandlordProfileCompletionEmail(to, firstName);
        }
    }
}
