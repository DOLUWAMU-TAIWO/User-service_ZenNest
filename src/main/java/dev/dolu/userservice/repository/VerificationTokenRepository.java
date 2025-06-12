package dev.dolu.userservice.repository;

import dev.dolu.userservice.models.User;
import dev.dolu.userservice.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    void deleteByUser(User user);
    // Useful for removing old tokens

   Optional<VerificationToken> findByUser(User user);
    // Corrected: Should return an Optional to handle missing tokens
}
