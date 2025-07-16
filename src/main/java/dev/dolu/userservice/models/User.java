package dev.dolu.userservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "qorelabs_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "phoneNumber")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String firstName;
    private String lastName;
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    @NotBlank
    @Size(min = 8)
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    private boolean verified = false;
    private boolean enabled = true;
    private String profileImage;

    private String city;
    private String country;

    private LocalDate dateOfBirth;
    private String profession;
    private String activePlan;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // New tracking fields
    @Column
    private LocalDateTime lastLogin;

    @Column()
    private boolean profileCompleted = false;

    @Column()
    private boolean onboardingCompleted = false;

    @Column
    private String subscriptionPlan;

    @Column()
    private boolean subscriptionActive = false;

    @ElementCollection
    @CollectionTable(name = "user_favourites", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "listing_id")
    private List<UUID> favourites;

    @Enumerated(EnumType.STRING)
    private UserIntention intention;

    private String profileDescription;
    private String profilePicture;

    @Embedded
    private PayoutInfo payoutInfo; // TODO: Consider resolving some payout fields from the payment service for business logic

    @Column
    private Double totalEarnings = 0.0; // TODO: Increment this from the payment service whenever a payout is completed

    @Column(nullable = false)
    private boolean openVisitations = false;

    @Column(nullable = false)
    private boolean paymentVerified = false;

    @ElementCollection(targetClass = OnboardingFeature.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_onboarding_features", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "feature")
    private Set<OnboardingFeature> completedFeatures = new HashSet<>();

    @Version
    private Long version = 0L;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Enumerated(EnumType.STRING)
    private VisitDuration visitDuration = VisitDuration.getDefault();

    @Column(nullable = false)
    private boolean autoAcceptBooking = false;

    @Column(nullable = false)
    private boolean autoAcceptVisitation = false;

    @Column(nullable = false)
    private boolean emailNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean smsNotificationsEnabled = false;

    @Column(nullable = false)
    private boolean pushNotificationsEnabled = false;

    @Column(nullable = false)
    private int bufferTimeHours = 0;

    @Column(length = 512)
    private String fcmDeviceToken;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters (same as before)

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getActivePlan() {
        return activePlan;
    }

    public void setActivePlan(String activePlan) {
        this.activePlan = activePlan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public boolean isSubscriptionActive() {
        return subscriptionActive;
    }

    public void setSubscriptionActive(boolean subscriptionActive) {
        this.subscriptionActive = subscriptionActive;
    }

    public List<UUID> getFavourites() {
        return favourites;
    }

    public void setFavourites(List<UUID> favourites) {
        this.favourites = favourites;
    }

    public UserIntention getIntention() {
        return intention;
    }

    public void setIntention(UserIntention intention) {
        this.intention = intention;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public PayoutInfo getPayoutInfo() {
        return payoutInfo;
    }

    public void setPayoutInfo(PayoutInfo payoutInfo) {
        this.payoutInfo = payoutInfo;
    }

    public Double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(Double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public void increaseTotalEarnings(Double amount) {
        if (amount != null && amount > 0) {
            if (this.totalEarnings == null) this.totalEarnings = 0.0;
            this.totalEarnings += amount;
        }
    }

    public boolean isOpenVisitations() {
        return openVisitations;
    }

    public void setOpenVisitations(boolean openVisitations) {
        this.openVisitations = openVisitations;
    }

    public boolean isPaymentVerified() {
        return paymentVerified;
    }

    public void setPaymentVerified(boolean paymentVerified) {
        this.paymentVerified = paymentVerified;
    }

    public Set<OnboardingFeature> getCompletedFeatures() {
        return completedFeatures;
    }

    public void setCompletedFeatures(Set<OnboardingFeature> completedFeatures) {
        this.completedFeatures = completedFeatures;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
    }

    public VisitDuration getVisitDuration() {
        return visitDuration;
    }

    public void setVisitDuration(VisitDuration visitDuration) {
        this.visitDuration = visitDuration;
    }

    public boolean isAutoAcceptBooking() {
        return autoAcceptBooking;
    }

    public void setAutoAcceptBooking(boolean autoAcceptBooking) {
        this.autoAcceptBooking = autoAcceptBooking;
    }

    public boolean isAutoAcceptVisitation() {
        return autoAcceptVisitation;
    }

    public void setAutoAcceptVisitation(boolean autoAcceptVisitation) {
        this.autoAcceptVisitation = autoAcceptVisitation;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public boolean isSmsNotificationsEnabled() {
        return smsNotificationsEnabled;
    }

    public void setSmsNotificationsEnabled(boolean smsNotificationsEnabled) {
        this.smsNotificationsEnabled = smsNotificationsEnabled;
    }

    public boolean isPushNotificationsEnabled() {
        return pushNotificationsEnabled;
    }

    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) {
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }

    public int getBufferTimeHours() {
        return bufferTimeHours;
    }

    public void setBufferTimeHours(int bufferTimeHours) {
        this.bufferTimeHours = bufferTimeHours;
    }

    public String getFcmDeviceToken() {
        return fcmDeviceToken;
    }

    public void setFcmDeviceToken(String fcmDeviceToken) {
        this.fcmDeviceToken = fcmDeviceToken;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}