package dev.dolu.userservice.controller;

import dev.dolu.userservice.models.PayoutInfoRequest;
import dev.dolu.userservice.models.PayoutInfoResponse;
import dev.dolu.userservice.service.PayoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{id}/payout-info")
public class PayoutController {

    private final PayoutService payoutService;

    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @PostMapping
    public ResponseEntity<PayoutInfoResponse> resolveAndSavePayoutInfo(@PathVariable UUID id, @RequestBody PayoutInfoRequest request) {
        try {
            PayoutInfoResponse response = payoutService.resolveAndSavePayoutInfo(id, request);
            if (!response.isSuccess()) {
                return ResponseEntity.status(502).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // If the cause is user not found, return 404, else 400
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("user not found")) {
                return ResponseEntity.status(404).body(new PayoutInfoResponse(false, null));
            }
            return ResponseEntity.badRequest().body(new PayoutInfoResponse(false, null));
        }
    }

    @GetMapping
    public ResponseEntity<PayoutInfoResponse> getPayoutInfo(@PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        // JWT validation is handled by security filter
        PayoutInfoResponse response = payoutService.getPayoutInfo(id);
        if (!response.isSuccess()) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
