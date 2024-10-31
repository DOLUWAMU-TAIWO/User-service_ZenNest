package dev.dolu.userservice.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

    @Configuration
    public class RedisConfig {

        @Bean
        public LettuceConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory();
        }

        @Bean
        public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
            // Create a new instance of RedisTemplate with String keys and values
            RedisTemplate<String, String> template = new RedisTemplate<>();

            // Set the connection factory for the RedisTemplate
            template.setConnectionFactory(redisConnectionFactory);

            // Set the serializer for the keys to StringRedisSerializer
            template.setKeySerializer(new StringRedisSerializer());

            // Set the serializer for the values to StringRedisSerializer
            template.setValueSerializer(new StringRedisSerializer());

            // Return the configured RedisTemplate instance
            return template;
        }
    }


