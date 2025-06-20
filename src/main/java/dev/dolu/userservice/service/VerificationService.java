package dev.dolu.userservice.service;

import dev.dolu.userservice.metrics.CustomMetricService;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.models.VerificationToken;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException; // Import MessagingException
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class VerificationService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    // TODO: Replace hardcoded URL with environment variable
    @Value("${verification.url.prefix}")
    private String verificationUrlPrefix;

    private final CustomMetricService customMetricService;
    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    @Autowired
    public VerificationService(VerificationTokenRepository verificationTokenRepository,
                               UserRepository userRepository, EmailService emailService, CustomMetricService customMetricService) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
        this.customMetricService = customMetricService;
        this.emailService = emailService;
    }

    // Step 1: Define TokenGenerator
    public static class TokenGenerator {
        private static final String CHARACTER_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        public static String generateToken(int length) {
            return SECURE_RANDOM.ints(length, 0, CHARACTER_SET.length())
                    .mapToObj(CHARACTER_SET::charAt)
                    .map(Object::toString)
                    .collect(Collectors.joining());
        }
    }

    // Step 2: Create and send verification token using TokenGenerator
    @Transactional
    public boolean createAndSendVerificationToken(User user) {
        try {
            // Clear previous tokens for this user
            verificationTokenRepository.deleteByUser(user);

            // Generate a token and set expiry
            String token = TokenGenerator.generateToken(6);
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

            // Save token
            VerificationToken verificationToken = new VerificationToken(token, user, expiryDate);
            verificationTokenRepository.save(verificationToken);

            // Build verification URL
            String verificationUrl = verificationUrlPrefix + "?token=" + token;

            // Send the email and check the response
            boolean sent = emailService.sendVerificationEmail(user.getEmail(), verificationUrl);
            if (!sent) {
                logger.error("Failed to send verification email to '{}'.", user.getEmail());
                return false;  // Indicate email sending failure
            }
            return true;  // Email sent successfully

        } catch (Exception e) {
            // Log any unexpected errors
            logger.error("Exception while sending verification email to '{}'.", user.getEmail(), e);
            return false;
        }
    }


    public boolean verifyToken(String token) {
        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByToken(token);

        if (optionalToken.isPresent()) {
            VerificationToken verificationToken = optionalToken.get();

            // Log token details for debugging
            logger.debug("Token found: {}", token);
            logger.debug("Expiry date: {}", verificationToken.getExpiryDate());

            // Check if the token has expired
            if (verificationToken.getExpiryDate().isAfter(LocalDateTime.now())) {
                User user = verificationToken.getUser();
                user.setEnabled(true);
                user.setVerified(true);
                userRepository.save(user);  // Update user status in the database
                customMetricService.incrementUserActivationSuccessCounter();

                // Remove the token after successful verification
                verificationTokenRepository.delete(verificationToken);
                return true;
            } else {
                logger.warn("Token expired: {}", token);
            }
        } else {
            logger.warn("Token not found or invalid: {}", token);
        }

        customMetricService.incrementUserActivationFailureCounter();
        return false;  // Return false if the token is invalid or expired
    }

    @Transactional
    public void resendVerificationToken(User user) throws MessagingException {
        // Clear any previous tokens for this user
        verificationTokenRepository.deleteByUser(user);

        // Generate a new token and save it with a 5-minute expiry
        String token = TokenGenerator.generateToken(6);
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

        VerificationToken newToken = new VerificationToken(token, user, expiryDate);
        verificationTokenRepository.save(newToken);

        // Send a new verification email
        String verificationUrl = verificationUrlPrefix + "?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), verificationUrl);
    }

    @Transactional
    public boolean sendZenestVerificationCode(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(NOT_FOUND, "User not found");
        }
        // Clear previous codes
        verificationTokenRepository.deleteByUser(user);
        String code = TokenGenerator.generateToken(6);
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);
        VerificationToken token = new VerificationToken(code, user, expiryDate);
        verificationTokenRepository.save(token);
        boolean sent = emailService.sendZenestVerificationEmail(email, code);
        if (!sent) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to send verification email");
        }
        return true;
    }

    public boolean verifyZenestCode(String email, String code) {
        Optional<VerificationToken> opt = verificationTokenRepository.findByToken(code);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid verification code");
        }
        VerificationToken vt = opt.get();
        if (!vt.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Code does not match user");
        }
        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Verification code expired");
        }
        User user = vt.getUser();
        user.setEnabled(true);
        user.setVerified(true);
        userRepository.save(user);
        verificationTokenRepository.delete(vt);
        customMetricService.incrementUserActivationSuccessCounter();
        return true;
    }

    @Transactional
    public boolean resendZenestVerificationCode(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(NOT_FOUND, "User not found");
        }
        if (user.isEnabled()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "User already verified");
        }
        // Clear previous codes
        verificationTokenRepository.deleteByUser(user);
        String code = TokenGenerator.generateToken(6);
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);
        VerificationToken token = new VerificationToken(code, user, expiryDate);
        verificationTokenRepository.save(token);
        boolean sent = emailService.sendZenestResendVerificationEmail(email, code);
        if (!sent) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to send verification email");
        }
        return true;
    }

}