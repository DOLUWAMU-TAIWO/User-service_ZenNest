package dev.dolu.userservice.config;

import dev.dolu.userservice.metrics.CustomMetricService;
import dev.dolu.userservice.security.ApiKeyFilter;
import dev.dolu.userservice.security.JwtAuthenticationFilter;
import dev.dolu.userservice.service.CustomOAuth2UserService;
import dev.dolu.userservice.service.CustomUserDetailsService;
import dev.dolu.userservice.utils.JwtUtils;
import dev.dolu.userservice.service.OAuth2LoginSuccessHandler;

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

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private JwtUtils jwtUtils;
    @Autowired private CustomUserDetailsService customUserDetailsService;
    @Autowired private CustomMetricService customMetricService;
    @Autowired private ApiKeyFilter apiKeyFilter;
    @Autowired private CustomOAuth2UserService customOAuth2UserService;
    @Autowired private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "https://vhsvcalumni.org",
                "http://localhost:5173",
                "https://qorelabs.online",
                "https://qorelabs.xyz",
                "https://qorelabs.space",
                "https://zennest.ng",
                "https://zennest.dev",
                "https://zennest.live",
                "https://qorelabs.store"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-API-KEY"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", config);
        return src;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/health", "/actuator/**", "/error",
                                "/graphql", "/graphiql",
                                "/api/users/register", "/api/users/{id}",
                                "/api/users/login", "/api/users/batch",
                                "/api/users/logout", "/api/users/verify",
                                "/", "/favicon.ico",
                                "/api/users/all", "/api/users/search",
                                "/api/users/verify-email", "/api/users/forgot-password",
                                "/api/users/reset-password", "/api/users/resend-verification",
                                "/api/users/refresh-token", "/api/test-email",
                                "/oauth2/**", "/login/oauth2/**"
                        ).permitAll()
                        .requestMatchers("/api/users/*/payout-info").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
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