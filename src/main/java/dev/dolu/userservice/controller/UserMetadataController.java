package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.UserMetadata;
import dev.dolu.userservice.service.UserMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-metadata")
public class UserMetadataController {
    @Autowired
    private UserMetadataService userMetadataService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserMetadata> getMetadata(@PathVariable UUID userId) {
        UserMetadata metadata = userMetadataService.getMetadataByUserId(userId);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metadata);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<UserMetadata> createOrUpdateMetadata(
            @PathVariable UUID userId,
            @RequestParam(required = false) String device,
            @RequestParam(required = false) String sipAddress,
            @RequestParam(required = false) String userAgent,
            @RequestParam(required = false) String ipAddress) {
        UserMetadata metadata = userMetadataService.createOrUpdateMetadata(userId, device, sipAddress, userAgent, ipAddress);
        return ResponseEntity.ok(metadata);
    }
}
