package dev.dolu.userservice.service;

import dev.dolu.userservice.metrics.CustomMetricService;
import dev.dolu.userservice.models.ChangePasswordRequest;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.models.VerificationToken;
import dev.dolu.userservice.models.VisitDuration;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.repository.VerificationTokenRepository;
import dev.dolu.userservice.utils.JwtUtils;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final VerificationService verificationService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;


    private final CustomMetricService customMetricService;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtils jwtUtils, VerificationService verificationService, CustomMetricService customMetricService, VerificationTokenRepository verificationTokenRepository, EmailService emailService) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.verificationService = verificationService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
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

        // Normalize username: treat blank as null
        if (user.getUsername() != null && user.getUsername().isBlank()) {
            user.setUsername(null);
        }

        // Check for existing username if one is provided
        if (user.getUsername() != null && userRepository.existsByUsername(user.getUsername())) {
            logger.warn("Registration failed: Username '{}' is already taken.", user.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }

        // Check for existing email
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Registration failed: Email '{}' is already in use.", user.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }

        // Start timing
        long startTime = System.currentTimeMillis();

        // Hash password and mark account as disabled (pending verification)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);

        // Save user
        User savedUser = userRepository.save(user);
        response.put("user", savedUser);
        response.put("message", "User registered successfully. Please verify your email.");

        // Send verification email
        boolean emailSent = verificationService.createAndSendVerificationToken(savedUser);
        if (!emailSent) {
            logger.error("Verification email failed for '{}'", user.getEmail());
            throw new EmailSendingFailedException("User registered but failed to send verification email. Please try again or use /reverify.");
        }

        // Metrics
        long duration = System.currentTimeMillis() - startTime;
        customMetricService.recordUserRegistrationTime(duration);
        customMetricService.incrementUserRegistrationCounter();

        return response;
    }

    /**
     * Authenticates a user by their email and password, and issues JWT tokens upon successful login.
     * If the account is not verified, resends the verification token and returns a 403 Forbidden error.
     *
     * @param email    The user's email.
     * @param password The user's raw password.
     * @return A map containing the access and refresh tokens.
     * @throws MessagingException If an error occurs while resending the verification token.
     */

    //TODO BLOCK PASSWORD LOGIN FOR AUTH USERS
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

            // Record last login timestamp
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

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
     * Authenticates a user for Zenest flow by email and password, and issues JWT tokens upon successful login.
     * If the account is not verified, resends the Zenest verification code and returns a 403 Forbidden error.
     */
    public Map<String, String> loginZenest(String email, String password) throws MessagingException {
        long startTime = System.currentTimeMillis();
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            if (!user.isEnabled()) {
                // resend Zenest-specific verification code
                verificationService.resendZenestVerificationCode(email);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not verified. A new Zennest verification code has been sent.");
            }
            String accessToken = jwtUtils.generateJwtToken(email);
            String refreshToken = jwtUtils.generateRefreshToken(email);
            jwtUtils.storeRefreshToken(refreshToken, email);
            customMetricService.recordLoginTime(System.currentTimeMillis() - startTime);
            customMetricService.incrementLoginSuccessCounter();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            return tokens;
        }
        customMetricService.recordLoginTime(System.currentTimeMillis() - startTime);
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

    // New helper methods
    public void recordLogin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public User markProfileComplete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setProfileCompleted(true);
        return userRepository.save(user);
    }

    public User markOnboardingComplete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setOnboardingCompleted(true);
        return userRepository.save(user);
    }

    public User updateSubscription(UUID userId, String plan, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setSubscriptionPlan(plan);
        user.setSubscriptionActive(active);
        return userRepository.save(user);
    }

    /**
     * Registers a new user and sends a Zenest verification code.
     */
    @Transactional
    public Map<String, Object> registerUserWithZenest(User user) {
        // Normalize username
        if (user.getUsername() != null && user.getUsername().isBlank()) {
            user.setUsername(null);
        }
        // Check for existing username
        if (user.getUsername() != null && userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }
        // Check for existing email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }
        // Hash password and disable until verified
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        User savedUser = userRepository.save(user);
        // Send Zenest code
        boolean sent = verificationService.sendZenestVerificationCode(savedUser.getEmail());
        if (!sent) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send verification code");
        }
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("user", savedUser);
        response.put("emailStatus", "code_sent");
        response.put("message", "User registered successfully. Please verify using the code sent to your email.");
        // Metrics
        customMetricService.incrementUserRegistrationCounter();
        return response;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return; // Optionally, do not reveal user existence

        verificationTokenRepository.deleteByUser(user);
        String code = VerificationService.TokenGenerator.generateToken(6);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        VerificationToken token = new VerificationToken(code, user, expiry);
        verificationTokenRepository.save(token);
        emailService.sendPasswordResetEmail(email, code);

    }

    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new ResponseStatusException(NOT_FOUND, "User not found");

        Optional<VerificationToken> opt = verificationTokenRepository.findByToken(code);
        if (opt.isEmpty() || !opt.get().getUser().getEmail().equals(email) ||
                opt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid or expired code");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "New password cannot be the same as the current password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        verificationTokenRepository.delete(opt.get());
    }

    @Transactional
    public User increaseUserTotalEarnings(UUID userId, Double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.increaseTotalEarnings(amount);
        return userRepository.save(user);
    }

    @Transactional
    public User setUserVisitation(UUID userId, boolean openVisitations) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setOpenVisitations(openVisitations);
        return userRepository.save(user);
    }

    @Transactional
    public User setUserPaymentVerified(UUID userId, boolean paymentVerified) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPaymentVerified(paymentVerified);
        return userRepository.save(user);
    }

    /**
     * Adds a listing to the user's favourites list if not already present.
     *
     * @param userId    UUID of the user
     * @param listingId UUID of the listing to add
     */
    @Transactional
    public void addListingToFavourites(UUID userId, UUID listingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if (user.getFavourites() == null) {
            user.setFavourites(new java.util.ArrayList<>());
        }
        if (!user.getFavourites().contains(listingId)) {
            user.getFavourites().add(listingId);
            userRepository.save(user);
        }
    }

    /**
     * Removes a listing from the user's favourites list if present.
     *
     * @param userId    UUID of the user
     * @param listingId UUID of the listing to remove
     */
    @Transactional
    public void removeListingFromFavourites(UUID userId, UUID listingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if (user.getFavourites() != null && user.getFavourites().contains(listingId)) {
            user.getFavourites().remove(listingId);
            userRepository.save(user);
        }
    }

    /**
     * Changes the password for the authenticated user.
     * Validates current password, checks new password confirmation, and updates password.
     *
     * @param userId  The ID of the user changing their password.
     * @param request The ChangePasswordRequest DTO.
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "New password and confirmation do not match");
        }
        if (request.getNewPassword().length() < 8) {
            throw new ResponseStatusException(BAD_REQUEST, "New password must be at least 8 characters long");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void setVisitDuration(UUID userId, VisitDuration visitDuration) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setVisitDuration(visitDuration);
        userRepository.save(user);
    }

    @Transactional
    public void setAutoAcceptBooking(UUID userId, boolean autoAccept) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setAutoAcceptBooking(autoAccept);
        userRepository.save(user);
    }

    @Transactional
    public void setAutoAcceptVisitation(UUID userId, boolean autoAccept) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setAutoAcceptVisitation(autoAccept);
        userRepository.save(user);
    }

    @Transactional
    public void setNotificationPreferences(UUID userId, Boolean emailEnabled, Boolean smsEnabled, Boolean pushEnabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if (emailEnabled != null) user.setEmailNotificationsEnabled(emailEnabled);
        if (smsEnabled != null) user.setSmsNotificationsEnabled(smsEnabled);
        if (pushEnabled != null) user.setPushNotificationsEnabled(pushEnabled);
        userRepository.save(user);
    }

    @Transactional
    public void setBufferTimeHours(UUID userId, int bufferTimeHours) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setBufferTimeHours(bufferTimeHours);
        userRepository.save(user);
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User updateSearchRadius(UUID userId, Integer searchRadius) {
        User user = getUserById(userId);
        user.setSearchRadius(searchRadius);
        return userRepository.save(user);
    }

    public User updatePriceAlerts(UUID userId, Boolean priceAlerts) {
        User user = getUserById(userId);
        user.setPriceAlerts(priceAlerts);
        return userRepository.save(user);
    }

    public User updateNewListingAlerts(UUID userId, Boolean newListingAlerts) {
        User user = getUserById(userId);
        user.setNewListingAlerts(newListingAlerts);
        return userRepository.save(user);
    }

    public User updateVisitReminders(UUID userId, Boolean visitReminders) {
        User user = getUserById(userId);
        user.setVisitReminders(visitReminders);
        return userRepository.save(user);
    }

    public User updateAutoSaveSearches(UUID userId, Boolean autoSaveSearches) {
        User user = getUserById(userId);
        user.setAutoSaveSearches(autoSaveSearches);
        return userRepository.save(user);
    }

    public User updateMaxBudget(UUID userId, Double maxBudget) {
        User user = getUserById(userId);
        user.setMaxBudget(maxBudget);
        return userRepository.save(user);
    }

    public User updatePreferredPropertyTypes(UUID userId, List<String> preferredPropertyTypes) {
        User user = getUserById(userId);
        user.setPreferredPropertyTypes(preferredPropertyTypes);
        return userRepository.save(user);
    }

    public User updatePreferredAmenities(UUID userId, List<String> preferredAmenities) {
        User user = getUserById(userId);
        user.setPreferredAmenities(preferredAmenities);
        return userRepository.save(user);
    }
}
