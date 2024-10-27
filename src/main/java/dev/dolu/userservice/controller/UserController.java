package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.User;
import dev.dolu.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Constructor-based dependency injection for UserService
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody User loginRequest) {
        // Call the login method from UserService
        String token = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

        if (token != null) {
            // Return token upon successful login
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            // Return structured error message
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
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
}
