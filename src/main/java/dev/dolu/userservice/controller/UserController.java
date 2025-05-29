package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.LoginRequest;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.models.UserDTO;
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
            return ResponseEntity.ok(Map.of(
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "id", user.getId(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "username", user.getUsername(),
                    "profileImage", user.getProfileImage()
            ));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        logger.warn("Validation errors: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}