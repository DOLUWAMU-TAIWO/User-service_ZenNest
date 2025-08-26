package dev.dolu.userservice.controller;


import dev.dolu.userservice.exception.BadRequestException;
import dev.dolu.userservice.exception.NotFoundException;
import dev.dolu.userservice.models.OnboardingFeature;
import dev.dolu.userservice.models.PayoutInfo;
import dev.dolu.userservice.models.Role;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.models.UserIntention;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static graphql.ErrorType.DataFetchingException;

@Controller
public class GraphqlController {

    private static final Logger logger = LoggerFactory.getLogger(GraphqlController.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserService userService;

    @Autowired
    public GraphqlController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }


    @QueryMapping
    public User getUserById(@Argument UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @QueryMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @QueryMapping
    public List<User> searchUsers(@Argument String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query, query, query, query);
    }

    @QueryMapping
    public List<User> getUsersByRole(@Argument Role role) {
        return userRepository.findByRole(role);
    }

    @QueryMapping
    public List<User> getUsersByCity(@Argument String city) {
        return userRepository.findByCity(city);
    }

    @QueryMapping
    public List<User> getUsersByCountry(@Argument String country) {
        return userRepository.findByCountry(country);
    }

    @QueryMapping
    public List<User> getRecentUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @QueryMapping
    public User getUserByEmail(@Argument String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    @QueryMapping
    public int countUsers() {
        return userRepository.findAll().size();
    }

    @MutationMapping
    public User createUser(
            @Argument String firstName, @Argument String lastName, @Argument String username,
            @Argument String phoneNumber, @Argument String email, @Argument String password,
            @Argument String dateOfBirth, @Argument String profession,
            @Argument String city, @Argument String country, @Argument Role role,
            @Argument UserIntention intention, @Argument String profileDescription,
            @Argument String profilePicture, @Argument List<UUID> favourites,
            // New fields
            @Argument Boolean profileCompleted, @Argument Boolean onboardingCompleted,
            @Argument String subscriptionPlan, @Argument Boolean subscriptionActive,
            @Argument Boolean openVisitations, @Argument Boolean paymentVerified,
            @Argument Double totalEarnings, @Argument Set<OnboardingFeature> completedFeatures,
            // PayoutInfo fields
            @Argument String payoutAccountNumber, @Argument String payoutBankCode,
            @Argument String payoutBankName, @Argument String payoutAccountHolderName,
            @Argument String payoutRecipientCode, @Argument String payoutBvn,
            @Argument String payoutEmailForPayouts, @Argument Boolean payoutVerified,
            @Argument String payoutCurrency, @Argument String payoutLastUpdated
    ) {
        try {
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername((username == null || username.isBlank()) ? null : username);
            user.setPhoneNumber(phoneNumber);
            user.setEmail(email);
            user.setPassword(password); // let service encode
            user.setProfession(profession);
            user.setCity(city);
            user.setCountry(country);
            user.setRole(role != null ? role : Role.USER);
            user.setEnabled(false); // let service handle verification
            if (intention != null) user.setIntention(intention);
            if (profileDescription != null) user.setProfileDescription(profileDescription);
            if (profilePicture != null) user.setProfilePicture(profilePicture);
            if (favourites != null) user.setFavourites(favourites);
            if (dateOfBirth != null) user.setDateOfBirth(LocalDate.parse(dateOfBirth));
            if (profileCompleted != null) user.setProfileCompleted(profileCompleted);
            if (onboardingCompleted != null) user.setOnboardingCompleted(onboardingCompleted);
            if (subscriptionPlan != null) user.setSubscriptionPlan(subscriptionPlan);
            if (subscriptionActive != null) user.setSubscriptionActive(subscriptionActive);
            if (openVisitations != null) user.setOpenVisitations(openVisitations);
            if (paymentVerified != null) user.setPaymentVerified(paymentVerified);
            if (totalEarnings != null) user.setTotalEarnings(totalEarnings);
            if (completedFeatures != null) user.setCompletedFeatures(completedFeatures);
            // PayoutInfo
            boolean hasPayout = payoutAccountNumber != null || payoutBankCode != null || payoutBankName != null || payoutAccountHolderName != null || payoutRecipientCode != null || payoutBvn != null || payoutEmailForPayouts != null || payoutVerified != null || payoutCurrency != null || payoutLastUpdated != null;
            if (hasPayout) {
                PayoutInfo payoutInfo = new PayoutInfo();
                if (payoutAccountNumber != null) payoutInfo.setAccountNumber(payoutAccountNumber);
                if (payoutBankCode != null) payoutInfo.setBankCode(payoutBankCode);
                if (payoutBankName != null) payoutInfo.setBankName(payoutBankName);
                if (payoutAccountHolderName != null) payoutInfo.setAccountHolderName(payoutAccountHolderName);
                if (payoutRecipientCode != null) payoutInfo.setRecipientCode(payoutRecipientCode);
                if (payoutBvn != null) payoutInfo.setBvn(payoutBvn);
                if (payoutEmailForPayouts != null) payoutInfo.setEmailForPayouts(payoutEmailForPayouts);
                if (payoutVerified != null) payoutInfo.setVerified(payoutVerified);
                if (payoutCurrency != null) payoutInfo.setCurrency(payoutCurrency);
                if (payoutLastUpdated != null) payoutInfo.setLastUpdated(LocalDateTime.parse(payoutLastUpdated));
                user.setPayoutInfo(payoutInfo);
            }
            return (User) userService.registerUser(user).get("user");
        } catch (Exception e) {
            logger.error("User creation failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @MutationMapping
    public User updateUser(
            @Argument UUID id, @Argument String firstName, @Argument String lastName,
            @Argument String username, @Argument String phoneNumber, @Argument String email,
            @Argument String password, @Argument String dateOfBirth,
            @Argument String profession, @Argument String city, @Argument String country,
            @Argument Role role,
            @Argument Boolean profileCompleted,
            @Argument Boolean onboardingCompleted,
            @Argument String subscriptionPlan,
            @Argument Boolean subscriptionActive,
            @Argument UserIntention intention, @Argument String profileDescription,
            @Argument String profilePicture, @Argument List<UUID> favourites,
            @Argument Boolean openVisitations, @Argument Boolean paymentVerified,
            @Argument Double totalEarnings, @Argument Set<OnboardingFeature> completedFeatures,
            @Argument Boolean emailNotificationsEnabled,
            @Argument Boolean smsNotificationsEnabled,
            @Argument Boolean pushNotificationsEnabled,
            @Argument Integer bufferTimeHours,
            @Argument String fcmDeviceToken,
            @Argument Integer searchRadius,
            @Argument Boolean priceAlerts,
            @Argument Boolean newListingAlerts,
            @Argument Boolean visitReminders,
            @Argument Boolean autoSaveSearches,
            @Argument Double maxBudget,
            @Argument List<String> preferredPropertyTypes,
            @Argument List<String> preferredAmenities,
            @Argument dev.dolu.userservice.models.BusinessType businessType,
            @Argument dev.dolu.userservice.models.VisitDuration visitDuration
    ) {
        try {
            // 1. Find user with proper error handling
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));

            // 2. Validate email uniqueness if email is being updated
            if (email != null && !email.equals(user.getEmail())) {
                User existingUserWithEmail = userRepository.findByEmail(email);
                if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(id)) {
                    throw new BadRequestException("Email address is already in use by another user");
                }
            }

            // 3. Validate phone number uniqueness if phone is being updated
            if (phoneNumber != null && !phoneNumber.equals(user.getPhoneNumber())) {
                User existingUserWithPhone = userRepository.findByPhoneNumber(phoneNumber);
                if (existingUserWithPhone != null && !existingUserWithPhone.getId().equals(id)) {
                    throw new BadRequestException("Phone number is already in use by another user");
                }
            }

            // 4. Validate date of birth format and age
            if (dateOfBirth != null) {
                try {
                    LocalDate dob = LocalDate.parse(dateOfBirth);
                    int age = java.time.Period.between(dob, LocalDate.now()).getYears();
                    if (age < 16) {
                        throw new BadRequestException("User must be at least 16 years old");
                    }
                    user.setDateOfBirth(dob);
                } catch (java.time.format.DateTimeParseException e) {
                    throw new BadRequestException("Invalid date format for date of birth. Use YYYY-MM-DD format");
                }
            }

            // 5. Validate email format
            if (email != null && !isValidEmail(email)) {
                throw new BadRequestException("Invalid email format");
            }

            // 6. Update user fields with validation
            if (firstName != null) {
                if (firstName.trim().isEmpty()) {
                    throw new BadRequestException("First name cannot be empty");
                }
                user.setFirstName(firstName.trim());
            }
            if (lastName != null) {
                if (lastName.trim().isEmpty()) {
                    throw new BadRequestException("Last name cannot be empty");
                }
                user.setLastName(lastName.trim());
            }
            if (username != null) user.setUsername(username.trim());
            if (phoneNumber != null) user.setPhoneNumber(phoneNumber.trim());
            if (email != null) user.setEmail(email.toLowerCase().trim());
            if (password != null) user.setPassword(passwordEncoder.encode(password));
            if (profession != null) user.setProfession(profession.trim());
            if (city != null) user.setCity(city.trim());
            if (country != null) user.setCountry(country.trim());
            if (role != null) user.setRole(role);
            if (profileCompleted != null) user.setProfileCompleted(profileCompleted);
            if (onboardingCompleted != null) user.setOnboardingCompleted(onboardingCompleted);
            if (subscriptionPlan != null) user.setSubscriptionPlan(subscriptionPlan.trim());
            if (subscriptionActive != null) user.setSubscriptionActive(subscriptionActive);
            if (intention != null) user.setIntention(intention);
            if (profileDescription != null) user.setProfileDescription(profileDescription.trim());
            if (profilePicture != null) user.setProfilePicture(profilePicture.trim());
            if (favourites != null) user.setFavourites(favourites);
            if (openVisitations != null) user.setOpenVisitations(openVisitations);
            if (paymentVerified != null) user.setPaymentVerified(paymentVerified);
            if (totalEarnings != null) user.setTotalEarnings(totalEarnings);
            if (completedFeatures != null) user.setCompletedFeatures(completedFeatures);
            if (emailNotificationsEnabled != null) user.setEmailNotificationsEnabled(emailNotificationsEnabled);
            if (smsNotificationsEnabled != null) user.setSmsNotificationsEnabled(smsNotificationsEnabled);
            if (pushNotificationsEnabled != null) user.setPushNotificationsEnabled(pushNotificationsEnabled);
            if (bufferTimeHours != null) user.setBufferTimeHours(bufferTimeHours);
            if (fcmDeviceToken != null) user.setFcmDeviceToken(fcmDeviceToken.trim());
            if (searchRadius != null) user.setSearchRadius(searchRadius);
            if (priceAlerts != null) user.setPriceAlerts(priceAlerts);
            if (newListingAlerts != null) user.setNewListingAlerts(newListingAlerts);
            if (visitReminders != null) user.setVisitReminders(visitReminders);
            if (autoSaveSearches != null) user.setAutoSaveSearches(autoSaveSearches);
            if (maxBudget != null) user.setMaxBudget(maxBudget);
            if (preferredPropertyTypes != null) user.setPreferredPropertyTypes(preferredPropertyTypes);
            if (preferredAmenities != null) user.setPreferredAmenities(preferredAmenities);
            if (businessType != null) user.setBusinessType(businessType);
            if (visitDuration != null) user.setVisitDuration(visitDuration);

            // 7. Save with database constraint error handling
            try {
                return userRepository.save(user);
            } catch (DataIntegrityViolationException e) {
                String message = e.getMessage().toLowerCase();
                if (message.contains("email")) {
                    throw new BadRequestException("Email address is already in use");
                } else if (message.contains("phone")) {
                    throw new BadRequestException("Phone number is already in use");
                } else {
                    throw new BadRequestException("A database constraint was violated. Please check your data");
                }
            }
        } catch (NotFoundException | BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating user: ", e);
            throw new BadRequestException("An unexpected error occurred while updating the user profile");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") &&
                email.length() <= 255;
    }
}
