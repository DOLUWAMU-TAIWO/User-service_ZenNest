package dev.dolu.userservice.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilsTest {
    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private String secret = "testSecretKey1234567890abcdefghijklmnopqrstuvwxyz"; // Longer key for HS512
    private long expirationMs = 3600000; // 1 hour
    private long refreshExpirationMs = 7200000; // 2 hours

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtils.setJwtSecret(secret);
        jwtUtils.setJwtExpirationMs(expirationMs);
        jwtUtils.setRefreshExpirationMs(refreshExpirationMs);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGenerateAndParseJwtTokenWithEmail() {
        String email = "test@example.com";
        String token = jwtUtils.generateJwtToken(email);
        assertNotNull(token);
        String parsedEmail = jwtUtils.getUsernameFromJwtToken(token);
        assertEquals(email, parsedEmail);
    }

    @Test
    void testValidateJwtToken() {
        String email = "test@example.com";
        String token = jwtUtils.generateJwtToken(email);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertFalse(jwtUtils.validateJwtToken(token + "invalid"));
    }

    @Test
    void testGenerateAndParseRefreshTokenWithEmail() {
        String email = "test@example.com";
        String refreshToken = jwtUtils.generateRefreshToken(email);
        assertNotNull(refreshToken);
        String parsedEmail = jwtUtils.getUsernameFromJwtToken(refreshToken);
        assertEquals(email, parsedEmail);
    }

    @Test
    void testStoreAndValidateRefreshToken() {
        String email = "test@example.com";
        String refreshToken = jwtUtils.generateRefreshToken(email);
        when(valueOperations.get("refresh_" + email)).thenReturn(refreshToken);
        jwtUtils.storeRefreshToken(refreshToken, email);
        assertTrue(jwtUtils.isRefreshTokenValid(email, refreshToken));
        assertFalse(jwtUtils.isRefreshTokenValid(email, "wrongToken"));
    }

    @Test
    void testGenerateJwtTokenWithUserDetails() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String role = "TENANT";

        String token = jwtUtils.generateJwtToken(userId, email, role);
        assertNotNull(token);

        // Test that we can extract the user details
        Map<String, Object> userDetails = jwtUtils.getUserDetailsFromJwtToken(token);
        assertNotNull(userDetails);
        assertEquals(userId.toString(), userDetails.get("id"));
        assertEquals(email, userDetails.get("email"));
        assertEquals(role, userDetails.get("role"));

        // Test that the subject is still the email
        String parsedEmail = jwtUtils.getUsernameFromJwtToken(token);
        assertEquals(email, parsedEmail);
    }
}
