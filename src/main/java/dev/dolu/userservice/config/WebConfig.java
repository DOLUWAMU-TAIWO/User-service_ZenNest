package dev.dolu.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "https://vhsvc-alumni.org",
                        "https://qorelabs.online",
                        "https://qorelabs.xyz",
                        "https://qorelabs.space",
                        "https://qorelabs.store"
                )
                .allowedMethods("GET", "POST", "OPTIONS", "PUT", "DELETE")
                .allowedHeaders("Authorization", "Content-Type", "x-api-key") // ✅ critical fix
                .allowCredentials(true)
                .maxAge(3600);
    }
}