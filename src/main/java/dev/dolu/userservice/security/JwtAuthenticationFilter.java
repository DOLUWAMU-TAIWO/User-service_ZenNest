package dev.dolu.userservice.security;

import dev.dolu.userservice.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    @Autowired
    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Skip JWT validation for login and registration endpoints
        if (requestPath.equals("/api/users/login") || requestPath.equals("/api/users/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token from Authorization header
            String jwt = getJwtFromRequest(request);

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
                }
            } else {
                logger.log(Level.WARNING, "JWT is invalid or expired for request: {0}", request.getRequestURI());
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Cannot set user authentication: ", ex);
        }

        // Proceed with the filter chain
        filterChain.doFilter(request, response);
    }


    // Helper method to extract JWT from the Authorization header
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
