package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.*;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.service.UserService;
import dev.dolu.userservice.service.VerificationService;
import dev.dolu.userservice.utils.JwtUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import dev.dolu.userservice.models.Role;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final VerificationService verificationService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, JwtUtils jwtUtils, VerificationService verificationService, UserRepository userRepository) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.verificationService = verificationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for email: {}", loginRequest.getEmail());
            Map<String, String> tokens = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
            return new ResponseEntity<>(tokens, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            logger.warn("Login failed: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/login-zennest")
    public ResponseEntity<Map<String, String>> loginZenest(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        logger.info("Zennest login requested for email: {}", email);

        try {
            Map<String, String> tokens = userService.loginZenest(email, password);
            return ResponseEntity.ok(tokens);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            logger.error("Unexpected error during Zenest login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected server error."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody User user) {
        logger.info("Registering user: {}", user.getEmail());
        Map<String, Object> response = userService.registerUser(user);
        if (response.containsKey("emailStatus")) {
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
            long expiration = jwtUtils.getExpirationFromToken(jwt);
            jwtUtils.blacklistToken(jwt, expiration, TimeUnit.MILLISECONDS);
            logger.info("User logged out. Token blacklisted.");
            return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
        }
        logger.warn("Logout failed: Invalid token format");
        return new ResponseEntity<>("Invalid token", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, @RequestBody Map<String, String> refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.get("refreshToken");
        String email = jwtUtils.getUsernameFromJwtToken(refreshToken);
        logger.info("Refresh token requested by user: {}", email);
        if (email != null && jwtUtils.validateRefreshToken(refreshToken) && jwtUtils.isRefreshTokenValid(email, refreshToken)) {
            String newAccessToken = jwtUtils.generateJwtToken(email);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } else {
            logger.warn("Invalid or expired refresh token for email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token.");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        logger.info("Verification attempt with token: {}", token);
        boolean isVerified = verificationService.verifyToken(token);
        if (isVerified) {
            return ResponseEntity.ok("Account verified successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired verification token.");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Resend verification requested for email: {}", email);
        User user = userRepository.findByEmail(email);
        if (user != null && !user.isEnabled()) {
            try {
                verificationService.resendVerificationToken(user);
                return ResponseEntity.ok("A new verification email has been sent to " + email);
            } catch (MessagingException e) {
                logger.error("Failed to send verification email", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send verification email.");
            }
        }
        return ResponseEntity.badRequest().body("User not found or already verified.");
    }


    @GetMapping("/user-details")
    public ResponseEntity<?> getUserDetails(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization").substring(7);
        String email = jwtUtils.getUsernameFromJwtToken(jwt);
        logger.info("Fetching user details for email: {}", email);
        User user = userRepository.findByEmail(email);
        if (user != null) {
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("email", user.getEmail());
            userDetails.put("role", user.getRole());
            userDetails.put("firstName", user.getFirstName());
            userDetails.put("lastName", user.getLastName());
            userDetails.put("username", user.getUsername());
            userDetails.put("profileImage", user.getProfileImage());
            userDetails.put("phoneNumber", user.getPhoneNumber());
            userDetails.put("verified", user.isVerified());
            userDetails.put("enabled", user.isEnabled());
            userDetails.put("city", user.getCity());
            userDetails.put("country", user.getCountry());
            userDetails.put("dateOfBirth", user.getDateOfBirth());
            userDetails.put("profession", user.getProfession());
            userDetails.put("activePlan", user.getActivePlan());
            userDetails.put("favourites", user.getFavourites());
            userDetails.put("intention", user.getIntention());
            userDetails.put("profileDescription", user.getProfileDescription());
            userDetails.put("profilePicture", user.getProfilePicture());
            userDetails.put("updatedAt", user.getUpdatedAt());

            // Additional fields
            userDetails.put("openVisitations", user.isOpenVisitations());
            userDetails.put("paymentVerified", user.isPaymentVerified());
            userDetails.put("fcmDeviceToken", user.getFcmDeviceToken());
            userDetails.put("lastLogin", user.getLastLogin());
            userDetails.put("profileCompleted", user.isProfileCompleted());
            userDetails.put("onboardingCompleted", user.isOnboardingCompleted());
            userDetails.put("subscriptionPlan", user.getSubscriptionPlan());
            userDetails.put("subscriptionActive", user.isSubscriptionActive());
            userDetails.put("totalEarnings", user.getTotalEarnings());
            userDetails.put("completedFeatures", user.getCompletedFeatures());
            userDetails.put("businessType", user.getBusinessType());
            userDetails.put("visitDuration", user.getVisitDuration());
            userDetails.put("autoAcceptBooking", user.isAutoAcceptBooking());
            userDetails.put("autoAcceptVisitation", user.isAutoAcceptVisitation());
            userDetails.put("emailNotificationsEnabled", user.isEmailNotificationsEnabled());
            userDetails.put("smsNotificationsEnabled", user.isSmsNotificationsEnabled());
            userDetails.put("pushNotificationsEnabled", user.isPushNotificationsEnabled());
            userDetails.put("bufferTimeHours", user.getBufferTimeHours());
            userDetails.put("createdAt", user.getCreatedAt());
            userDetails.put("version", user.getVersion());
            // Additional missing fields
            userDetails.put("authProvider", user.getAuthProvider());
            userDetails.put("payoutInfo", user.getPayoutInfo());

// Tenant-specific fields
            userDetails.put("searchRadius", user.getSearchRadius());
            userDetails.put("priceAlerts", user.getPriceAlerts());
            userDetails.put("newListingAlerts", user.getNewListingAlerts());
            userDetails.put("visitReminders", user.getVisitReminders());
            userDetails.put("autoSaveSearches", user.getAutoSaveSearches());
            userDetails.put("maxBudget", user.getMaxBudget());
            userDetails.put("preferredPropertyTypes", user.getPreferredPropertyTypes());
            userDetails.put("preferredAmenities", user.getPreferredAmenities());


            return ResponseEntity.ok(userDetails);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    @PostMapping("/register-zennest")
    public ResponseEntity<Map<String, Object>> registerZenest(@Valid @RequestBody User user) {
        logger.info("Registering user from Zennest: {}", user.getEmail());
        Map<String, Object> resp = userService.registerUserWithZenest(user);
        return new ResponseEntity<>(resp,
                resp.containsKey("emailStatus") ? HttpStatus.ACCEPTED : HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        logger.info("Fetching user by ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
        User u = user.get();
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail()
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("Fetching all users");
        List<UserDTO> users = userRepository.findAll().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping("/batch")
    public ResponseEntity<?> getUsersByIds(@RequestBody List<UUID> userIds) {
        logger.info("Fetching batch users: {}", userIds);
        List<User> users = userRepository.findAllById(userIds);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/profile-complete")
    public ResponseEntity<User> markProfileComplete(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.markProfileComplete(id));
    }

    @PatchMapping("/{id}/onboarding-complete")
    public ResponseEntity<User> markOnboardingComplete(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.markOnboardingComplete(id));
    }

    @PatchMapping("/{id}/subscription")
    public ResponseEntity<User> updateSubscription(@PathVariable UUID id,
                                                   @RequestBody Map<String, Object> req) {
        String plan = (String) req.get("plan");
        Boolean active = (Boolean) req.get("active");
        return ResponseEntity.ok(userService.updateSubscription(id, plan, active));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        logger.warn("Validation errors: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Password reset requested for email: {}", email);
        userService.requestPasswordReset(email);
        // Always return success to avoid revealing user existence
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset code has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");
        logger.info("Password reset attempt for email: {}", email);
        userService.resetPassword(email, code, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
    }

    @PostMapping("/resend-zennest-verification")
    public ResponseEntity<Map<String, String>> resendZenestVerification(@Valid @RequestBody ResendZenestRequest request) {
        logger.info("Resending Zenest verification code for email: {}", request.getEmail());
        verificationService.resendZenestVerificationCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Verification code resent successfully"));
    }

    @PostMapping("/verify-zennest")
    public ResponseEntity<Map<String, String>> verifyZenest(@Valid @RequestBody VerifyZenestRequest request) {
        logger.info("Verifying Zenest code for email: {}", request.getEmail());

        boolean verified = verificationService.verifyZenestCode(request.getEmail(), request.getCode());
        if (!verified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid or expired verification code"));
        }

        // Generate tokens after successful verification
        String accessToken = jwtUtils.generateJwtToken(request.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(request.getEmail());

        return ResponseEntity.ok(Map.of(
                "message", "User verified successfully",
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    @PostMapping("/magic-signin")
    public ResponseEntity<?> magicSignIn(@RequestBody Map<String, String> request) {
        try {
            String magicToken = request.get("token");
            logger.info("Magic sign-in attempt with token");

            // Validate the magic token
            if (magicToken == null || !jwtUtils.validateJwtToken(magicToken)) {
                logger.warn("Invalid or expired magic token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired magic link"));
            }

            // Check if token is blacklisted
            if (jwtUtils.isTokenBlacklisted(magicToken)) {
                logger.warn("Attempt to use blacklisted magic token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Magic link has already been used"));
            }

            // Extract user details from the magic token
            Map<String, Object> userDetails = jwtUtils.getUserDetailsFromJwtToken(magicToken);
            if (userDetails == null) {
                logger.warn("Magic token missing user details");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid magic link format"));
            }

            String userId = (String) userDetails.get("id");
            String email = (String) userDetails.get("email");
            String role = (String) userDetails.get("role");

            // Verify user exists in database
            User user = userRepository.findByEmail(email);
            if (user == null) {
                logger.warn("User not found for magic sign-in: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            // Generate new access and refresh tokens for the authenticated session
            String accessToken = jwtUtils.generateJwtToken(user.getId(), user.getEmail(), user.getRole().toString());
            String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
            jwtUtils.storeRefreshToken(refreshToken, user.getEmail());

            // Optionally blacklist the magic token to prevent reuse
            long expiration = jwtUtils.getExpirationFromToken(magicToken);
            if (expiration > 0) {
                jwtUtils.blacklistToken(magicToken, expiration, TimeUnit.MILLISECONDS);
            }

            logger.info("Magic sign-in successful for user: {}", email);

            // Return the new tokens and user info
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Magic sign-in successful");
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during magic sign-in", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PatchMapping("/{id}/increase-earnings")
    public ResponseEntity<User> increaseUserTotalEarnings(@PathVariable UUID id, @RequestParam Double amount) {
        User updatedUser = userService.increaseUserTotalEarnings(id, amount);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/visitation")
    public ResponseEntity<UserDTO> setUserVisitation(@PathVariable UUID id, @RequestParam boolean openVisitations) {
        User updatedUser = userService.setUserVisitation(id, openVisitations);
        return ResponseEntity.ok(new UserDTO(updatedUser));
    }

    @PatchMapping("/{id}/payment-verified")
    public ResponseEntity<UserDTO> setUserPaymentVerified(@PathVariable UUID id, @RequestParam boolean paymentVerified) {
        User updatedUser = userService.setUserPaymentVerified(id, paymentVerified);
        return ResponseEntity.ok(new UserDTO(updatedUser));
    }

    @PostMapping("/favourites/add")
    public ResponseEntity<?> addListingToFavourites(@RequestBody Map<String, String> payload) {
        try {
            UUID userId = UUID.fromString(payload.get("userId"));
            UUID listingId = UUID.fromString(payload.get("listingId"));
            userService.addListingToFavourites(userId, listingId);
            return ResponseEntity.ok(Map.of("message", "Listing added to favourites."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid UUID format."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }


    }
    @PostMapping("/favourites/remove")
    public ResponseEntity<?> removeListingFromFavourites(@RequestBody Map<String, String> payload) {
        try {
            UUID userId = UUID.fromString(payload.get("userId"));
            UUID listingId = UUID.fromString(payload.get("listingId"));
            userService.removeListingFromFavourites(userId, listingId);
            return ResponseEntity.ok(Map.of("message", "Listing removed from favourites."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid UUID format."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            userService.changePassword(user.getId(), request);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/set-visit-duration")
    public ResponseEntity<?> setVisitDuration(@RequestParam("duration") VisitDuration duration, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            if (user.getRole() != Role.LANDLORD) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only LANDLORD users can perform this action."));
            }
            userService.setVisitDuration(user.getId(), duration);
            return ResponseEntity.ok(Map.of("message", "Visit duration updated successfully."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }
    @PostMapping("/set-auto-accept-visitation")
    public ResponseEntity<?> setAutoAcceptVisitation(@RequestParam("enabled") boolean enabled, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            if (user.getRole() != Role.LANDLORD) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only LANDLORD users can perform this action."));
            }
            userService.setAutoAcceptVisitation(user.getId(), enabled);
            return ResponseEntity.ok(Map.of("message", "Auto-accept visitation updated successfully."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }
    @PostMapping("/set-auto-accept-booking")
    public ResponseEntity<?> setAutoAcceptBooking(@RequestParam("enabled") boolean enabled, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            if (user.getRole() != Role.LANDLORD) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only LANDLORD users can perform this action."));
            }
            userService.setAutoAcceptBooking(user.getId(), enabled);
            return ResponseEntity.ok(Map.of("message", "Auto-accept booking updated successfully."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }
    @PostMapping("/set-notification-preferences")
    public ResponseEntity<?> setNotificationPreferences(
            @RequestParam(value = "emailEnabled", required = false) Boolean emailEnabled,
            @RequestParam(value = "smsEnabled", required = false) Boolean smsEnabled,
            @RequestParam(value = "pushEnabled", required = false) Boolean pushEnabled,
            HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            userService.setNotificationPreferences(user.getId(), emailEnabled, smsEnabled, pushEnabled);
            return ResponseEntity.ok(Map.of("message", "Notification preferences updated successfully."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }
    @PostMapping("/set-buffer-time")
    public ResponseEntity<?> setBufferTime(@RequestParam("hours") int hours, HttpServletRequest httpRequest) {
        try {
            if (hours < 0 || hours > 72) {
                return ResponseEntity.badRequest().body(Map.of("error", "Buffer time must be between 0 and 72 hours."));
            }
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            if (user.getRole() != Role.LANDLORD) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only LANDLORD users can perform this action."));
            }
            userService.setBufferTimeHours(user.getId(), hours);
            return ResponseEntity.ok(Map.of("message", "Buffer time updated successfully.", "bufferTimeHours", hours));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }


    @PostMapping("/set-search-radius")
    public ResponseEntity<?> setSearchRadius(@RequestParam("radius") Integer radius, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            User updatedUser = userService.updateSearchRadius(user.getId(), radius);
            return ResponseEntity.ok(Map.of("message", "Search radius updated successfully.", "searchRadius", radius));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/set-price-alerts")
    public ResponseEntity<?> setPriceAlerts(@RequestParam("enabled") Boolean enabled, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            User updatedUser = userService.updatePriceAlerts(user.getId(), enabled);
            return ResponseEntity.ok(Map.of("message", "Price alerts updated successfully.", "priceAlerts", enabled));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/set-new-listing-alerts")
    public ResponseEntity<?> setNewListingAlerts(@RequestParam("enabled") Boolean enabled, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            User updatedUser = userService.updateNewListingAlerts(user.getId(), enabled);
            return ResponseEntity.ok(Map.of("message", "New listing alerts updated successfully.", "newListingAlerts", enabled));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/set-visit-reminders")
    public ResponseEntity<?> setVisitReminders(@RequestParam("enabled") Boolean enabled, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            User updatedUser = userService.updateVisitReminders(user.getId(), enabled);
            return ResponseEntity.ok(Map.of("message", "Visit reminders updated successfully.", "visitReminders", enabled));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/set-auto-save-searches")
    public ResponseEntity<?> setAutoSaveSearches(@RequestParam("enabled") Boolean enabled, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            User updatedUser = userService.updateAutoSaveSearches(user.getId(), enabled);
            return ResponseEntity.ok(Map.of("message", "Auto-save searches updated successfully.", "autoSaveSearches", enabled));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/set-max-budget")
    public ResponseEntity<?> setMaxBudget(@RequestParam("budget") Double budget, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            User updatedUser = userService.updateMaxBudget(user.getId(), budget);
            return ResponseEntity.ok(Map.of("message", "Max budget updated successfully.", "maxBudget", budget));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/set-preferred-property-types")
    public ResponseEntity<?> setPreferredPropertyTypes(@RequestBody List<String> propertyTypes, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            User updatedUser = userService.updatePreferredPropertyTypes(user.getId(), propertyTypes);
            return ResponseEntity.ok(Map.of("message", "Preferred property types updated successfully.", "preferredPropertyTypes", propertyTypes));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/set-preferred-amenities")
    public ResponseEntity<?> setPreferredAmenities(@RequestBody List<String> amenities, HttpServletRequest httpRequest) {
        try {
            String jwt = httpRequest.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid token."));
            }
            String email = jwtUtils.getUsernameFromJwtToken(jwt.substring(7));
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
            }
            User updatedUser = userService.updatePreferredAmenities(user.getId(), amenities);
            return ResponseEntity.ok(Map.of("message", "Preferred amenities updated successfully.", "preferredAmenities", amenities));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }


}