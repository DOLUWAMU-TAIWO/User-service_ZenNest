package dev.dolu.userservice.repository;

import dev.dolu.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query methods for production-grade functionality
    User findByEmail(String email);
    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
