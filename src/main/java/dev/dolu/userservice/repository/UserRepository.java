package dev.dolu.userservice.repository;

import dev.dolu.userservice.models.AlumniStatus;
import dev.dolu.userservice.models.Gender;
import dev.dolu.userservice.models.Role;
import dev.dolu.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Custom query methods for production-grade functionality

    // Find by Email
    User findByEmail(String email);

    // Find by Username
    User findByUsername(String username);

    // Check existence by Username
    boolean existsByUsername(String username);

    // Check existence by Email
    boolean existsByEmail(String email);

    // Search by partial match on username or email (case-insensitive)
    // Search by partial match on username, email, first name, or last name (case-insensitive)
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String username, String email, String firstName, String lastName);
    // Find users by role
    List<User> findByRole(Role role);

    // Find users by city
    List<User> findByCity(String city);

    // Find users by alumni status
    List<User> findByAlumniStatus(AlumniStatus status);

    // Find users by graduation year
    List<User> findByGraduationYear(Integer graduationYear);

    // Find users by gender
    List<User> findByGender(Gender gender);

    List<User> findByCountry(String country);

    // Get all users sorted by creation date
    List<User> findAllByOrderByCreatedAtDesc();

    // Check existence by Phone Number
    boolean existsByPhoneNumber(String phoneNumber);

}