package dev.dolu.userservice.config;

import dev.dolu.userservice.security.JwtAuthenticationFilter;
import dev.dolu.userservice.utils.JwtUtils;
import dev.dolu.userservice.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures the application's security filter chain.
 * <p>
 * This configuration uses JWT-based authentication for a stateless API.
 * It disables CSRF protection and sets session management to stateless.
 * A custom JwtAuthenticationFilter is added before the standard UsernamePasswordAuthenticationFilter.
 * <p>
 * The authorization rules permit access to certain endpoints (e.g., login, register, actuator endpoints, favicon, etc.)
 * while requiring authentication for any endpoints not explicitly permitted.
 * Additionally, endpoints under "/api/admin/**" are restricted to users with the ADMIN role.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * BCryptPasswordEncoder bean used for hashing passwords.
     * 
     * @return a new BCryptPasswordEncoder instance.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the security filter chain.
     * <p>
     * - CSRF is disabled as the API is stateless.
     * - Session management is set to STATELESS.
     * - Specific endpoints are permitted without authentication.
     * - Endpoints under "/api/admin/**" require the ADMIN role.
     * - All other endpoints require authentication.
     * - The custom JwtAuthenticationFilter is added before the UsernamePasswordAuthenticationFilter.
     *
     * @param http the HttpSecurity object to configure.
     * @return the configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless APIs
            .csrf(csrf -> csrf.disable())
            // Set session management to stateless (no session will be created or used)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Configure endpoint authorization
            .authorizeRequests(authorize -> authorize
                // Publicly accessible endpoints
                .requestMatchers(
                        "/error",
                        "/graphql",
                        "graphiql",
                        "/api/users/register",
                        "/api/users/{id}",
                        "/api/users/login",
                        "/api/users/batch",
                        "/api/users/logout",
                        "/api/users/verify",
                        "/",
                        "/favicon.ico",
                        "/api/users/all",
                        "/api/users/search",
                        "/api/users/verify-email",
                        "/api/users/forgot-password",
                        "/api/users/reset-password",
                        "/api/users/resend-verification",
                        "/health",
                        "/actuator/**",
                        "/api/users/refresh-token",
                        "/api/test-email"
                ).permitAll()
                // Endpoints under /api/admin/** require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            // Add the custom JWT authentication filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates an instance of JwtAuthenticationFilter, passing in the required dependencies.
     * 
     * @return a new JwtAuthenticationFilter instance.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils, customUserDetailsService);
    }
}
