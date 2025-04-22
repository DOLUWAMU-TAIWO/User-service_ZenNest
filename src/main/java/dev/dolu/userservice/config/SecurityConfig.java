package dev.dolu.userservice.config;

import dev.dolu.userservice.metrics.CustomMetricService;
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

    @Autowired
    private CustomMetricService customMetricService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeRequests(authorize -> authorize
                        .requestMatchers(
                                "/health",
                                "/actuator/**"
                        ).permitAll()
                        .requestMatchers(
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
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils, customUserDetailsService, customMetricService);
    }
}
