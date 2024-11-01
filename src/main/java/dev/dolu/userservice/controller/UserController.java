package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.service.UserService;
import dev.dolu.userservice.service.VerificationService;
import dev.dolu.userservice.utils.JwtUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final VerificationService verificationService;
    private final UserRepository userRepository;

    // Constructor-based dependency injection for UserService
    @Autowired
    public UserController(UserService userService, JwtUtils jwtUtils, VerificationService verificationService, UserRepository userRepository) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.userRepository= userRepository;


        this.verificationService = verificationService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody User loginRequest) {
        try {
            Map<String, String> tokens = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            if (tokens != null) {
                return new ResponseEntity<>(tokens, HttpStatus.OK);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid username or password");
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
            }
        } catch (MessagingException e) {
            // Log the error if needed
            return new ResponseEntity<>("Failed to send verification email.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    /**
     * Handles HTTP POST requests for user registration.
     * The @Valid annotation triggers validation for the User object,
     * ensuring fields meet validation constraints (e.g., email format, required fields).
     *
     * @param user User object containing registration details.
     * @return ResponseEntity with the saved User and HTTP status 201 (Created) if successful.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        // Calls the service layer to save the user after hashing the password.
        User savedUser = userService.registerUser(user);
        // Returns the saved User object with HTTP 201 status (Created).
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // Other CRUD endpoints for managing user data...
    // New Logout Endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");

        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);  // Remove "Bearer " prefix
            long expiration = jwtUtils.getExpirationFromToken(jwt);
            jwtUtils.blacklistToken(jwt, expiration, TimeUnit.MILLISECONDS);
            return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid token", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, @RequestBody Map<String, String> refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.get("refreshToken");
        String username = jwtUtils.getUsernameFromJwtToken(refreshToken);

        if (username != null && jwtUtils.validateRefreshToken(refreshToken) && jwtUtils.isRefreshTokenValid(username, refreshToken)) {
            String newAccessToken = jwtUtils.generateJwtToken(username);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token.");
        }
    }





    /**
     * Exception handler to catch validation errors on invalid user input.
     * This method is triggered when a MethodArgumentNotValidException is thrown,
     * which occurs if the @Valid annotation detects invalid data.
     *
     * @param ex The MethodArgumentNotValidException that contains details about validation errors.
     * @return ResponseEntity with a map of field names and error messages, and HTTP 400 (Bad Request) status.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Creates a map to store field-specific error messages
        Map<String, String> errors = new HashMap<>();

        // Iterates through each field error in the exception's binding result
        ex.getBindingResult().getFieldErrors().forEach(error ->
                // Maps the field name to its default error message
                errors.put(error.getField(), error.getDefaultMessage()));

        // Returns the map of errors with HTTP 400 status, indicating a bad request due to validation failure
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }






    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
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
        User user = userRepository.findByEmail(email);

        if (user != null && !user.isEnabled()) {
            try {
                verificationService.resendVerificationToken(user);
                return ResponseEntity.ok("A new verification email has been sent to " + email);
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send verification email.");
            }
        }

        return ResponseEntity.badRequest().body("User not found or already verified.");
    }

}

