package dev.dolu.userservice.utils;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JwtUtils {

    private static final Logger logger = Logger.getLogger(JwtUtils.class.getName());

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    /**
     * Generates a JWT token for a given username.
     *
     * @param username The username for which the JWT is generated.
     * @return A signed JWT token.
     */
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

    /**
     * Extracts the username from a given JWT token.
     *
     * @param token The JWT token.
     * @return The username embedded in the token.
     */
    public String getUsernameFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Validates the JWT token.
     *
     * @param token The JWT token.
     * @return True if the token is valid; otherwise, false.
     */
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            logger.log(Level.SEVERE, "Invalid JWT signature: {0}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.log(Level.SEVERE, "Invalid JWT token: {0}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.log(Level.WARNING, "JWT token is expired: {0}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.log(Level.SEVERE, "JWT token is unsupported: {0}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "JWT claims string is empty: {0}", e.getMessage());
        }
        return false;
    }
}
