package dev.dolu.userservice.utils;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }

    public String generateJwtToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Invalid JWT token: {0}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
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
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public void blacklistToken(String token, long expiration, TimeUnit unit) {
        redisTemplate.opsForValue().set(token, "blacklisted", expiration, unit);
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Refresh token is invalid: {0}", e.getMessage());
        }
        return false;
    }

    // For testing only
    void setJwtSecret(String secret) { this.jwtSecret = secret; }
    void setJwtExpirationMs(long ms) { this.jwtExpirationMs = ms; }
    void setRefreshExpirationMs(long ms) { this.refreshExpirationMs = ms; }
}
