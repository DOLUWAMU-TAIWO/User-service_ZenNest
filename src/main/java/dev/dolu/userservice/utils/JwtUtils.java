package dev.dolu.userservice.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JwtUtils {

    private static final Logger logger = Logger.getLogger(JwtUtils.class.getName());

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    @Value("${jwt.refreshExpirationMs}")
    private long refreshExpirationMs;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }

    public String generateJwtToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateJwtToken(UUID id, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> userClaim = new HashMap<>();
        userClaim.put("id", id.toString());
        userClaim.put("email", email);
        userClaim.put("role", role);
        claims.put("user", userClaim);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // Generate magic link token (shorter expiration for security)
    public String generateMagicLinkToken(UUID userId, String email, String role) {
        try {
            Map<String, Object> claims = new HashMap<>();
            Map<String, Object> userClaim = new HashMap<>();
            userClaim.put("id", userId.toString());
            userClaim.put("email", email);
            userClaim.put("role", role);
            userClaim.put("type", "MAGIC_LINK"); // Mark as magic link token
            claims.put("user", userClaim);

            // Magic link tokens expire in 30 minutes for security
            long magicLinkExpiration = 30 * 60 * 1000; // 30 minutes

            return Jwts.builder()
                    .claims(claims)
                    .subject(email)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + magicLinkExpiration))
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to generate magic link token: {0}", e.getMessage());
            return null;
        }
    }

    // Check if token is a magic link token
    public boolean isMagicLinkToken(String token) {
        try {
            Map<String, Object> userDetails = getUserDetailsFromJwtToken(token);
            return userDetails != null && "MAGIC_LINK".equals(userDetails.get("type"));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error checking magic link token: {0}", e.getMessage());
            return false;
        }
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Invalid JWT token: {0}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public void storeRefreshToken(String refreshToken, String username) {
        redisTemplate.opsForValue().set("refresh_" + username, refreshToken, refreshExpirationMs, TimeUnit.MILLISECONDS);
    }

    public boolean isRefreshTokenValid(String username, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get("refresh_" + username);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public long getExpirationFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public void blacklistToken(String token, long expiration, TimeUnit unit) {
        redisTemplate.opsForValue().set(token, "blacklisted", expiration, unit);
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Refresh token is invalid: {0}", e.getMessage());
        }
        return false;
    }

    public Map<String, Object> getUserDetailsFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return (Map<String, Object>) claims.get("user");
    }

    // For testing only
    void setJwtSecret(String secret) { this.jwtSecret = secret; }
    void setJwtExpirationMs(long ms) { this.jwtExpirationMs = ms; }
    void setRefreshExpirationMs(long ms) { this.refreshExpirationMs = ms; }
}
