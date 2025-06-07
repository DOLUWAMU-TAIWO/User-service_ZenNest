package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.UserSearch;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.repository.UserSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-searches")
public class UserSearchController {

    private final UserSearchRepository userSearchRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserSearchController(UserSearchRepository userSearchRepository, UserRepository userRepository) {
        this.userSearchRepository = userSearchRepository;
        this.userRepository = userRepository;
    }

    // Create a new search
    @PostMapping
    public ResponseEntity<?> createSearch(@RequestBody UserSearch search, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        search.setUser(user);
        search.setSearchedAt(LocalDateTime.now());
        UserSearch saved = userSearchRepository.save(search);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get all searches for the authenticated user
    @GetMapping
    public ResponseEntity<?> getUserSearches(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        List<UserSearch> searches = userSearchRepository.findByUserId(user.getId());
        return ResponseEntity.ok(searches);
    }

    // Get a specific search by ID (only if it belongs to the user)
    @GetMapping("/{id}")
    public ResponseEntity<?> getSearchById(@PathVariable Long id, Principal principal) {
        Optional<UserSearch> searchOpt = userSearchRepository.findById(id);
        if (searchOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Search not found");
        }
        UserSearch search = searchOpt.get();
        if (principal == null || !search.getUser().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        return ResponseEntity.ok(search);
    }

    // Delete a search (only if it belongs to the user)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSearch(@PathVariable Long id, Principal principal) {
        Optional<UserSearch> searchOpt = userSearchRepository.findById(id);
        if (searchOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Search not found");
        }
        UserSearch search = searchOpt.get();
        if (principal == null || !search.getUser().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        userSearchRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // (Optional) Admin: List all searches
    @GetMapping("/all")
    public ResponseEntity<List<UserSearch>> getAllSearches() {
        return ResponseEntity.ok(userSearchRepository.findAll());
    }
}

