package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.Role;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.service.UserService;
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

@Controller
public class GraphqlController {

    private static final Logger logger = LoggerFactory.getLogger(GraphqlController.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserService userService;

    @Autowired
    public GraphqlController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }


    @QueryMapping
    public User getUserById(@Argument UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @QueryMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @QueryMapping
    public List<User> searchUsers(@Argument String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query, query, query, query);
    }

    @QueryMapping
    public List<User> getUsersByRole(@Argument Role role) {
        return userRepository.findByRole(role);
    }

    @QueryMapping
    public List<User> getUsersByCity(@Argument String city) {
        return userRepository.findByCity(city);
    }

    @QueryMapping
    public List<User> getUsersByCountry(@Argument String country) {
        return userRepository.findByCountry(country);
    }

    @QueryMapping
    public List<User> getRecentUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @QueryMapping
    public User getUserByEmail(@Argument String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    @QueryMapping
    public int countUsers() {
        return userRepository.findAll().size();
    }

    @MutationMapping
    public User createUser(@Argument String firstName, @Argument String lastName, @Argument String username,
                           @Argument String phoneNumber, @Argument String email, @Argument String password,
                           @Argument String dateOfBirth, @Argument String profession,
                           @Argument String city, @Argument String country, @Argument Role role) {
        try {
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername((username == null || username.isBlank()) ? null : username);
            user.setPhoneNumber(phoneNumber);
            user.setEmail(email);
            user.setPassword(password); // let service encode
            user.setProfession(profession);
            user.setCity(city);
            user.setCountry(country);
            user.setRole(role != null ? role : Role.USER);
            user.setEnabled(false); // let service handle verification

            if (dateOfBirth != null) user.setDateOfBirth(LocalDate.parse(dateOfBirth));

            return (User) userService.registerUser(user).get("user");
        } catch (Exception e) {
            logger.error("User creation failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @MutationMapping
    public User updateUser(@Argument UUID id, @Argument String firstName, @Argument String lastName,
                           @Argument String username, @Argument String phoneNumber, @Argument String email,
                           @Argument String password, @Argument String dateOfBirth,
                           @Argument String profession, @Argument String city, @Argument String country,
                           @Argument Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (username != null) user.setUsername(username);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
        if (email != null) user.setEmail(email);
        if (password != null) user.setPassword(passwordEncoder.encode(password));
        if (dateOfBirth != null) user.setDateOfBirth(LocalDate.parse(dateOfBirth));
        if (profession != null) user.setProfession(profession);
        if (city != null) user.setCity(city);
        if (country != null) user.setCountry(country);
        if (role != null) user.setRole(role);

        return userRepository.save(user);
    }

    @MutationMapping
    public boolean deleteUser(@Argument UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
        return true;
    }
}