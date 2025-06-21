package dev.dolu.userservice.service;

import dev.dolu.userservice.models.OnboardingFeature;
import dev.dolu.userservice.models.User;
import dev.dolu.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class OnboardingService {
    private final UserRepository userRepository;

    public OnboardingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void markFeatureCompleted(UUID userId, OnboardingFeature feature) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();
        user.getCompletedFeatures().add(feature);
        userRepository.save(user);
    }

    public Set<OnboardingFeature> getCompletedFeatures(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        return userOpt.get().getCompletedFeatures();
    }
}

