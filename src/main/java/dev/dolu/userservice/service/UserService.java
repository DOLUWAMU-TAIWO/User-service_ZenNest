package dev.dolu.userservice.service;

import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.utils.JwtUtils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    // Password encoder for hashing user passwords before saving them
    private final BCryptPasswordEncoder passwordEncoder;

    // Repository to handle database operations for the User entity
    private final UserRepository userRepository;

    /**
     * Constructor-based dependency injection for UserRepository. and jwutils
     * Initializes BCryptPasswordEncoder for secure password hashing.
     *
     * @param userRepository Injected UserRepository for interacting with the database
     */
    private final JwtUtils jwtUtils;
    private final VerificationService verificationService;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtils jwtUtils, VerificationService verificationService) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.verificationService = verificationService;
    }

    /**
     * Registers a new user by hashing their password and saving the User entity.
     *
     * @param user User object containing registration details (username, email, raw password, etc.)
     * @return The saved User object with the hashed password stored in the database
     */
    public User registerUser(User user) {
        // Hash the user's password to ensure secure storage
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword); // Set the hashed password on the user object
        user.setEnabled(false); // Set the user to disabled until they verify their email

        User savedUser = userRepository.save(user); // Save the user to the database

        try {
            verificationService.createAndSendVerificationToken(savedUser);
        } catch (MessagingException e) {
            e.printStackTrace(); // Log error and handle any further actions if needed
        }

        return savedUser;
    }

    public Map<String, String> login(String username, String password) throws MessagingException {
        User user = userRepository.findByUsername(username);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // Check if the user's account is enabled
            if (!user.isEnabled()) {
                // Resend the verification token since the user is not yet verified
                verificationService.resendVerificationToken(user);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not verified. A new verification email has been sent.");
            }

            // User is enabled, proceed with login
            String accessToken = jwtUtils.generateJwtToken(username);
            String refreshToken = jwtUtils.generateRefreshToken(username);

            jwtUtils.storeRefreshToken(refreshToken, username);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);

            return tokens;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");

    }


    // Other CRUD methods for managing user data...
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }


}
