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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // BCrypt bean for password hashing
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Main security configuration, now using JWT authentication
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity; enable for production
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Set to stateless for JWT usage
                )
                .authorizeRequests(authorize -> authorize
                .requestMatchers(
                        "/error",
                        "/api/users/register",
                        "/api/users/{id}",
                        "/api/users/login",
                        "/api/users/batch",
                        "/api/users/logout",
                        "/api/users/verify",
                        "/api/users/user-details",
                        "/api/users/refresh-token",
                        "/api/test-email"
                ).permitAll() // Publicly accessible endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Restricted to ADMIN role
                .anyRequest().authenticated() // Other requests require authentication
        )

                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class); // Add JWT filter

        return http.build();
    }

    // Define JwtAuthenticationFilter as a bean, passing in both jwtUtils and customUserDetailsService
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils, customUserDetailsService);
    }
}

