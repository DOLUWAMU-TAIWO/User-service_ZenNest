package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.OnboardingFeature;
import dev.dolu.userservice.service.OnboardingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{id}/onboarding")
public class OnboardingController {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingController.class);
    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /**
     * Get onboarding progress for a user
     * Returns format expected by frontend: { completedSteps, hasSeenWelcome, businessType, skipOnboarding }
     */
    @GetMapping
    public ResponseEntity<?> getOnboardingProgress(@PathVariable UUID id) {
        try {
            logger.debug("Fetching onboarding progress for user: {}", id);

            var progress = onboardingService.getOnboardingProgress(id);

            return ResponseEntity.ok(progress);

        } catch (IllegalArgumentException e) {
            logger.warn("User not found for onboarding progress: {}", id);
            // Return default progress for new users instead of 404
            return ResponseEntity.ok(Map.of(
                    "completedSteps", new String[0],
                    "hasSeenWelcome", false,
                    "businessType", (Object) null,
                    "skipOnboarding", false
            ));
        } catch (Exception e) {
            logger.error("Error fetching onboarding progress for user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to fetch onboarding progress"
            ));
        }
    }

    /**
     * Update onboarding progress (PATCH for partial updates)
     * Handles: hasSeenWelcome, businessType, skipOnboarding
     */
    @PatchMapping
    public ResponseEntity<?> updateOnboardingProgress(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> updates) {

        try {
            logger.debug("Updating onboarding progress for user: {} with updates: {}", id, updates);

            onboardingService.updateOnboardingProgress(id, updates);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "updatedAt", java.time.Instant.now().toString()
            ));

        } catch (IllegalArgumentException e) {
            logger.warn("User not found for onboarding update: {}", id);
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "User not found"
            ));
        } catch (Exception e) {
            logger.error("Error updating onboarding progress for user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to update onboarding progress"
            ));
        }
    }

    /**
     * Mark specific feature as completed
     * Used by frontend components to track feature usage
     */
    @PostMapping("/{feature}")
    public ResponseEntity<?> markFeatureCompleted(
            @PathVariable UUID id,
            @PathVariable String feature,
            @RequestBody(required = false) Map<String, Object> requestBody) {

        try {
            OnboardingFeature onboardingFeature = OnboardingFeature.valueOf(feature.toUpperCase());

            logger.debug("Marking feature {} as completed for user: {}", feature, id);

            onboardingService.markFeatureCompleted(id, onboardingFeature);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "feature", feature.toUpperCase(),
                    "completedAt", java.time.Instant.now().toString()
            ));

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains(feature)) {
                logger.warn("Invalid feature name: {}", feature);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Invalid feature name: " + feature
                ));
            } else {
                logger.warn("User not found for feature completion: {}", id);
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", "User not found"
                ));
            }
        } catch (Exception e) {
            logger.error("Error marking feature {} as completed for user {}: {}", feature, id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Internal server error"
            ));
        }
    }

    /**
     * Get completed features (legacy endpoint for backward compatibility)
     */
    @GetMapping("/features")
    public ResponseEntity<?> getCompletedFeatures(@PathVariable UUID id) {
        try {
            Set<OnboardingFeature> features = onboardingService.getCompletedFeatures(id);

            String[] featureNames = features.stream()
                    .map(OnboardingFeature::name)
                    .toArray(String[]::new);

            return ResponseEntity.ok(Map.of("features", featureNames));

        } catch (IllegalArgumentException e) {
            logger.warn("User not found for completed features: {}", id);
            return ResponseEntity.status(404).body(Map.of(
                    "error", "User not found"
            ));
        } catch (Exception e) {
            logger.error("Error fetching completed features for user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal server error"
            ));
        }
    }
}