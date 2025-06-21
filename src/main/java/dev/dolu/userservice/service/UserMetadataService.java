package dev.dolu.userservice.service;

import dev.dolu.userservice.models.User;
import dev.dolu.userservice.models.UserMetadata;
import dev.dolu.userservice.repository.UserMetadataRepository;
import dev.dolu.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserMetadataService {
    @Autowired
    private UserMetadataRepository userMetadataRepository;

    @Autowired
    private UserRepository userRepository;

    public UserMetadata createOrUpdateMetadata(UUID userId, String device, String sipAddress, String userAgent, String ipAddress) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();
        UserMetadata metadata = userMetadataRepository.findByUserId(userId);
        if (metadata == null) {
            metadata = new UserMetadata();
            metadata.setUser(user);
        }
        metadata.setDevice(device);
        metadata.setSipAddress(sipAddress);
        metadata.setUserAgent(userAgent);
        metadata.setIpAddress(ipAddress);
        return userMetadataRepository.save(metadata);
    }

    public UserMetadata getMetadataByUserId(UUID userId) {
        return userMetadataRepository.findByUserId(userId);
    }
}
