package dev.dolu.userservice.config;

import dev.dolu.userservice.metrics.CustomMetricService;
import dev.dolu.userservice.security.ApiKeyFilter;
import dev.dolu.userservice.security.JwtAuthenticationFilter;
import dev.dolu.userservice.service.CustomUserDetailsService;
import dev.dolu.userservice.utils.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private JwtUtils jwtUtils;
    @Autowired private CustomUserDetailsService customUserDetailsService;
    @Autowired private CustomMetricService customMetricService;
    @Autowired private ApiKeyFilter apiKeyFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 1) Define your CORS rules
    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://vhsvcalumni.org",
                "http://localhost:5173",
                "https://qorelabs.online",
                "https://qorelabs.xyz",
                "https://qorelabs.space",
                "https://qorelabs.store"
        ));
        config.setAllowedMethods(List.of("GET","POST","OPTIONS","PUT","DELETE"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type","X-API-KEY"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // 2) Expose a CorsFilter so it runs BEFORE Spring Security
    @Bean
    public CorsFilter corsFilter(CorsConfigurationSource source) {
        return new CorsFilter(source);
    }

    // 3) Security chain: add CorsFilter first, then your filters
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsFilter corsFilter) throws Exception {
        http
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/health","/actuator/**","/error",
                                "/graphql","/graphiql",
                                "/api/users/register","/api/users/{id}",
                                "/api/users/login","/api/users/batch",
                                "/api/users/logout","/api/users/verify",
                                "/","/favicon.ico",
                                "/api/users/all","/api/users/search",
                                "/api/users/verify-email","/api/users/forgot-password",
                                "/api/users/reset-password","/api/users/resend-verification",
                                "/api/users/refresh-token","/api/test-email"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter(), ApiKeyFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils, customUserDetailsService, customMetricService);
    }
}