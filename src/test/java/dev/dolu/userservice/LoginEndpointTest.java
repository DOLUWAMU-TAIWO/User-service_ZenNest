package dev.dolu.userservice;

import dev.dolu.userservice.models.LoginRequest;
import dev.dolu.userservice.models.Role;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.repository.VerificationTokenRepository;
import dev.dolu.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LoginEndpointTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @MockBean
    private JavaMailSender mailSender;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Clear the verification_token table
        verificationTokenRepository.deleteAll();
        verificationTokenRepository.flush();

        // Clear the users table
        userRepository.deleteAll();
        userRepository.flush();

        // Reset the TestRestTemplate
        restTemplate = restTemplate.withBasicAuth(null, null);
        restTemplate.getRestTemplate().getInterceptors().clear();

        System.out.println("User count after setup: " + userRepository.count());
    }






    @Test
    void shouldLoginSuccessfully() {
        // Arrange: Create and save a verified user
        User user = new User();
        user.setUsername("twale");
        user.setEmail("modothegreatest@gmail.com");
        user.setPassword(passwordEncoder.encode("AdminPasswo")); // Save hashed password
        user.setEnabled(true); // Simulate verified user
        user.setRole(Role.USER);
        userRepository.save(user);

        // Prepare the login request
        LoginRequest loginRequest = new LoginRequest("twale", "AdminPasswo");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/users/login", loginRequest, Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> tokens = response.getBody();
        assertThat(tokens).containsKeys("accessToken", "refreshToken");
    }

    @Test
    void shouldLoginSuccessfully2() {
        // Arrange: Create and save a verified user
        User user = new User();
        user.setUsername("demilade");
        user.setEmail("badd@gmail.com");
        user.setPassword(passwordEncoder.encode("AdminPassword")); // Save hashed password
        user.setEnabled(true); // Simulate verified user
        user.setRole(Role.USER);
        userRepository.save(user);

        // Prepare the login request
        LoginRequest loginRequest = new LoginRequest("twale", "AdminPasswo");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/users/login", loginRequest, Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> tokens = response.getBody();
        assertThat(tokens).containsKeys("accessToken", "refreshToken");
    }





}
