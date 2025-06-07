package dev.dolu.userservice.repository;

import dev.dolu.userservice.models.UserSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface UserSearchRepository extends JpaRepository<UserSearch, Long> {
    List<UserSearch> findByUserId(java.util.UUID userId);
}

