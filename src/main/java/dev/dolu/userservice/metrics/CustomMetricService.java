package dev.dolu.userservice.metrics;

import dev.dolu.userservice.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class CustomMetricService {

    public CustomMetricService(UserRepository userRepository, MeterRegistry meterRegistry) {
        // Register a gauge metric named "users.total" that calls userRepository.count()
        Gauge.builder("users.total", userRepository, UserRepository::count)
                .description("Total number of users")
                .register(meterRegistry);
    }
}