package dev.dolu.userservice.service;

import dev.dolu.userservice.metrics.CustomMetricService;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.utils.JwtUtils;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final VerificationService verificationService;

    private final CustomMetricService customMetricService;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtils jwtUtils, VerificationService verificationService, CustomMetricService customMetricService) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.verificationService = verificationService;
        this.customMetricService = customMetricService;
    }

    /**
     * Registers a new user by validating inputs, hashing their password, and saving the User entity.
     * If the email or username is already taken, throws a 409 Conflict error.
     *
     * @param user User object containing registration details.
     * @return The saved User object with sensitive fields like the password hashed.
     */
    public Map<String, Object> registerUser(User user) {
        Map<String, Object> response = new HashMap<>();

        // Validate for duplicate username
        // Validate for duplicate username if present
        if (user.getUsername() != null && userRepository.existsByUsername(user.getUsername())) {
            logger.warn("Registration failed: Username '{}' is already taken.", user.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }

        // Validate for duplicate email
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Registration failed: Email '{}' is already in use.", user.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }

        // Start timing registration process
        long startTime = System.currentTimeMillis();

        // Process user details
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);

        // Save user to repository
        User savedUser = userRepository.save(user);
        response.put("user", savedUser);
        response.put("message", "User registered successfully. Please verify your email.");

        // Send verification email and ensure it was sent successfully
        boolean emailSent = verificationService.createAndSendVerificationToken(savedUser);
        if (!emailSent) {
            logger.error("Failed to send verification email to '{}'.", user.getEmail());
            throw new EmailSendingFailedException("User registered but failed to send verification email. Please try again or use /reverify.");
        }

        // Calculate the duration and record the registration time metric
        long duration = System.currentTimeMillis() - startTime;
        customMetricService.recordUserRegistrationTime(duration);

        // Increment the user registration count metric only after email is sent
        customMetricService.incrementUserRegistrationCounter();

        return response;
    }
    /**
     * Authenticates a user by their email and password, and issues JWT tokens upon successful login.
     * If the account is not verified, resends the verification token and returns a 403 Forbidden error.
     *
     * @param email The user's email.
     * @param password The user's raw password.
     * @return A map containing the access and refresh tokens.
     * @throws MessagingException If an error occurs while resending the verification token.
     */
    public Map<String, String> login(String email, String password) throws MessagingException {
        long startTime = System.currentTimeMillis();

        // Retrieve user from database
        User user = userRepository.findByEmail(email);

        if (user != null && passwordEncoder.matches(password, user
                .getPassword())) {
            // If user exists and password matches

            // Check if user is enabled (verified)
            if (!user.isEnabled()) {
                // If not verified, resend verification token and return an error
                verificationService.resendVerificationToken(user);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not verified. A new verification email has been sent.");
            }

            // Generate access and refresh tokens
            String accessToken = jwtUtils.generateJwtToken(email);
            String refreshToken = jwtUtils.generateRefreshToken(email);

            // Store refresh token securely (Redis-backed storage)
            jwtUtils.storeRefreshToken(refreshToken, email);

            // Calculate duration and record login time
            long duration = System.currentTimeMillis() - startTime;
            customMetricService.recordLoginTime(duration);

            // Increment success counter and return tokens
            customMetricService.incrementLoginSuccessCounter();
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            return tokens;
        }

        // Record login time for failed attempt as well
        long duration = System.currentTimeMillis() - startTime;
        customMetricService.recordLoginTime(duration);
        customMetricService.incrementLoginFailureCounter();

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }


    /**
     * Finds a user by their ID.
     *
     * @param userId The user's ID.
     * @return The User object if found, or null if not found.
     */
    public User findUserById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
