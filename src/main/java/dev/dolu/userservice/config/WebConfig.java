package dev.dolu.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply CORS to all endpoints
                .allowedOrigins("https://Vhsvc-alumni.org","http://localhost:3000","http://qorelabs.org","http://localhost:7070","https://qorelabs.xyz" )// Add your specific allowed origin(s)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS","UPDATE","CREATE") // Allowed HTTP methods
                .allowedHeaders("*") // Allow all headers or specify only required ones
                .allowCredentials(true); // Allows cookies/auth headers if needed
    }
}
