package dev.dolu.userservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest
public class UserServiceApplicationTests {

    private static final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:6-alpine").withExposedPorts(6379);

    static {
        redisContainer.start();
        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty("spring.data.redis.port", redisContainer.getFirstMappedPort().toString());
    }

    // Your test methods here
}
