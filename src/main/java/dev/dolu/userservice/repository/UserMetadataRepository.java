package dev.dolu.userservice.repository;

import dev.dolu.userservice.models.UserMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserMetadataRepository extends JpaRepository<UserMetadata, UUID> {
    UserMetadata findByUserId(UUID userId);
}
