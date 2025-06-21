package dev.dolu.userservice.service;

import java.util.UUID;

import dev.dolu.userservice.models.PayoutInfoRequest;
import dev.dolu.userservice.models.PayoutInfoResponse;

public interface PayoutService {
    PayoutInfoResponse resolveAndSavePayoutInfo(UUID userId, PayoutInfoRequest request) throws Exception;
}
