package dev.dolu.userservice.repository;

import dev.dolu.userservice.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find by Email
    User findByEmail(String email);

    // Find by Username
    User findByUsername(String username);

    // Check if Username exists
    boolean existsByUsername(String username);

    // Check if Email exists
    boolean existsByEmail(String email);

    // Check if Phone Number exists
    boolean existsByPhoneNumber(String phoneNumber);

    // Search by partial match across fields (for admin use)
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String username, String email, String firstName, String lastName
    );

    // Filter by Role
    List<User> findByRole(Role role);

    // Filter by Auth Provider
    List<User> findByAuthProvider(AuthProvider authProvider);

    // Filter by City
    List<User> findByCity(String city);

    // Filter by Country
    List<User> findByCountry(String country);

    // Sort all users by latest created
    List<User> findAllByOrderByCreatedAtDesc();
}