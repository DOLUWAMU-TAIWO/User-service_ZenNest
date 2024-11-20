package dev.dolu.userservice;

import dev.dolu.userservice.models.User; // Import your User entity
import dev.dolu.userservice.models.VerificationToken;
import dev.dolu.userservice.repository.UserRepository; // Import the User repository
import dev.dolu.userservice.repository.VerificationTokenRepository;
import dev.dolu.userservice.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the /register endpoint.
 * This class tests the registration workflow end-to-end,
 * including database interactions.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RegisterEndpointTest extends BaseIntegrationTest {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @MockBean
    private EmailService emailService;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void clearDatabase() {
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
        assertThat(userRepository.findAll()).isEmpty();
        assertThat(verificationTokenRepository.findAll()).isEmpty();
    }




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
    void shouldRejectInvalidEmailFormat() {
        User invalidEmailUser = new User();
        invalidEmailUser.setUsername("invalidemailuser");
        invalidEmailUser.setEmail("not-an-email");
        invalidEmailUser.setPassword("SecurePassword123!");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/users/register", invalidEmailUser, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldRejectRegistrationWithMissingFields() {
        User invalidUser = new User();
        invalidUser.setUsername("");
        invalidUser.setEmail("");
        invalidUser.setPassword("");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/users/register", invalidUser, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Rollback
    void shouldGenerateVerificationTokenAfterRegistration() {
        // Define the register endpoint
        String registerUrl = "/api/users/register";

        // Create a new user to register
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("testuser@example.com");
        newUser.setPassword("SecurePassword123!");

        // Call the register endpoint
        ResponseEntity<String> response = restTemplate.postForEntity(registerUrl, newUser, String.class);

        // Assert that the registration is successful
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Fetch the saved user from the database
        User savedUser = userRepository.findByUsername("testuser");
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEnabled()).isFalse(); // Ensure the user is not enabled by default

        // Fetch the verification token linked to the user
        VerificationToken token = verificationTokenRepository.findByUser(savedUser);

        // Assert that the token is generated and linked to the correct user
        assertThat(token).isNotNull();
        assertThat(token.getUser()).isEqualTo(savedUser);
        assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now()); // Ensure the token is not expired
    }



    @Test
    void testRepository() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        userRepository.save(user);

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setToken("sample-token");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        verificationTokenRepository.save(token);

        VerificationToken fetchedToken = verificationTokenRepository.findByUser(user);
        assertThat(fetchedToken).isNotNull();
    }
//    @DirtiesContext
//
//    @Test
//    void shouldNotRegisterUserWhenUserAlreadyExists() {
//        String registerUrl = "/api/users/register";
//        User existingUser = new User();
//        existingUser.setUsername("testuser");
//        existingUser.setEmail("existinguser@example.com");
//        existingUser.setPassword("SecurePassword123!");
//        userRepository.save(existingUser);
//
//        User duplicateUser = new User();
//        duplicateUser.setUsername("duplicateuser");
//        duplicateUser.setEmail("existinguser@example.com");
//        duplicateUser.setPassword("SecurePassword123!");
//
//        ResponseEntity<String> response = restTemplate.postForEntity(registerUrl, duplicateUser, String.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
//    }
}