package dev.dolu.userservice;

import dev.dolu.userservice.models.User;
import dev.dolu.userservice.service.UserService;
import dev.dolu.userservice.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import java.util.List;

import static dev.dolu.userservice.models.Role.ADMIN;

@SpringBootApplication
public class UserServiceApplication {
    private static final Logger log = LoggerFactory.getLogger(UserServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);


    }
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 5 seconds connect timeout
        requestFactory.setReadTimeout(5000);    // 5 seconds read timeout
        return new RestTemplate(requestFactory);
    }

}
