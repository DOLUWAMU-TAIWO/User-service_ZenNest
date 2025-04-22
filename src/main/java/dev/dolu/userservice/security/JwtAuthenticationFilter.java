package dev.dolu.userservice.security;

import dev.dolu.userservice.metrics.CustomMetricService;
import dev.dolu.userservice.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Order(2)  // Processed after the API key filter
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());
    private final CustomMetricService customMetricService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService, CustomMetricService customMetricService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.customMetricService = customMetricService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Temporarily bypass JWT checks for all requests
        logger.info("JWT filter bypassed for: " + request.getRequestURI());
        filterChain.doFilter(request, response);
        return;

        /*
        String requestPath = request.getRequestURI();

        // Skip JWT authentication for public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token from Authorization header
            String jwt = getJwtFromRequest(request);

            // Check if JWT is blacklisted
            if (jwt != null && jwtUtils.isTokenBlacklisted(jwt)) {
                logger.log(Level.WARNING, "Attempt to use blacklisted token for request: {0}", request.getRequestURI());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated.");
                return;
            }

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Get the username and verify it
                String username = jwtUtils.getUsernameFromJwtToken(jwt);

                // Load user details to create authentication
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails != null) {
                    // Create authentication token and set the context
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.log(Level.INFO, "Authenticated user: {0}", username);
                } else {
                    logger.log(Level.WARNING, "User not found for token: {0}", jwt);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                }
            } else {
                logger.log(Level.WARNING, "Invalid or expired JWT for request: {0}", request.getRequestURI());
                customMetricService.incrementJwtValidationFailureCounter();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT is invalid or expired");
                return;
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error during JWT authentication: ", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during authentication");
            return;
        }

        filterChain.doFilter(request, response);
        */
    }

    // Helper method to extract JWT from the Authorization header
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Helper method to check if the endpoint is public
    private boolean isPublicEndpoint(String requestPath) {
        return requestPath.equals("/health") ||
                requestPath.startsWith("/actuator") ||
                requestPath.equals("/favicon.ico") ||
                requestPath.startsWith("/graphql") ||
                requestPath.startsWith("/graphiql");
    }
}