package dev.dolu.userservice.service;

import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Autowired
    public UserService(UserRepository userRepository, JwtUtils jwtUtils) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
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

        // Save the user to the database with the hashed password and return the saved entity
        return userRepository.save(user);
    }

    public String login(String username, String password) {
        // Fetch the user by username
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // If credentials match, generate a JWT token for the user
            return jwtUtils.generateJwtToken(username);
        }
        // Return null or throw an exception if authentication fails
        return null;
    }
    // Other CRUD methods for managing user data...
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }


}
