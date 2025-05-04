package dev.dolu.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)  // Highest priority to be processed first
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class);
    private static final String API_KEY = System.getenv("SERVICE_PASSWORD");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // ✅ Skip health checks, actuator, and ALL OPTIONS (CORS preflight)
        return request.getMethod().equalsIgnoreCase("OPTIONS")
                || path.equals("/health")
                || path.startsWith("/actuator");
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || !API_KEY.equals(apiKey)) {
            logger.warn("Unauthorized request - Missing or invalid API key");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized - Missing or invalid API key");
            return;
        }

        logger.info("API key authenticated successfully for: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}