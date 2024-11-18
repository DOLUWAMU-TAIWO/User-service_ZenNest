package dev.dolu.userservice;

import dev.dolu.userservice.models.User; // Import your User entity
import dev.dolu.userservice.repository.UserRepository; // Import the User repository
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the /register endpoint.
 * This class tests the registration workflow end-to-end,
 * including database interactions.
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RegisterEndpointTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRegisterUserSuccessfully() {
        String registerUrl = "/api/users/register";
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("testuser@example.com");
        newUser.setPassword("SecurePassword123!");

        ResponseEntity<String> response = restTemplate.postForEntity(registerUrl, newUser, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        User savedUser = userRepository.findByUsername("testuser");
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEnabled()).isFalse();
        assertThat(savedUser.getPassword()).isNotEqualTo("SecurePassword123!");
    }

    @Test
    void shouldNotRegisterUserWhenUserAlreadyExists() {
        String registerUrl = "/api/users/register";
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("exisitnguser@example.com");
        newUser.setPassword("SecurePassword123!");
        userRepository.save(newUser);

        User duplicateUser = new User();
        newUser.setUsername("duplicateuser");
        newUser.setEmail("exisitnguser@example.com");
        newUser.setPassword("SecurePassword123!");


        ResponseEntity<String> response = restTemplate.postForEntity(registerUrl,duplicateUser,String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        // Arrange: Create a user with an invalid email
        User invalidEmailUser = new User();
        invalidEmailUser.setUsername("invalidemailuser");
        invalidEmailUser.setEmail("not-an-email"); // Invalid email
        invalidEmailUser.setPassword("SecurePassword123!");

        // Act: Attempt to register the user
        ResponseEntity<String> response = restTemplate.postForEntity("/api/users/register", invalidEmailUser, String.class);

        // Assert: Verify that the response status is 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldRejectRegistrationWithMissingFields() {
        // Arrange: Create a user with missing fields
        User invalidUser = new User();
        invalidUser.setUsername(""); // Empty username
        invalidUser.setEmail("");    // Empty email
        invalidUser.setPassword(""); // Empty password

        // Act: Attempt to register the user
        ResponseEntity<String> response = restTemplate.postForEntity("/api/users/register", invalidUser, String.class);

        // Assert: Verify that the response status is 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }




}
