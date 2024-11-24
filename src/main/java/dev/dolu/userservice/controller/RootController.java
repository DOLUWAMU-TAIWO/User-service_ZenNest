package dev.dolu.userservice.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class RootController {

    @GetMapping("/")
    public ResponseEntity<String> rootEndpoint() {
        return ResponseEntity.ok("Welcome to User Service!");
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is up and running!");
    }
}
