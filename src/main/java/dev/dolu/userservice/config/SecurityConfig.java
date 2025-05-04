package dev.dolu.userservice.config;

import dev.dolu.userservice.metrics.CustomMetricService;
import dev.dolu.userservice.security.JwtAuthenticationFilter;
import dev.dolu.userservice.utils.JwtUtils;
import dev.dolu.userservice.service.CustomUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the UserService.
 * <p>
 * Configures JWT-based authentication, stateless session management,
 * and secure CORS settings to allow requests from approved frontend origins.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomMetricService customMetricService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Enable CORS using corsConfigurationSource
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeRequests(authorize -> authorize
                        // ✅ Allow all OPTIONS requests (for CORS preflight)
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Your public endpoints
                        .requestMatchers(
                                "/health",
                                "/actuator/**",
                                "/error",
                                "/graphql",
                                "/graphiql",
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
                                "/api/users/refresh-token",
                                "/api/test-email"
                        ).permitAll()

                        // 🔐 Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 🔒 Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils, customUserDetailsService, customMetricService);
    }

    /**
     * CORS configuration for allowing trusted frontend origins and secure headers.
     *
     * @return CorsConfigurationSource for Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "https://vhsvcalumni.org",
                "https://vhsvcalumni.org/",  // 🔥 Handles buggy Origin header from iOS
                "https://qorelabs.online",
                "https://qorelabs.xyz",
                "https://qorelabs.space",
                "https://qorelabs.store"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "x-api-key"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}