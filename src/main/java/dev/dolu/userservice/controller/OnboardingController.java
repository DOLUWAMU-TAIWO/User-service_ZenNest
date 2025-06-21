package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.OnboardingFeature;
import dev.dolu.userservice.service.OnboardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{id}/onboarding")
public class OnboardingController {
    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/{feature}")
    public ResponseEntity<?> markFeatureCompleted(@PathVariable UUID id, @PathVariable String feature) {
        OnboardingFeature onboardingFeature;
        try {
            onboardingFeature = OnboardingFeature.valueOf(feature.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid feature name");
        }
        try {
            onboardingService.markFeatureCompleted(id, onboardingFeature);
            Set<OnboardingFeature> updated = onboardingService.getCompletedFeatures(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @GetMapping
    public ResponseEntity<?> getCompletedFeatures(@PathVariable UUID id) {
        try {
            Set<OnboardingFeature> features = onboardingService.getCompletedFeatures(id);
            return ResponseEntity.ok(features);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("User not found");
        }
    }
}

