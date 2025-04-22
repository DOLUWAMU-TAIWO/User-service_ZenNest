package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.*;
import dev.dolu.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class GraphqlController {
    // At the top of your GraphqlController class, add:
    private static final Logger logger = LoggerFactory.getLogger(GraphqlController.class);
    private final UserRepository userRepository;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public GraphqlController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get a user by ID
    @QueryMapping
    public User getUserById(@Argument UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    // Get all users
    @QueryMapping
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found.");
        }
        return users;
    }

    // Search users by partial username or email match
    @QueryMapping
    public List<User> searchUsers(@Argument String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query, query, query);
    }

    // Get users by role
    @QueryMapping
    public List<User> getUsersByRole(@Argument Role role) {
        List<User> users = userRepository.findByRole(role);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found with role: " + role);
        }
        return users;
    }

    // Get users by city
    @QueryMapping
    public List<User> getUsersByCity(@Argument String city) {
        List<User> users = userRepository.findByCity(city);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found in city: " + city);
        }
        return users;
    }

    // Get users by alumni status
    @QueryMapping
    public List<User> getUsersByAlumniStatus(@Argument AlumniStatus status) {
        List<User> users = userRepository.findByAlumniStatus(status);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found with alumni status: " + status);
        }
        return users;
    }

    // Get users by graduation year
    @QueryMapping
    public List<User> getUsersByGraduationYear(@Argument Integer graduationYear) {
        List<User> users = userRepository.findByGraduationYear(graduationYear);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found for graduation year: " + graduationYear);
        }
        return users;
    }

    // Get users by gender
    @QueryMapping
    public List<User> getUsersByGender(@Argument Gender gender) {
        List<User> users = userRepository.findByGender(gender);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found with gender: " + gender);
        }
        return users;
    }

    // Get recent users by creation date
    @QueryMapping
    public List<User> getRecentUsers() {
        List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No recent users found.");
        }
        return users;
    }

    @QueryMapping
    public List<User> getUsersByCountry(@Argument String country) {
        List<User> users = userRepository.findByCountry(country);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found in country: " + country);
        }
        return users;
    }

    @QueryMapping
    public User getUserByEmail(@Argument String email) {
        logger.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.warn("User not found with email: {}", email);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    @QueryMapping
    public int countUsers() {
        List<User> users = userRepository.findAll();
        int count = users.size();
        return count;
    }




    @MutationMapping
    public User createUser(@Argument String firstName, @Argument String lastName, @Argument String username,
                           @Argument String phoneNumber, @Argument String email, @Argument String password,
                           @Argument String dateOfBirth, @Argument Gender gender, @Argument String profession,
                           @Argument String city, @Argument String country, @Argument Integer graduationYear,
                           @Argument AlumniStatus alumniStatus, @Argument String chapter, @Argument Role role,
                           @Argument List<SocialMediaLinkInput> socialMediaLinks) {

        try {
            // Validate required fields
            if (firstName == null || lastName == null || username == null || email == null || password == null || gender == null) {
                throw new IllegalArgumentException("Missing required fields.");
            }

            // Check for duplicate username
            if (userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Username already exists: " + username);
            }

            // Check for duplicate email
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists: " + email);
            }

            // Check for duplicate phone number
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new IllegalArgumentException("Phone number already exists: " + phoneNumber);
            }

            // Handle alumniStatus with a default value
            if (alumniStatus == null) {
                alumniStatus = AlumniStatus.ACTIVE;
            }

            // Create a new user object (password is still raw here)
            User user = new User(firstName, lastName, username, phoneNumber, email, password,
                    gender, profession, city, country, graduationYear, chapter, role);

            // Set the date of birth if provided
            if (dateOfBirth != null) {
                user.setDateOfBirth(LocalDate.parse(dateOfBirth));
            }

            // Process social media links if provided
            if (socialMediaLinks != null && !socialMediaLinks.isEmpty()) {
                List<SocialMediaLink> links = socialMediaLinks.stream()
                        .map(link -> new SocialMediaLink(link.getPlatform(), link.getUrl()))
                        .collect(Collectors.toList());
                user.setSocialMediaLinks(links);
            }

            // Encode the raw password before saving
            user.setPassword(passwordEncoder.encode(password));

            // Save the user to the database
            User savedUser = userRepository.save(user);

            // Log success using the logger
            logger.info("User created successfully with ID: {}, Username: {}, Email: {}",
                    savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());

            return savedUser;

        } catch (IllegalArgumentException ex) {
            logger.error("Validation error: {}", ex.getMessage());
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            logger.error("Database constraint violation: {}", ex.getMessage());
            throw new RuntimeException("User creation failed due to a database error", ex);
        } catch (Exception ex) {
            logger.error("Unexpected error during user creation: {}", ex.getMessage());
            throw new RuntimeException("User creation failed due to an unexpected error", ex);
        }
    }

    @MutationMapping
    public User updateUser(@Argument UUID id, @Argument String firstName, @Argument String lastName,
                           @Argument String username, @Argument String phoneNumber, @Argument String email,
                           @Argument String password, @Argument String dateOfBirth, @Argument Gender gender,
                           @Argument String profession, @Argument String city, @Argument String country,
                           @Argument Integer graduationYear, @Argument AlumniStatus alumniStatus,
                           @Argument String chapter, @Argument Role role,
                           @Argument List<SocialMediaLinkInput> socialMediaLinks) {

        logger.info("Attempting to update user with ID: {}", id);

        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            logger.debug("Existing user data: {}", user);

            try {
                // Update fields if provided and log each update
                if (firstName != null) {
                    user.setFirstName(firstName);
                    logger.debug("Updated firstName to: {}", firstName);
                }
                if (lastName != null) {
                    user.setLastName(lastName);
                    logger.debug("Updated lastName to: {}", lastName);
                }
                if (username != null) {
                    user.setUsername(username);
                    logger.debug("Updated username to: {}", username);
                }
                if (phoneNumber != null) {
                    user.setPhoneNumber(phoneNumber);
                    logger.debug("Updated phoneNumber to: {}", phoneNumber);
                }
                if (email != null) {
                    user.setEmail(email);
                    logger.debug("Updated email to: {}", email);
                }
                if (password != null) {
                    // Encode the new password before updating
                    user.setPassword(passwordEncoder.encode(password));
                    logger.debug("Updated password (encoded).");
                }
                if (dateOfBirth != null) {
                    user.setDateOfBirth(LocalDate.parse(dateOfBirth));
                    logger.debug("Updated dateOfBirth to: {}", dateOfBirth);
                }
                if (gender != null) {
                    user.setGender(gender);
                    logger.debug("Updated gender to: {}", gender);
                }
                if (profession != null) {
                    user.setProfession(profession);
                    logger.debug("Updated profession to: {}", profession);
                }
                if (city != null) {
                    user.setCity(city);
                    logger.debug("Updated city to: {}", city);
                }
                if (country != null) {
                    user.setCountry(country);
                    logger.debug("Updated country to: {}", country);
                }
                if (graduationYear != null) {
                    user.setGraduationYear(graduationYear);
                    logger.debug("Updated graduationYear to: {}", graduationYear);
                }
                if (alumniStatus != null) {
                    user.setAlumniStatus(alumniStatus);
                    logger.debug("Updated alumniStatus to: {}", alumniStatus);
                }
                if (chapter != null) {
                    user.setChapter(chapter);
                    logger.debug("Updated chapter to: {}", chapter);
                }
                if (role != null) {
                    user.setRole(role);
                    logger.debug("Updated role to: {}", role);
                }
                // Update social media links if provided
                if (socialMediaLinks != null) {
                    user.getSocialMediaLinks().clear();
                    for (SocialMediaLinkInput link : socialMediaLinks) {
                        user.getSocialMediaLinks().add(new SocialMediaLink(link.getPlatform(), link.getUrl()));
                        logger.debug("Added social media link: {} - {}", link.getPlatform(), link.getUrl());
                    }
                }
            } catch (Exception ex) {
                logger.error("Error updating user {}: {}", id, ex.getMessage());
                throw ex; // Let the global exception handler deal with it
            }

            User updatedUser = userRepository.save(user);
            logger.info("User updated successfully: {}", updatedUser.getId());
            return updatedUser;
        } else {
            logger.warn("User with ID {} not found for update", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @MutationMapping
    public boolean deleteUser(@Argument UUID id) {
        logger.info("Attempting to delete user with ID: {}", id);
        if (userRepository.existsById(id)) {
            try {
                userRepository.deleteById(id);
                logger.info("User deleted successfully with ID: {}", id);
                return true;
            } catch (Exception ex) {
                logger.error("Error deleting user {}: {}", id, ex.getMessage());
                throw ex; // This will be handled by the global exception handler
            }
        } else {
            logger.warn("User with ID {} not found for deletion", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}