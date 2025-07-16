package dev.dolu.userservice.service;

import dev.dolu.userservice.models.BusinessType;
import dev.dolu.userservice.models.OnboardingFeature;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import dev.dolu.userservice.utils.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class OnboardingService {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingService.class);
    private final UserRepository userRepository;

    public OnboardingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get comprehensive onboarding progress for frontend
     * Returns the exact format expected by LandlordDashboard.jsx
     */
    public Map<String, Object> getOnboardingProgress(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<OnboardingFeature> completedFeatures = user.getCompletedFeatures();
        if (completedFeatures == null) {
            completedFeatures = new HashSet<>();
        }

        // Convert features to string array
        String[] completedSteps = completedFeatures.stream()
                .map(OnboardingFeature::name)
                .toArray(String[]::new);

        // Use explicit businessType field if set
        BusinessType businessTypeEnum = user.getBusinessType();
        String businessType = null;
        if (businessTypeEnum != null) {
            businessType = businessTypeEnum.name().toLowerCase();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("completedSteps", completedSteps);
        result.put("hasSeenWelcome", completedFeatures.contains(OnboardingFeature.WELCOME_MODAL_SEEN));
        if (businessType != null) {
            result.put("businessType", businessType);
        }
        result.put("skipOnboarding", user.isOnboardingCompleted());
        return result;
    }

    /**
     * Update onboarding progress with partial updates
     */
    public void updateOnboardingProgress(UUID userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<OnboardingFeature> features = user.getCompletedFeatures();
        if (features == null) {
            features = new HashSet<>();
        }

        boolean changed = false;

        // Handle hasSeenWelcome
        if (updates.containsKey("hasSeenWelcome") && Boolean.TRUE.equals(updates.get("hasSeenWelcome"))) {
            if (features.add(OnboardingFeature.WELCOME_MODAL_SEEN)) {
                changed = true;
                logger.debug("Added WELCOME_MODAL_SEEN for user: {}", userId);
            }
        }

        // Handle businessType
        if (updates.containsKey("businessType")) {
            String businessType = (String) updates.get("businessType");
            if (businessType != null) {
                BusinessType type = EnumUtils.safeValueOf(BusinessType.class, businessType);
                if (type != null) {
                    user.setBusinessType(type);
                } else {
                    logger.warn("Invalid business type in update: {}", businessType);
                }
                if (features.add(OnboardingFeature.BUSINESS_TYPE_SELECTED)) {
                    changed = true;
                    logger.debug("Added BUSINESS_TYPE_SELECTED for user: {} with type: {}", userId, businessType);
                }
                // Add specific intro features based on business type
                switch (businessType.trim().toLowerCase()) {
                    case "shortlet":
                        changed |= features.add(OnboardingFeature.SHORTLET_BOOKING_INTRO);
                        break;
                    case "longterm":
                        changed |= features.add(OnboardingFeature.VISIT_BOOKING_INTRO);
                        break;
                    case "both":
                        changed |= features.add(OnboardingFeature.SHORTLET_BOOKING_INTRO);
                        changed |= features.add(OnboardingFeature.VISIT_BOOKING_INTRO);
                        break;
                }
            }
        }

        // Handle skipOnboarding
        if (updates.containsKey("skipOnboarding") && Boolean.TRUE.equals(updates.get("skipOnboarding"))) {
            user.setOnboardingCompleted(true);
            changed = true;
            logger.debug("Set onboarding completed for user: {}", userId);
        }

        // Handle completedFeature (for backward compatibility)
        if (updates.containsKey("completedFeature")) {
            String featureName = (String) updates.get("completedFeature");
            OnboardingFeature feature = EnumUtils.safeValueOf(OnboardingFeature.class, featureName);
            if (feature != null) {
                if (features.add(feature)) {
                    changed = true;
                    logger.debug("Added feature {} for user: {}", featureName, userId);
                }
            } else {
                logger.warn("Invalid feature name in update: {}", featureName);
            }
        }

        if (changed) {
            user.setCompletedFeatures(features);
            userRepository.save(user);
            logger.debug("Updated onboarding progress for user: {}", userId);
        }
    }

    /**
     * Mark a specific feature as completed
     */
    public void markFeatureCompleted(UUID userId, OnboardingFeature feature) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<OnboardingFeature> features = user.getCompletedFeatures();
        if (features == null) {
            features = new HashSet<>();
        }

        if (features.add(feature)) {
            user.setCompletedFeatures(features);
            userRepository.save(user);
            logger.debug("Marked feature {} as completed for user: {}", feature, userId);

            // Auto-complete onboarding if key milestones are reached
            checkAndCompleteOnboarding(user, features);
        }
    }

    /**
     * Get completed features set
     */
    public Set<OnboardingFeature> getCompletedFeatures(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<OnboardingFeature> features = user.getCompletedFeatures();
        return features != null ? features : new HashSet<>();
    }

    /**
     * Check if user has completed enough features to mark onboarding as done
     */
    private void checkAndCompleteOnboarding(User user, Set<OnboardingFeature> features) {
        // Define completion criteria
        boolean hasSeenWelcome = features.contains(OnboardingFeature.WELCOME_MODAL_SEEN);
        boolean hasSelectedBusinessType = features.contains(OnboardingFeature.BUSINESS_TYPE_SELECTED);
        boolean hasCompletedTour = features.contains(OnboardingFeature.DASHBOARD_TOUR_COMPLETED);
        boolean hasCreatedListing = features.contains(OnboardingFeature.CREATE_FIRST_LISTING);

        // Auto-complete if user has done the essential onboarding steps
        if (hasSeenWelcome && hasSelectedBusinessType && (hasCompletedTour || hasCreatedListing)) {
            if (!user.isOnboardingCompleted()) {
                user.setOnboardingCompleted(true);
                logger.info("Auto-completed onboarding for user: {} based on completed features", user.getId());
            }
        }
    }

    /**
     * Reset onboarding progress (for testing/admin purposes)
     */
    public void resetOnboardingProgress(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setCompletedFeatures(new HashSet<>());
        user.setOnboardingCompleted(false);
        userRepository.save(user);

        logger.info("Reset onboarding progress for user: {}", userId);
    }

    /**
     * Get onboarding completion percentage
     */
    public Map<String, Object> getOnboardingStats(UUID userId) {
        Set<OnboardingFeature> completed = getCompletedFeatures(userId);

        // Core features for basic onboarding
        OnboardingFeature[] coreFeatures = {
                OnboardingFeature.WELCOME_MODAL_SEEN,
                OnboardingFeature.BUSINESS_TYPE_SELECTED,
                OnboardingFeature.DASHBOARD_TOUR_COMPLETED,
                OnboardingFeature.CREATE_FIRST_LISTING
        };

        long coreCompleted = 0;
        for (OnboardingFeature feature : coreFeatures) {
            if (completed.contains(feature)) {
                coreCompleted++;
            }
        }

        double corePercentage = (double) coreCompleted / coreFeatures.length * 100;
        double overallPercentage = (double) completed.size() / OnboardingFeature.values().length * 100;

        return Map.of(
                "totalFeatures", OnboardingFeature.values().length,
                "completedFeatures", completed.size(),
                "coreCompletionPercentage", Math.round(corePercentage),
                "overallCompletionPercentage", Math.round(overallPercentage),
                "isOnboardingComplete", coreCompleted >= coreFeatures.length
        );
    }
}